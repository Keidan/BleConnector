package fr.ralala.bleconnector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ralala.bleconnector.callbacks.GattCallback;
import fr.ralala.bleconnector.fragments.AppFragmentsFactory;
import fr.ralala.bleconnector.utils.AppPermissions;
import fr.ralala.bleconnector.utils.UIHelper;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Main activity
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
  private static final int REQUEST_ENABLE_BT = 1;
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private boolean mViewIsAtHome = false;
  private NavigationView mNavigationView = null;
  private DrawerLayout mDrawer = null;
  private AppFragmentsFactory mFragments;
  private BleConnectorApplication mApp;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothLeScanner mBluetoothLeScanner;
  private AlertDialog mProgress;
  private BluetoothGatt mBluetoothGatt;
  private GattCallback mGattCallback;
  private List<BluetoothGattService> mListDataHeader = new ArrayList<>();
  private HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> mListDataChild = new HashMap<>();
  private List<ScanResult> mScanResults = new ArrayList<>();
  private BleStateChangedBroadcast mBleStateChangedBroadcast = new BleStateChangedBroadcast();

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mApp = BleConnectorApplication.getInstance();
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    mDrawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawer.addDrawerListener(toggle);
    toggle.syncState();
    mGattCallback = new GattCallback(this);
    mNavigationView = findViewById(R.id.nav_view);
    if(mNavigationView != null) {
      mNavigationView.setNavigationItemSelectedListener(this);
      mNavigationView.getMenu().getItem(0).setChecked(true);
    }
    mFragments = new AppFragmentsFactory(mNavigationView);
    displayView(mFragments.getDefaultDevicesId());
    mProgress = UIHelper.showCircularProgressDialog(this, (dialog) -> mFragments.switchToScan(true));

    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }
    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    registerReceiver(mBleStateChangedBroadcast, filter);
    if (!AppPermissions.checkPermissions(this)) {
      AppPermissions.shouldShowRequest(this);
    } else
      initialize();
  }

  /**
   * Returns the reference to the GattCallback.
   * @return GattCallback
   */
  public GattCallback getGattCallback() {
    return mGattCallback;
  }

  /**
   * Returns the List<BluetoothGattService>
   * @return List<BluetoothGattService>
   */
  public List<BluetoothGattService> getListDataHeader() {
    return mListDataHeader;
  }

  /**
   * Returns the <code>HashMap<BluetoothGattService, List<BluetoothGattCharacteristic></code>
   * @return <code>HashMap<BluetoothGattService, List<BluetoothGattCharacteristic></code>
   */
  public HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> getListDataChild() {
    return mListDataChild;
  }

  /**
   * Returns the List<ScanResult>
   * @return List<ScanResult>
   */
  public List<ScanResult> getScanResults() {
    return mScanResults;
  }

  /**
   * Returns the reference to the BluetoothGatt object.
   * @return BluetoothGatt
   */
  public BluetoothGatt getBluetoothGatt() {
    return mBluetoothGatt;
  }

  /**
   * Returns the reference to the BluetoothLeScanner object.
   * @return BluetoothLeScanner
   */
  public BluetoothLeScanner getBluetoothLeScanner() {
    if(mBluetoothLeScanner == null && mBluetoothAdapter != null)
      mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    return mBluetoothLeScanner;
  }

  /**
   * Connect the GATT part.
   * @param scanResult The scan result containing the remote device.
   */
  public void connectGATT(ScanResult scanResult) {
    closeGATT();
    mBluetoothGatt = scanResult.getDevice().connectGatt(this, false, mGattCallback);
    mGattCallback.setBluetoothGatt(mBluetoothGatt);
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
   // mFragments.onResume();
    if(mApp.isUseServer() && !UIHelper.isServiceRunning(mApp, GattServerService.class))
      startService(GattServerService.getIntent());
  }

  /**
   * Called when the activity is destroyed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    // Unregister broadcast listeners
    unregisterReceiver(mBleStateChangedBroadcast);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mViewIsAtHome)
      mDrawer.openDrawer(Gravity.START);
    else { //if the current view is not the News fragment
      displayView(mFragments.getDefaultDevicesId()); //display the home fragment
      mNavigationView.getMenu().getItem(mFragments.getDefaultDevicesIndex()).setChecked(true); /* select home title */
      return;
    }
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      if(UIHelper.isServiceRunning(mApp, GattServerService.class))
        stopService(GattServerService.getIntent());
      closeGATT();
      super.onBackPressed();
      return;
    } else {
      Toast.makeText(this, R.string.on_double_back_exit_text, Toast.LENGTH_SHORT).show();
    }
    mLastBackPressed = System.currentTimeMillis();
  }

  /**
   * Closes the GATT connection
   */
  public void closeGATT() {
    if(mBluetoothGatt != null && mProgress != null && mProgress.isShowing())
      mProgress.dismiss();
    if(mGattCallback != null)
      mGattCallback.abort();
    if(mBluetoothGatt != null) {
      mBluetoothGatt.close();
      mBluetoothGatt = null;
    }
  }

  /**
   * Called when an item is selected in the navigation view.
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    displayView(item.getItemId());
    return true;
  }

  /**
   * Displays a specific view.
   * @param viewId The view id to display.
   */
  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    mViewIsAtHome = mFragments.getDefaultDevicesId() == viewId;
    switch (viewId) {
      case R.id.nav_devices:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_DEVICES);
        title = getString(R.string.chart_scan);
        break;
      case R.id.nav_chart_scan:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_CHART_SCAN);
        title = getString(R.string.chart_scan);
        break;
      case R.id.nav_server:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_SERVER);
        title  = getString(R.string.server);
        break;
      default:
        mFragments.setCurrentToFragment(-1);
        break;

    }

    if (mFragments.getCurrentFragment() != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.replace(R.id.content_frame, mFragments.getCurrentFragment());
      ft.commit();
    }
    mFragments.fixMenuSelection();

    // set the toolbar title
    if (getSupportActionBar() != null) {
      setSubTitle(title);
    }

    if(mDrawer != null) mDrawer.closeDrawer(GravityCompat.START);
  }

  /**
   * Sets the application title.
   * @param title The new title, null for default title.
   */
  public void setSubTitle(String title) {
    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(getString(R.string.app_name));
      ab.setSubtitle(title == null ? getString(R.string.gatt_tab_scan) : title);
    }
  }


  /**
   * Notify service discovered event.
   */
  public void notifyServicesDiscovered() {
    mFragments.notifyServicesDiscovered();
  }


  /**
   * Show the progress dialog.
   */
  public void progressShow() {
    mProgress.show();
    Window window = mProgress.getWindow();
    if(window != null) {
      window.setLayout(350, 350);
      View v = window.getDecorView();
      v.setBackgroundResource(R.drawable.rounded_border);
    }
  }

  /**
   * Dismiss the progress dialog.
   */
  public void progressDismiss() {
    mProgress.dismiss();
  }



  /*-----------------------------------------*/
  /* INIT                                    */
  /*-----------------------------------------*/
  /**
   * Initialize the application.
   */
  private void initialize() {
    // Initializes Bluetooth adapter.
    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    if (bluetoothManager != null)
      mBluetoothAdapter = bluetoothManager.getAdapter();
    // Ensures Bluetooth is available on the device and it is enabled. If not,
    // displays a dialog requesting user permission to enable Bluetooth.
    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      UIHelper.snackInfo(this, getString(R.string.ble_enabled));
      mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }
  }

  /**
   * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned, and any additional data from it.
   * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
   * @param resultCode  The integer result code returned by the child activity through its setResult().
   * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
   */
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_ENABLE_BT) {
      if (mBluetoothAdapter.isEnabled()) {
        if (mBluetoothAdapter.isDiscovering()) {
          UIHelper.snackInfo(this, getString(R.string.ble_currently_in_discovery));
        } else {
          UIHelper.snackInfo(this, getString(R.string.ble_enabled));
        }
      } else {
        UIHelper.snackInfo(this, getString(R.string.ble_not_enabled));
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
  }

  /*-----------------------------------------*/
  /* PERMISSIONS                             */
  /*-----------------------------------------*/
  /**
   * Callback for the result from requesting permissions.
   *
   * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int).
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case AppPermissions.PERMISSIONS_REQUEST_BLUETOOTH: {
        if(AppPermissions.onRequestPermissionsResult(this, permissions, grantResults))
          initialize();
        break;
      }
    }
  }
  /*-----------------------------------------*/
  /* BLE STATE CHANGED                       */
  /*-----------------------------------------*/
  private class BleStateChangedBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // It means the user has changed his bluetooth state.
      if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        if (state == BluetoothAdapter.STATE_TURNING_OFF) {
          if(UIHelper.isServiceRunning(mApp, GattServerService.class))
            stopService(GattServerService.getIntent());
          closeGATT();
          initialize();
        }
      }
    }

  }
}
