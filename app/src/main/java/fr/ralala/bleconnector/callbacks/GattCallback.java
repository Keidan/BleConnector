package fr.ralala.bleconnector.callbacks;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.activities.GattActivity;
import fr.ralala.bleconnector.adapters.GattInspectListAdapter;
import fr.ralala.bleconnector.adapters.GattReadListAdapter;
import fr.ralala.bleconnector.adapters.GattWriteListAdapter;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE GATT callback.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattCallback extends BluetoothGattCallback {
  // Stops scanning after 10 seconds.
  private static final long CONNECT_DELAY = 5000;
  private static final String TAG = "GattCallback";
  private GattActivity mGattActivity;
  private BluetoothGatt mBluetoothGatt;
  private GattInspectListAdapter mGattInspectListAdapter;
  private GattReadListAdapter mGattReadListAdapter;
  private GattWriteListAdapter mGattWriteListAdapter;
  private Handler mHandler = new Handler();
  private Runnable mRunnable;
  private List<GattReadListAdapter.Item> mPendingCharacteristicRead;
  private List<GattWriteListAdapter.Item> mPendingCharacteristicWrite;

  public GattCallback(GattActivity gattActivity) {
    mGattActivity = gattActivity;
    mRunnable = () -> {
      Toast.makeText(mGattActivity, R.string.gatt_connect_timeout, Toast.LENGTH_SHORT).show();
      mGattActivity.onBackPressed();
    };
    mPendingCharacteristicRead = new ArrayList<>();
    mPendingCharacteristicWrite = new ArrayList<>();
  }

  public void setGattInspectListAdapter(GattInspectListAdapter gattInspectListAdapter) {
    mGattInspectListAdapter = gattInspectListAdapter;
  }
  public void setGattReadListAdapter(GattReadListAdapter gattReadListAdapter) {
    mGattReadListAdapter = gattReadListAdapter;
  }
  public void setGattWriteListAdapter(GattWriteListAdapter gattWriteListAdapter) {
    mGattWriteListAdapter = gattWriteListAdapter;
  }


  public void readCharacteristic(List<GattReadListAdapter.Item> items) {
    if(items.isEmpty()) {
      mGattActivity.progressDismiss();
      return;
    }
    mPendingCharacteristicRead.addAll(items);
    if (!mBluetoothGatt.readCharacteristic(items.get(0).characteristic)) {
      mGattActivity.progressDismiss();
      items.get(0).characteristic.setValue(mGattActivity.getString(R.string.error));
      mGattReadListAdapter.notifyDataSetChanged();
    }
  }

  public boolean writeCharacteristic(GattWriteListAdapter.Item item) {
    if(mBluetoothGatt.writeCharacteristic(item.characteristic)) {
      mPendingCharacteristicWrite.add(item);
      return true;
    } else
      return false;
  }

  public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
    mBluetoothGatt = bluetoothGatt;
  }

  public void abort() {
    mHandler.removeCallbacks(mRunnable);
  }

  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                      int newState) {
    if (newState == BluetoothProfile.STATE_CONNECTED) {
      abort();
      mHandler.postDelayed(mRunnable, CONNECT_DELAY);
      Log.i(TAG, "Connected to GATT server.");
      boolean discovery = mBluetoothGatt.discoverServices();
      Log.i(TAG, "Attempting to start service discovery:" + discovery);
    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
      Log.i(TAG, "Disconnected from GATT server.");
      mHandler.removeCallbacks(mRunnable);
      mHandler.post(mRunnable);
    }
  }

  @Override
  // New services discovered
  public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
      Log.w(TAG, "onServicesDiscovered ACTION_GATT_SERVICES_DISCOVERED");
      abort();
      if(mGattInspectListAdapter != null)
        mGattActivity.runOnUiThread(() -> {
          mGattInspectListAdapter.clear();
          for (BluetoothGattService service : gatt.getServices()) {
            mGattInspectListAdapter.add(service);
          }
          mGattInspectListAdapter.notifyDataSetChanged();
          mGattActivity.notifyServicesDiscovered();
          mGattActivity.progressDismiss();
        });
    } else {
      Log.w(TAG, "onServicesDiscovered received: " + status);
    }
  }

  @Override
  // Result of a characteristic read operation
  public void onCharacteristicRead(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicRead: " + status + " - " + characteristic);
    if(!mPendingCharacteristicRead.isEmpty()) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        BluetoothGattCharacteristic charac = mPendingCharacteristicRead.get(0).characteristic;
        charac.setValue(characteristic.getValue());
        mGattActivity.runOnUiThread(() -> mGattReadListAdapter.notifyDataSetChanged());
      } else {
        mPendingCharacteristicRead.get(0).characteristic.setValue(getGattError(status));
        mGattActivity.runOnUiThread(() -> mGattReadListAdapter.notifyDataSetChanged());
      }
      mPendingCharacteristicRead.remove(0);
      if (mPendingCharacteristicRead.isEmpty()) {
        mGattActivity.runOnUiThread(() -> mGattActivity.progressDismiss());
      } else {
        if (!mBluetoothGatt.readCharacteristic(mPendingCharacteristicRead.get(0).characteristic)) {
          mPendingCharacteristicRead.get(0).characteristic.setValue(mGattActivity.getString(R.string.error));
          mGattActivity.runOnUiThread(() -> {
            mGattReadListAdapter.notifyDataSetChanged();
            mGattActivity.progressDismiss();
          });
        }
      }
    } else if(!mPendingCharacteristicWrite.isEmpty()) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        BluetoothGattCharacteristic charac = mPendingCharacteristicWrite.get(0).characteristic;
        mPendingCharacteristicWrite.remove(0);
        charac.setValue(characteristic.getValue());
      }
      mGattActivity.runOnUiThread(() -> {
        mGattWriteListAdapter.notifyDataSetChanged();
        mGattActivity.progressDismiss();
      });
    }
  }

  public void onCharacteristicWrite(BluetoothGatt gatt,
                                    BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicWrite: " + status + " - " + characteristic);
    GattWriteListAdapter.Item item = mPendingCharacteristicWrite.get(0);
    BluetoothGattCharacteristic charac = item.characteristic;
    if(!mPendingCharacteristicRead.isEmpty())
      item.characteristic.setValue(characteristic.getValue());
    else
      item.characteristic.setValue(getGattError(status));
    mGattActivity.runOnUiThread(() -> mGattWriteListAdapter.notifyDataSetChanged());
    if (status == BluetoothGatt.GATT_SUCCESS) {
      int props = charac.getProperties();
      if ((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0 && (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
        if (!mBluetoothGatt.readCharacteristic(charac)) {
          item.characteristic.setValue(mGattActivity.getString(R.string.error));
          mGattActivity.runOnUiThread(() -> {
            mGattWriteListAdapter.notifyDataSetChanged();
            mGattActivity.progressDismiss();
          });
        }
      } else {
        mPendingCharacteristicWrite.remove(0);
        mGattActivity.runOnUiThread(() -> mGattActivity.progressDismiss());
      }
    } else {
      mPendingCharacteristicWrite.remove(0);
      mGattActivity.runOnUiThread(() -> mGattActivity.progressDismiss());
    }
  }
  private String getGattError(int status) {
    String error;
    switch(status) {
      case BluetoothGatt.GATT_READ_NOT_PERMITTED:
        error = mGattActivity.getString(R.string.data_error_read_not_permitted);
        break;
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
        error = mGattActivity.getString(R.string.data_error_write_not_permitted);
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
        error = mGattActivity.getString(R.string.data_error_insufficient_authentication);
        break;
      case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
        error = mGattActivity.getString(R.string.data_error_not_supported);
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
        error = mGattActivity.getString(R.string.data_error_insufficient_encryption);
        break;
      case BluetoothGatt.GATT_INVALID_OFFSET:
        error = mGattActivity.getString(R.string.data_error_invalid_offset);
        break;
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
        error = mGattActivity.getString(R.string.data_error_exceeds_the_max_length);
        break;
      case BluetoothGatt.GATT_CONNECTION_CONGESTED:
        error = mGattActivity.getString(R.string.data_error_connection_is_congested);
        break;
      case BluetoothGatt.GATT_FAILURE:
        error = mGattActivity.getString(R.string.data_error_gatt_operation_failed);
        break;
      default:
        error = mGattActivity.getString(R.string.error);
    }
    return error;
  }
}
