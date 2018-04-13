package fr.ralala.bleconnector.fragments.tabs;

import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentScanListAdapter;
import fr.ralala.bleconnector.callbacks.LeScanCallback;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Scan fragment used in TabLayout view.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TabFragmentScan extends GenericTabFragment {
  private boolean mScanning;
  private Handler mHandler = new Handler();
  private TabFragmentScanListAdapter mScanListAdapter;
  private LeScanCallback mLeScanCallback;
  // Stops scanning after 30 seconds.
  private static final long SCAN_PERIOD = 30000;
  private MenuItem mItem;


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_tab_scan, container, false);
    mScanListAdapter = new TabFragmentScanListAdapter(mActivity, mActivity.getScanResults());
    mLeScanCallback = new LeScanCallback(mActivity, mScanListAdapter);
    ListView listDevices = root.findViewById(R.id.listDevices);
    listDevices.setAdapter(mScanListAdapter);
    listDevices.setOnItemClickListener((parent, view, position, id) -> {
      ScanResult sr = mScanListAdapter.getItem(position);
      if(sr != null) {
        stopScan(mItem);
        mActivity.progressShow();
        mActivity.connectGATT(sr);
        mDevicesFragment.switchToDetails();
      }
    });
    return root;
  }

  @Override
  public void onResume() {
    super.onResume();
    /* Force refresh */
    if(mScanListAdapter != null)
      mScanListAdapter.notifyDataSetChanged();
  }

  /**
   * Called when the services are discovered.
   */
  @Override
  public void notifyServicesDiscovered() {

  }

  /**
   * Returns true if the fragment is locked and a switch can't be processed.
   * @return boolean
   */
  @Override
  public boolean isLocked() {
    if(mScanning) {
      Toast.makeText(mActivity, R.string.scan_running, Toast.LENGTH_SHORT).show();
      return true;
    }
    return false;
  }

  /**
   * Called when a menu is clicked.
   * @param mi The menu.
   * @return true if consumed.
   */
  public boolean onMenuClicked(MenuItem mi) {
    switch (mi.getItemId()) {
      case R.id.action_scan:
        mItem = mi;
        if(mScanning) {
          stopScan(mi);
        } else {
          mActivity.closeGATT();
          mScanListAdapter.clear();
          // Stops scanning after a pre-defined scan period.
          Runnable run = () -> stopScan(mi);
          mHandler.removeCallbacks(run);
          mHandler.postDelayed(run, SCAN_PERIOD);
          mActivity.getBluetoothLeScanner().startScan(mLeScanCallback);
          mScanning = true;
          mi.setTitle(R.string.stop_scan);
          mDevicesFragment.requestClear();
        }
        return true;
    }
    return false;
  }

  /**
   * Requests for clear UI.
   */
  @Override
  public void requestClearUI()  {
    if(mScanListAdapter != null) {
      mScanListAdapter.clear();
      mScanListAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Stops the BLE scan.
   */
  public void stopScan(MenuItem mi) {
    if(mScanning) {
      mScanning = false;
      mActivity.getBluetoothLeScanner().stopScan(mLeScanCallback);
      mi.setTitle(R.string.start_scan);
    }
  }
}
