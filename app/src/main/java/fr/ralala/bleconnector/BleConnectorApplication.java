package fr.ralala.bleconnector;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fr.ralala.bleconnector.utils.gatt.GattHelper;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Application context.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class BleConnectorApplication extends Application {
  private static final String KEY_CTS = "kcts";
  public static final boolean DEFAULT_CTS = true;
  private GattHelper mGattHelper;
  private SharedPreferences mSharedPreferences;
  private static BleConnectorApplication singleton;

  public static BleConnectorApplication getInstance(){
    return singleton;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    singleton = this;
    mGattHelper = new GattHelper().loadFromAssets();
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
  }

  /**
   * Returns the instance to the GATT.
   * @return Gatt
   */
  public GattHelper getGattHelper() {
    return mGattHelper;
  }

  public boolean useCurrentTimeService() {
    return mSharedPreferences.getBoolean(KEY_CTS, DEFAULT_CTS);
  }
}
