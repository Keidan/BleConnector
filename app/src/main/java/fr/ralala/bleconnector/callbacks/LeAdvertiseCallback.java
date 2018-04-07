package fr.ralala.bleconnector.callbacks;


import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE GATT advertiser.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class LeAdvertiseCallback extends AdvertiseCallback {
  /**
   * Callback triggered in response to startAdvertising(AdvertiseSettings, AdvertiseData, AdvertiseCallback) indicating that the advertising has been started successfully.
   * @param settingsInEffect The actual settings used for advertising, which may be different from what has been requested.
   */
  @Override
  public void onStartSuccess(AdvertiseSettings settingsInEffect) {
    Log.i(getClass().getSimpleName(), "LE Advertise Started.");
  }

  /**
   * Callback when advertising could not be started.
   * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start failures.
   */
  @Override
  public void onStartFailure(int errorCode) {
    Log.w(getClass().getSimpleName(), "LE Advertise Failed: "+errorCode);
  }
}
