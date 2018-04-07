package fr.ralala.bleconnector.callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.ralala.bleconnector.utils.gatt.services.BatteryService;
import fr.ralala.bleconnector.utils.gatt.services.CurrentTimeService;
import fr.ralala.bleconnector.utils.gatt.services.Service;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE GATT server callback.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattServerCallback extends BluetoothGattServerCallback {
  public static final int CURRENT_TIME_SERVICE = 0;
  public static final int BATTERY_SERVICE = 1;
  /* Mandatory Client Characteristic Config Descriptor */
  public static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private static final UUID CLIENT_DESCRIPTION = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
  private BluetoothGattServer mGattServer = null;
  /* Collection of notification subscribers */
  private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();
  private static final List<Service> mServices;

  static {
    mServices = new ArrayList<>();
    mServices.add(CURRENT_TIME_SERVICE, new CurrentTimeService());
    mServices.add(BATTERY_SERVICE, new BatteryService());
  }

  public GattServerCallback() {
    for(Service service : mServices)
      service.setRegisteredDevices(mRegisteredDevices);
  }


  public void setGattServer(BluetoothGattServer gattServer) {
    mGattServer = gattServer;
    for(Service service : mServices)
      service.setGattServer(mGattServer);
  }

  public Service getService(int type) {
    return mServices.get(type);
  }

  public List<Service> getServices() {
    return mServices;
  }

  @Override
  public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
    if (newState == BluetoothProfile.STATE_CONNECTED) {
      Log.i(getClass().getSimpleName(), "BluetoothDevice CONNECTED: " + device);
    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
      Log.i(getClass().getSimpleName(), "BluetoothDevice DISCONNECTED: " + device);
      //Remove device from any active subscriptions
      mRegisteredDevices.remove(device);
    }
  }

  @Override
  public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

    for(Service service : mServices)
      if(service.isRegistered() && service.onProcessCharacteristicReadRequest(device, requestId, characteristic)) {
        return;
      }
    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
  }

  @Override
  public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                      BluetoothGattDescriptor descriptor) {
    Log.i(getClass().getSimpleName(), "onDescriptorReadRequest: " + descriptor);
    if (CLIENT_CONFIG.equals(descriptor.getUuid())) {
      Log.i(getClass().getSimpleName(), "Config descriptor read");
      byte[] returnValue;
      if (mRegisteredDevices.contains(device)) {
        returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
      } else {
        returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
      }
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, returnValue);
    } else {
      Log.w(getClass().getSimpleName(), "Unknown descriptor read request");
      mGattServer.sendResponse(device,
          requestId, BluetoothGatt.GATT_FAILURE, 0, null);
    }
  }

  @Override
  public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                       BluetoothGattDescriptor descriptor,
                                       boolean preparedWrite, boolean responseNeeded,
                                       int offset, byte[] value) {
    Log.i(getClass().getSimpleName(), "onDescriptorWriteRequest: " + descriptor);
    if (CLIENT_CONFIG.equals(descriptor.getUuid())) {
      if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
        Log.i(getClass().getSimpleName(), "Subscribe device to notifications: " + device);
        mRegisteredDevices.add(device);
      } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
        Log.i(getClass().getSimpleName(), "Unsubscribe device from notifications: " + device);
        mRegisteredDevices.remove(device);
      }

      if (responseNeeded) {
        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
      }
    } else {
      Log.w(getClass().getSimpleName(), "Unknown descriptor write request");
      if (responseNeeded) {
        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
      }
    }
  }

  /*-------------------------------------------*/
  // UTILS
  /*-------------------------------------------*/
  public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
        CLIENT_DESCRIPTION, (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    try {
      descriptor.setValue(defaultValue.getBytes("UTF-8"));
    } catch (Exception e){
      Log.w(GattServerCallback.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return descriptor;
  }
  public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
        CLIENT_CONFIG, (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    descriptor.setValue(new byte[]{0, 0});
    return descriptor;
  }
}
