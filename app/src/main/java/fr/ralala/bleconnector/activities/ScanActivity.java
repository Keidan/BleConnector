package fr.ralala.bleconnector.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.callbacks.LeScanCallback;
import fr.ralala.bleconnector.adapters.ScanListAdapter;
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
public class ScanActivity extends AppCompatActivity {
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothLeScanner mBluetoothLeScanner;
  private boolean mScanning;
  private Handler mHandler = new Handler();
  private MenuItem mItemScan;
  private ScanListAdapter mScanListAdapter;
  private LeScanCallback mLeScanCallback;
  // Stops scanning after 30 seconds.
  private static final long SCAN_PERIOD = 30000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    List<ScanResult> results = new ArrayList<>();
    mScanListAdapter = new ScanListAdapter(this, results);
    mLeScanCallback = new LeScanCallback(this, mScanListAdapter);
    ListView listDevices = findViewById(R.id.listDevices);
    listDevices.setAdapter(mScanListAdapter);
    listDevices.setOnItemClickListener((parent, view, position, id) -> {
      ScanResult sr = mScanListAdapter.getItem(position);
      if(sr != null) {
        Intent intent = new Intent(this, GattActivity.class);
        intent.putExtra(GattActivity.EXTRA, sr);
        startActivity(intent);
        stopScan();
      }
    });
    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }
    if (!AppPermissions.checkPermissions(this)) {
      AppPermissions.shouldShowRequest(this);
    } else
      initialize();
  }

  @Override
  public void onDestroy() {
    stopScan();
    super.onDestroy();
  }

  /**
   * Called to create the option menu.
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.acitivy_scan, menu);
    mItemScan = menu.findItem(R.id.action_scan);
    return true;
  }

  /**
   * Called when the user select an option menu item.
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_scan:
        if(item.getTitle().equals(getString(R.string.stop_scan))) {
          stopScan();
        } else {
          startScan();
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Starts the BLE scan.
   */
  private void startScan() {
    if(!mScanning) {
      mScanListAdapter.clear();
      // Stops scanning after a pre-defined scan period.
      Runnable run = this::stopScan;
      mHandler.removeCallbacks(run);
      mHandler.postDelayed(run, SCAN_PERIOD);
      if(mBluetoothLeScanner == null)
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
      mBluetoothLeScanner.startScan(mLeScanCallback);
      mScanning = true;
      mItemScan.setTitle(R.string.stop_scan);
    }
  }

  /**
   * Stops the BLE scan.
   */
  public void stopScan() {
    if(mScanning) {
      mScanning = false;
      mBluetoothLeScanner.stopScan(mLeScanCallback);
      mItemScan.setTitle(R.string.start_scan);
    }
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


}
