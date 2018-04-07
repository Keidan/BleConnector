package fr.ralala.bleconnector.utils.gatt.services;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Set;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT generic service.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public abstract class GenericService {

  protected Set<BluetoothDevice> mDevices;
  protected BluetoothGattServer mGattServer;
  protected boolean mRegistered = false;
  protected boolean mRegisteredBroadcast = false;

  /**
   * Sets the registered devices.
   * @param devices The registered devices.
   */
  public void setRegisteredDevices(Set<BluetoothDevice> devices) {
    mDevices = devices;
  }

  /**
   * Sets the reference to the GATT server.
   * @param gattServer The GATT server.
   */
  public void setGattServer(BluetoothGattServer gattServer) {
    mGattServer = gattServer;
  }

  /**
   * Registers the internal broadcast.
   * @param c The Android context.
   */
  public void registerBroadcast(Context c) {
    if(mRegistered && !mRegisteredBroadcast) {
      BroadcastReceiver br = getBroadcastReceiver();
      IntentFilter filter = getIntentFilter();
      if(br != null && filter != null)
        c.registerReceiver(br, filter);
      mRegisteredBroadcast = true;
    }
  }

  /**
   * Unregisters the internal broadcast.
   * @param c The Android context.
   */
  public void unregisterBroadcast(Context c) {
    if(mRegisteredBroadcast) {
      BroadcastReceiver br = getBroadcastReceiver();
      if(br != null)
        c.unregisterReceiver(br);
      mRegisteredBroadcast = false;
    }
  }

  /**
   * Registers the service.
   * @param c The Android context.
   */
  public void registerService(Context c) {
    if(!mRegistered && mGattServer != null) {
      mGattServer.addService(getBluetoothGattService());
      mRegistered = true;
      registerBroadcast(c);
    }
  }

  /**
   * Unregisters the service.
   * @param c The Android context.
   */
  public void unregisterService(Context c) {
    if(mRegistered && mGattServer != null) {
      mGattServer.removeService(getBluetoothGattService());
      mRegistered = false;
      unregisterBroadcast(c);
    }
  }

  /**
   * Tests if the service is registered.
   * @return boolean.
   */
  public boolean isRegistered() {
    return mRegistered;
  }

  /**
   * Called by the method onCharacteristicReadRequest.
   * @param device The remote device that has requested the read operation.
   * @param requestId The Id of the request.
   * @param characteristic Characteristic to be read.
   * @return true if processed.
   */
  public abstract boolean onProcessCharacteristicReadRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic);


  /*-------------------------------------------*/
  // Internal functions
  /*-------------------------------------------*/

  /**
   * Returns the BroadcastReceiver to use with [un]registerBroadcast methods.
   * @return The BroadcastReceiver or null if no BroadcastReceiver should be used.
   */
  protected abstract BroadcastReceiver getBroadcastReceiver();

  /**
   * Returns the BroadcastReceiver to use with the BroadcastReceiver.
   * @return The IntentFilter or null if no BroadcastReceiver should be used.
   */
  protected abstract IntentFilter getIntentFilter();

  /**
   * Returns the current service.
   * @return BluetoothGattService
   */
  protected abstract BluetoothGattService getBluetoothGattService();

}
