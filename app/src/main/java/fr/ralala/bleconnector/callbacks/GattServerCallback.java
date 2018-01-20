package fr.ralala.bleconnector.callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;

import java.util.Calendar;
import java.util.UUID;

import fr.ralala.bleconnector.utils.TimeData;
import fr.ralala.bleconnector.utils.gatt.GattUUID;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE GATT server callback.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattServerCallback extends BluetoothGattServerCallback {
  // UUID for Current Time Service (CTS)
  public static final UUID CTS_SERVICE_UUID = UUID.fromString(GattUUID.SERVICE_CURRENT_TIME.uuid);
  private static final UUID CURRENT_TIME_CHARACTERISTIC_UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
  private static final UUID LOCAL_TIME_INFO_CHARACTERISTIC_UUID = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
  public static final BluetoothGattService CTS_GATT_SERVICE = new BluetoothGattService(CTS_SERVICE_UUID,
      BluetoothGattService.SERVICE_TYPE_PRIMARY);
  private BluetoothGattServer mGattServer = null;

  static {
    CTS_GATT_SERVICE.addCharacteristic(
        new BluetoothGattCharacteristic(CURRENT_TIME_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ)
    );
    CTS_GATT_SERVICE.addCharacteristic(
        new BluetoothGattCharacteristic(LOCAL_TIME_INFO_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ)
    );
  }

  public void setGattServer(BluetoothGattServer gattServer) {
    mGattServer = gattServer;
  }

  @Override
  public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
    if (CURRENT_TIME_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.exactTime256WithUpdateReason(Calendar.getInstance(), TimeData.UPDATE_REASON_UNKNOWN));
    } else if (LOCAL_TIME_INFO_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.timezoneWithDstOffset(Calendar.getInstance()));
    } else {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
    }
  }
}
