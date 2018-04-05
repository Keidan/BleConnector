package fr.ralala.bleconnector.callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.ralala.bleconnector.utils.gatt.cts.ServerCTS;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE GATT server callback.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattServerCallback extends BluetoothGattServerCallback {
  /* Mandatory Client Characteristic Config Descriptor */
  private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private BluetoothGattServer mGattServer = null;
  /* Collection of notification subscribers */
  private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();
  private final ServerCTS mServerCTS;

  public GattServerCallback() {
    mServerCTS = new ServerCTS(mRegisteredDevices);
  }


  public void setGattServer(BluetoothGattServer gattServer) {
    mGattServer = gattServer;
    mServerCTS.setGattServer(mGattServer);
  }

  public ServerCTS getServerCTS() {
    return mServerCTS;
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
    if(mServerCTS.isRegistered() && mServerCTS.onProcessCharacteristicReadRequest(device, requestId, characteristic)) {
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
}
