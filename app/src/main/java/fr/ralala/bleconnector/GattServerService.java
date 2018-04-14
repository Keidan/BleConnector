package fr.ralala.bleconnector;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import fr.ralala.bleconnector.callbacks.GattServerCallback;
import fr.ralala.bleconnector.callbacks.LeAdvertiseCallback;
import fr.ralala.bleconnector.utils.gatt.services.BatteryService;
import fr.ralala.bleconnector.utils.gatt.services.CurrentTimeService;
import fr.ralala.bleconnector.utils.gatt.services.GenericService;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Service for the GATT server.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattServerService extends Service{
  private static final String EXIT_ACTION = "actionExit";
  private static final int NOTIFICATION_ID = 1001;
  private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
  private LeAdvertiseCallback mLeAdvertiseCallback;
  private BluetoothGattServer mBluetoothGattServer;
  private GattServerCallback mGattServerCallback;

  public static Intent getIntent() {
    return new Intent(BleConnectorApplication.getInstance(), GattServerService.class);
  }

  /**
   * Called by the system when the service is first created. Do not call this method directly.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    startForeground(NOTIFICATION_ID, getNotification());
  }

  /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
   */
  @Override
  public void onDestroy() {
    stopServer();
    super.onDestroy();
  }

  /**
   * Called when on service bind.
   * @param intent Unused.
   * @return null
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }

  /**
   * Called by the system every time a client explicitly starts the service
   * @param intent The Intent supplied to startService(Intent), as given.
   * @param flags Additional data about this start request.
   * @param startId A unique integer representing this specific request to start.
   * @return int
   */
  @Override
  public int onStartCommand(final Intent intent, final int flags,
                            final int startId) {
    /* Kill from notification */
    if(intent != null && intent.getAction() != null && intent.getAction().equals(EXIT_ACTION)) {
      // If you want to cancel the notification:
      NotificationManagerCompat.from(this).cancel(GattServerService.NOTIFICATION_ID);
      stopService(GattServerService.getIntent());
      //This is used to close the notification tray
      Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
      sendBroadcast(it);
      Process.killProcess(Process.myPid());
      return START_NOT_STICKY;
    }
    startServer();
    return START_STICKY;

  }

  /**
   * Starts the BLE server.
   */
  private void startServer() {
    BleConnectorApplication app = BleConnectorApplication.getInstance();
    if(app.isUseServer()) {
      BluetoothManager manager = (BluetoothManager) getSystemService(Activity.BLUETOOTH_SERVICE);
      if (manager != null) {
        mBluetoothLeAdvertiser = manager.getAdapter().getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser != null) {
          AdvertiseSettings settings = new AdvertiseSettings.Builder()
              .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
              .setConnectable(true)
              .setTimeout(0)
              .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
              .build();

        /* Adds all services */
          AdvertiseData data = new AdvertiseData.Builder()
              .setIncludeDeviceName(true)
              .setIncludeTxPowerLevel(true)
              .addServiceUuid(new ParcelUuid(CurrentTimeService.SERVICE_UUID))
              .addServiceUuid(new ParcelUuid(BatteryService.SERVICE_UUID))
              .build();
          mLeAdvertiseCallback = new LeAdvertiseCallback();
          mBluetoothLeAdvertiser
              .startAdvertising(settings, data, mLeAdvertiseCallback);
        }
        mGattServerCallback = new GattServerCallback();
        mBluetoothGattServer = manager.openGattServer(this, mGattServerCallback);
        if (mBluetoothGattServer == null) {
          Log.e(getClass().getSimpleName(), "Unable to start GATT server");
        } else {
          mGattServerCallback.setGattServer(mBluetoothGattServer);
          if (app.isUseCurrentTimeService())
            mGattServerCallback.getService(GattServerCallback.CURRENT_TIME_SERVICE).registerService(this);
          if (app.isUseBatteryService())
            mGattServerCallback.getService(GattServerCallback.BATTERY_SERVICE).registerService(this);
          for(GenericService service : mGattServerCallback.getServices())
            service.registerBroadcast(this);
        }
      } else
        Log.e(getClass().getSimpleName(), "Null BluetoothManager object");
    } else
      stopSelf();
  }

  /**
   * Stops the BLE server.
   */
  private void stopServer() {
    if(mGattServerCallback != null) {
      for(GenericService service : mGattServerCallback.getServices())
        service.unregisterBroadcast(this);
    }
    if (mBluetoothGattServer != null) {
      mBluetoothGattServer.close();
      mBluetoothGattServer = null;
    }
    if(mBluetoothLeAdvertiser != null)
      mBluetoothLeAdvertiser.stopAdvertising(mLeAdvertiseCallback);
  }

  /**
   * Returns the notification object.
   * @return Notification
   */
  public Notification getNotification() {
    Intent contentIntent = new Intent(this, MainActivity.class);
    contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);
    Intent intentAction = new Intent(this, GattServerService.class);
    intentAction.setAction(EXIT_ACTION);
    PendingIntent pIntent = PendingIntent.getService(this,0, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationCompat.Builder foregroundNotification = new NotificationCompat.Builder(this, "channel_id0");
    foregroundNotification.setOngoing(true);
    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_exit_black, getString(R.string.service_notification_exit), pIntent).build();
    foregroundNotification.addAction(action);
    foregroundNotification.setContentTitle(getString(R.string.service_notification_title))
        .setContentText(getString(R.string.service_notification_content))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(contentPendingIntent);
    return foregroundNotification.build();
  }
}
