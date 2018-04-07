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

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentInspectListAdapter;
import fr.ralala.bleconnector.adapters.TabFragmentReadListAdapter;
import fr.ralala.bleconnector.adapters.TabFragmentWriteListAdapter;

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
  private MainActivity mActivity;
  private BluetoothGatt mBluetoothGatt;
  private TabFragmentInspectListAdapter mGattInspectListAdapter;
  private TabFragmentReadListAdapter mGattReadListAdapter;
  private TabFragmentWriteListAdapter mGattWriteListAdapter;
  private Handler mHandler = new Handler();
  private Runnable mRunnable;
  private List<TabFragmentReadListAdapter.Item> mPendingCharacteristicRead;
  private List<TabFragmentWriteListAdapter.Item> mPendingCharacteristicWrite;

  public GattCallback(MainActivity gattActivity) {
    mActivity = gattActivity;
    mRunnable = () -> {
      Toast.makeText(mActivity, R.string.gatt_connect_timeout, Toast.LENGTH_SHORT).show();
      mActivity.onBackPressed();
    };
    mPendingCharacteristicRead = new ArrayList<>();
    mPendingCharacteristicWrite = new ArrayList<>();
  }

  /**
   * Set the reference to the TabFragmentInspectListAdapter object.
   * @param gattInspectListAdapter TabFragmentInspectListAdapter
   */
  public void setGattInspectListAdapter(TabFragmentInspectListAdapter gattInspectListAdapter) {
    mGattInspectListAdapter = gattInspectListAdapter;
  }
  /**
   * Set the reference to the TabFragmentReadListAdapter object.
   * @param gattReadListAdapter TabFragmentReadListAdapter
   */
  public void setGattReadListAdapter(TabFragmentReadListAdapter gattReadListAdapter) {
    mGattReadListAdapter = gattReadListAdapter;
  }
  /**
   * Set the reference to the TabFragmentWriteListAdapter object.
   * @param gattWriteListAdapter TabFragmentWriteListAdapter
   */
  public void setGattWriteListAdapter(TabFragmentWriteListAdapter gattWriteListAdapter) {
    mGattWriteListAdapter = gattWriteListAdapter;
  }

  /**
   * Reads characteristics.
   * @param items The characteristics to write.
   */
  public void readCharacteristics(List<TabFragmentReadListAdapter.Item> items) {
    if(items.isEmpty()) {
      mActivity.progressDismiss();
      return;
    }
    mPendingCharacteristicRead.addAll(items);
    if (!mBluetoothGatt.readCharacteristic(items.get(0).characteristic)) {
      mActivity.progressDismiss();
      items.get(0).characteristic.setValue(mActivity.getString(R.string.error));
      mGattReadListAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Write a characteristic.
   * @param item The characteristic to write.
   * @return false on error, true else.
   */
  public boolean writeCharacteristic(TabFragmentWriteListAdapter.Item item) {
    if(mBluetoothGatt.writeCharacteristic(item.characteristic)) {
      mPendingCharacteristicWrite.add(item);
      return true;
    } else
      return false;
  }

  /**
   * Set the reference to the BluetoothGatt object.
   * @param bluetoothGatt BluetoothGatt
   */
  public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
    mBluetoothGatt = bluetoothGatt;
  }

  /**
   * Aborts the discovery.
   */
  public void abort() {
    mHandler.removeCallbacks(mRunnable);
  }

  /**
   * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
   * @param gatt GATT client.
   * @param status Status of the connect or disconnect operation. GATT_SUCCESS if the operation succeeds.
   * @param newState Returns the new connection state. Can be one of STATE_DISCONNECTED or STATE_CONNECTED.
   */
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

  /**
   * Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.
   * @param gatt GATT client invoked discoverServices()
   * @param status GATT_SUCCESS if the remote device has been explored successfully.
   */
  @Override
  public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
      Log.w(TAG, "onServicesDiscovered ACTION_GATT_SERVICES_DISCOVERED");
      abort();
      if(mGattInspectListAdapter != null)
        mActivity.runOnUiThread(() -> {
          mGattInspectListAdapter.clear();
          for (BluetoothGattService service : gatt.getServices()) {
            mGattInspectListAdapter.add(service);
          }
          mGattInspectListAdapter.notifyDataSetChanged();
          mActivity.notifyServicesDiscovered();
          mActivity.progressDismiss();
        });
    } else {
      Log.w(TAG, "onServicesDiscovered received: " + status);
    }
  }

  /**
   * Callback reporting the result of a characteristic read operation.
   * @param gatt GATT client invoked readCharacteristic(BluetoothGattCharacteristic).
   * @param characteristic Characteristic that was read from the associated remote device.
   * @param status GATT_SUCCESS if the read operation was completed successfully.
   */
  @Override
  public void onCharacteristicRead(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicRead: " + status + " - " + characteristic);
    if(!mPendingCharacteristicRead.isEmpty()) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        BluetoothGattCharacteristic charac = mPendingCharacteristicRead.get(0).characteristic;
        charac.setValue(characteristic.getValue());
        mActivity.runOnUiThread(() -> mGattReadListAdapter.notifyDataSetChanged());
      } else {
        mPendingCharacteristicRead.get(0).characteristic.setValue(getGattError(status));
        mActivity.runOnUiThread(() -> mGattReadListAdapter.notifyDataSetChanged());
      }
      mPendingCharacteristicRead.remove(0);
      if (mPendingCharacteristicRead.isEmpty()) {
        mActivity.runOnUiThread(() -> mActivity.progressDismiss());
      } else {
        if (!mBluetoothGatt.readCharacteristic(mPendingCharacteristicRead.get(0).characteristic)) {
          mPendingCharacteristicRead.get(0).characteristic.setValue(mActivity.getString(R.string.error));
          mActivity.runOnUiThread(() -> {
            mGattReadListAdapter.notifyDataSetChanged();
            mActivity.progressDismiss();
          });
        }
      }
    } else if(!mPendingCharacteristicWrite.isEmpty()) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        BluetoothGattCharacteristic charac = mPendingCharacteristicWrite.get(0).characteristic;
        mPendingCharacteristicWrite.remove(0);
        charac.setValue(characteristic.getValue());
      }
      mActivity.runOnUiThread(() -> {
        mGattWriteListAdapter.notifyDataSetChanged();
        mActivity.progressDismiss();
      });
    }
  }

  /**
   * Callback indicating the result of a characteristic write operation.
   * @param gatt GATT client invoked writeCharacteristic(BluetoothGattCharacteristic).
   * @param characteristic Characteristic that was written to the associated remote device.
   * @param status The result of the write operation GATT_SUCCESS if the operation succeeds.
   */
  public void onCharacteristicWrite(BluetoothGatt gatt,
                                    BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicWrite: " + status + " - " + characteristic);
    TabFragmentWriteListAdapter.Item item = mPendingCharacteristicWrite.get(0);
    BluetoothGattCharacteristic charac = item.characteristic;
    if(!mPendingCharacteristicRead.isEmpty())
      item.characteristic.setValue(characteristic.getValue());
    else
      item.characteristic.setValue(getGattError(status));
    mActivity.runOnUiThread(() -> mGattWriteListAdapter.notifyDataSetChanged());
    if (status == BluetoothGatt.GATT_SUCCESS) {
      int props = charac.getProperties();
      if ((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0 && (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
        if (!mBluetoothGatt.readCharacteristic(charac)) {
          item.characteristic.setValue(mActivity.getString(R.string.error));
          mActivity.runOnUiThread(() -> {
            mGattWriteListAdapter.notifyDataSetChanged();
            mActivity.progressDismiss();
          });
        }
      } else {
        mPendingCharacteristicWrite.remove(0);
        mActivity.runOnUiThread(() -> mActivity.progressDismiss());
      }
    } else {
      mPendingCharacteristicWrite.remove(0);
      mActivity.runOnUiThread(() -> mActivity.progressDismiss());
    }
  }

  /**
   * Converts a GATT error to String
   * @param status The error status.
   * @return String
   */
  private String getGattError(int status) {
    String error;
    switch(status) {
      case BluetoothGatt.GATT_READ_NOT_PERMITTED:
        error = mActivity.getString(R.string.data_error_read_not_permitted);
        break;
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
        error = mActivity.getString(R.string.data_error_write_not_permitted);
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
        error = mActivity.getString(R.string.data_error_insufficient_authentication);
        break;
      case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
        error = mActivity.getString(R.string.data_error_not_supported);
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
        error = mActivity.getString(R.string.data_error_insufficient_encryption);
        break;
      case BluetoothGatt.GATT_INVALID_OFFSET:
        error = mActivity.getString(R.string.data_error_invalid_offset);
        break;
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
        error = mActivity.getString(R.string.data_error_exceeds_the_max_length);
        break;
      case BluetoothGatt.GATT_CONNECTION_CONGESTED:
        error = mActivity.getString(R.string.data_error_connection_is_congested);
        break;
      case BluetoothGatt.GATT_FAILURE:
        error = mActivity.getString(R.string.data_error_gatt_operation_failed);
        break;
      default:
        error = mActivity.getString(R.string.error);
    }
    return error;
  }
}

