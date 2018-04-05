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
  @Override
  public void onStartSuccess(AdvertiseSettings settingsInEffect) {
    Log.i(getClass().getSimpleName(), "LE Advertise Started.");
  }

  @Override
  public void onStartFailure(int errorCode) {
    Log.w(getClass().getSimpleName(), "LE Advertise Failed: "+errorCode);
  }
}
