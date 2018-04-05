package fr.ralala.bleconnector.utils.gatt.cts;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT current time service.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class ServerCTS {
  /* Current Time Service UUID */
  public static final UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
  /* Mandatory Current Time Information Characteristic */
  private static final UUID CURRENT_TIME    = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
  /* Optional Local Time Information Characteristic */
  private static final UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
  private static final BluetoothGattService CTS_GATT_SERVICE;
  private BluetoothGattServer mGattServer = null;
  private boolean mRegistered = false;
  private boolean mRegisteredBroadcast = false;
  private Set<BluetoothDevice> mDevices;

  static {
    CTS_GATT_SERVICE = new BluetoothGattService(TIME_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
    CTS_GATT_SERVICE.addCharacteristic(
        new BluetoothGattCharacteristic(CURRENT_TIME,
            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ)
    );
    CTS_GATT_SERVICE.addCharacteristic(
        new BluetoothGattCharacteristic(LOCAL_TIME_INFO,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ)
    );
  }

  /**
   * Listens for system time changes and triggers a notification to
   * Bluetooth subscribers.
   */
  private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
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

  public ServerCTS(Set<BluetoothDevice> devices) {
    mDevices = devices;
  }

  public void setGattServer(BluetoothGattServer gattServer) {
    mGattServer = gattServer;
  }

  public void registerService(Context c) {
    if(!mRegistered && mGattServer != null) {
      mGattServer.addService(CTS_GATT_SERVICE);
      mRegistered = true;
      registerBroadcast(c);
    }
  }

  public void unregisterService(Context c) {
    if(mRegistered && mGattServer != null) {
      mGattServer.removeService(CTS_GATT_SERVICE);
      mRegistered = false;
      unregisterBroadcast(c);
    }
  }

  public void registerBroadcast(Context c) {
    if(mRegistered && !mRegisteredBroadcast) {
      // Register for system clock events
      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_TIME_TICK);
      filter.addAction(Intent.ACTION_TIME_CHANGED);
      filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
      c.registerReceiver(mTimeReceiver, filter);
      mRegisteredBroadcast = true;
    }
  }

  public void unregisterBroadcast(Context c) {
    if(mRegisteredBroadcast) {
      c.unregisterReceiver(mTimeReceiver);
      mRegisteredBroadcast = false;
    }
  }

  public boolean isRegistered() {
    return mRegistered;
  }

  /**
   * Send a time service notification to any devices that are subscribed
   * to the characteristic.
   */
  public void notifyRegisteredDevices(long timestamp, byte adjustReason) {
    if (mDevices.isEmpty()) {
      Log.i(getClass().getSimpleName(), "No subscribers registered timestamp: " + timestamp + ", adjustReason: " + adjustReason);
      return;
    }
    if(mRegistered) {
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(timestamp);
      byte[] exactTime = TimeData.exactTime256WithUpdateReason(c, adjustReason);

      Log.i(getClass().getSimpleName(), "Sending update to " + mDevices.size() + " subscribers");
      for (BluetoothDevice device : mDevices) {
        BluetoothGattCharacteristic timeCharacteristic = mGattServer
            .getService(TIME_SERVICE)
            .getCharacteristic(CURRENT_TIME);
        timeCharacteristic.setValue(exactTime);
        mGattServer.notifyCharacteristicChanged(device, timeCharacteristic, false);
      }
    }
  }

  public boolean onProcessCharacteristicReadRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic) {
    if (CURRENT_TIME.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.exactTime256WithUpdateReason(Calendar.getInstance(), TimeData.ADJUST_NONE));
    } else if (LOCAL_TIME_INFO.equals(characteristic.getUuid())) {
      mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, TimeData.timezoneWithDstOffset(Calendar.getInstance()));
    } else
      return false;
    return true;
  }

}
