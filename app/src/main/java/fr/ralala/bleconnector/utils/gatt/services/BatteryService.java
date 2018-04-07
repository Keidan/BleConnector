package fr.ralala.bleconnector.utils.gatt.services;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.util.UUID;

import fr.ralala.bleconnector.callbacks.GattServerCallback;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT battery service.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class BatteryService extends Service {
  /* Service UUID */
  public static final UUID SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
  /* Mandatory Characteristic */
  private static final UUID CHARACTERISTIC = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
  private static final String BATTERY_LEVEL_DESCRIPTION = "The current charge level of a " +
      "battery. 100%=fully charged and 0%=fully discharged.";

  private static final BluetoothGattService GATT_SERVICE;
  private int mPrevPercent = -1;

  static {
    GATT_SERVICE = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
    BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ);
    characteristic.addDescriptor(GattServerCallback.getClientCharacteristicConfigurationDescriptor());
    characteristic.addDescriptor(GattServerCallback.getCharacteristicUserDescriptionDescriptor(BATTERY_LEVEL_DESCRIPTION));
    GATT_SERVICE.addCharacteristic(characteristic);
  }

  /**
   * Listens for battery changes and triggers a notification to
   * Bluetooth subscribers.
   */
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int percent;
      if(intent.getAction() == null) {
        Log.e(getClass().getSimpleName(), "Battery receiver NULL action");
        return;
      }
      switch (intent.getAction()) {
        case Intent.ACTION_BATTERY_CHANGED:
          percent = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
          break;
        default:
          Log.e(getClass().getSimpleName(), "Battery receiver unsupported action: '" + intent.getAction() + "'");
          return;
      }
      if(mPrevPercent != percent) {
        mPrevPercent = percent;
        notifyRegisteredDevices(percent);
      }
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
    return new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
  }

  /**
   * Returns the current service.
   * @return BluetoothGattService
   */
  protected BluetoothGattService getBluetoothGattService() {
    return GATT_SERVICE;
  }

  /**
   * Send a battery notification to any devices that are subscribed
   * to the characteristic.
   */
  private void notifyRegisteredDevices(int newBatteryLevelPercent) {
    if (mDevices.isEmpty()) {
      Log.i(getClass().getSimpleName(), "No subscribers registered newBatteryLevel: " + newBatteryLevelPercent + "%");
      return;
    }
    if(mRegistered) {
      Log.i(getClass().getSimpleName(), "Sending battery update to " + mDevices.size() + " subscribers");
      for (BluetoothDevice device : mDevices) {
        BluetoothGattCharacteristic characteristic = mGattServer
            .getService(SERVICE_UUID)
            .getCharacteristic(CHARACTERISTIC);
        boolean indicate = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        characteristic.setValue(newBatteryLevelPercent, BluetoothGattCharacteristic.FORMAT_UINT8, /* offset */ 0);
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
    return false;
  }

}
