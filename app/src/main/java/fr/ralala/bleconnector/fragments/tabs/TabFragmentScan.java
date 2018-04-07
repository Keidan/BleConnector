package fr.ralala.bleconnector.fragments.tabs;

import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import fr.ralala.bleconnector.MainActivity;
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
  private MenuItem mItemScan;
  private TabFragmentScanListAdapter mScanListAdapter;
  private LeScanCallback mLeScanCallback;
  // Stops scanning after 30 seconds.
  private static final long SCAN_PERIOD = 30000;
  private MainActivity mActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_tab_scan, menu);
    mItemScan = menu.findItem(R.id.action_scan);
    mItemScan.setTitle(getString(mScanning ? R.string.stop_scan : R.string.start_scan));
    super.onCreateOptionsMenu(menu, inflater);
  }

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
        stopScan();
        mActivity.progressShow();
        mActivity.connectGATT(sr);
        mHomeFragment.switchToInspect();
      }
    });
    return root;
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_scan:
        if(mScanning) {
          stopScan();
        } else {
          mActivity.closeGATT();
          startScan();
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
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
   * Starts the BLE scan.
   */
  private void startScan() {
    if(!mScanning) {
      mScanListAdapter.clear();
      // Stops scanning after a pre-defined scan period.
      Runnable run = this::stopScan;
      mHandler.removeCallbacks(run);
      mHandler.postDelayed(run, SCAN_PERIOD);
      mActivity.getBluetoothLeScanner().startScan(mLeScanCallback);
      mScanning = true;
      mItemScan.setTitle(R.string.stop_scan);
      mHomeFragment.requestClear();
    }
  }

  /**
   * Stops the BLE scan.
   */
  public void stopScan() {
    if(mScanning) {
      mScanning = false;
      mActivity.getBluetoothLeScanner().stopScan(mLeScanCallback);
      mItemScan.setTitle(R.string.start_scan);
    }
  }
}
