package fr.ralala.bleconnector.utils.gatt.services;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import fr.ralala.bleconnector.callbacks.GattServerCallback;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT current time service.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class CurrentTimeService extends Service {
  /* Service UUID */
  public static final UUID SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
  /* Mandatory Characteristic */
  private static final UUID CHARACTERISTIC = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
  /* Optional Local Time Information Characteristic */
  private static final UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
  private static final BluetoothGattService GATT_SERVICE;

  static {
    GATT_SERVICE = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
    BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ);
    characteristic.addDescriptor(GattServerCallback.getClientCharacteristicConfigurationDescriptor());
    GATT_SERVICE.addCharacteristic(characteristic);
    characteristic = new BluetoothGattCharacteristic(LOCAL_TIME_INFO,
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ);
    characteristic.addDescriptor(GattServerCallback.getClientCharacteristicConfigurationDescriptor());
    GATT_SERVICE.addCharacteristic(characteristic);
  }

  /**
   * Listens for system time changes and triggers a notification to
   * Bluetooth subscribers.
   */
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      byte adjustReason;
      if(intent.getAction() == null) return;
      switch (intent.getAction()) {
        case Intent.ACTION_TIME_CHANGED:
          adjustReason = TimeData.ADJUST_MANUAL;
          break;
        case Intent.ACTION_TIMEZONE_CHANGED:
          adjustReason = TimeData.ADJUST_TIMEZONE;
          break;
        default:
        case Intent.ACTION_TIME_TICK:
          adjustReason = TimeData.ADJUST_NONE;
          break;
      }
      long now = System.currentTimeMillis();
      notifyRegisteredDevices(now, adjustReason);
    }
  };


  /**
   * Returns the BroadcastReceiver to use with [un]registerBroadcast methods.
   * @return The BroadcastReceiver or null if no BroadcastReceiver should be used.
   */
  protected BroadcastReceiver getBroadcastReceiver() {
    return mReceiver;
  }

  /**
   * Returns the BroadcastReceiver to use with the BroadcastReceiver.
   * @return The IntentFilter or null if no BroadcastReceiver should be used.
   */
  protected IntentFilter getIntentFilter() {
    // Register for system clock events
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_TIME_TICK);
    filter.addAction(Intent.ACTION_TIME_CHANGED);
    filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    return filter;
  }

  /**
   * Returns the current service.
   * @return BluetoothGattService
   */
  protected BluetoothGattService getBluetoothGattService() {
    return GATT_SERVICE;
  }

  /**
   * Send a time service notification to any devices that are subscribed
   * to the characteristic.
   */
  private void notifyRegisteredDevices(long timestamp, byte adjustReason) {
    if (mDevices.isEmpty()) {
      Log.i(getClass().getSimpleName(), "No subscribers registered timestamp: " + timestamp + ", adjustReason: " + adjustReason);
      return;
    }
    if(mRegistered) {
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(timestamp);
      byte[] exactTime = TimeData.exactTime256WithUpdateReason(c, adjustReason);

      Log.i(getClass().getSimpleName(), "Sending current time update to " + mDevices.size() + " subscribers");
      for (BluetoothDevice device : mDevices) {
        BluetoothGattCharacteristic characteristic = mGattServer.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC);
        characteristic.setValue(exactTime);
        boolean indicate = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        mGattServer.notifyCharacteristicChanged(device, characteristic, indicate);
      }
    }
  }

  /**
   * Called by the method onCharacteristicReadRequest.
   * @param device The remote device that has requested the read operation.
   * @param requestId The Id of the request.
   * @param characteristic Characteristic to be read.
   * @return true if processed.
   */
  @Override
  public boolean onProcessCharacteristicReadRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic) {
    if (CHARACTERISTIC.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.exactTime256WithUpdateReason(Calendar.getInstance(), TimeData.ADJUST_NONE));
    } else if (LOCAL_TIME_INFO.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.timezoneWithDstOffset(Calendar.getInstance()));
    } else
      return false;
    return true;
  }

}
