package fr.ralala.bleconnector.callbacks;


import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import java.util.List;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentScanListAdapter;
import fr.ralala.bleconnector.utils.UIHelper;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * LE scan callback.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class LeScanCallback extends ScanCallback {
  private Activity mActivity;
  private TabFragmentScanListAdapter mScanListAdapter;

  public LeScanCallback(Activity activity, TabFragmentScanListAdapter scanListAdapter) {
    mActivity = activity;
    mScanListAdapter = scanListAdapter;
  }

  /**
   * Callback when a BLE advertisement has been found.
   *
   * @param callbackType Determines how this callback was triggered. Could be one of
   *                     {@link ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
   *                     {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
   *                     {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
   * @param result       A Bluetooth LE scan result.
   */
  public void onScanResult(int callbackType, ScanResult result) {
    mScanListAdapter.add(result);
    mActivity.runOnUiThread(() -> mScanListAdapter.notifyDataSetChanged());
  }

  /**
   * Callback when batch results are delivered.
   *
   * @param results List of scan results that are previously scanned.
   */
  public void onBatchScanResults(List<ScanResult> results) {
    mScanListAdapter.clear();
    for (ScanResult sr : results)
      mScanListAdapter.add(sr);
    mActivity.runOnUiThread(() -> mScanListAdapter.notifyDataSetChanged());
  }

  /**
   * Callback when scan could not be started.
   *
   * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
   */
  public void onScanFailed(int errorCode) {
    switch (errorCode) {
      case SCAN_FAILED_ALREADY_STARTED:
        UIHelper.snackInfo(mActivity,
            mActivity.getString(R.string.ble_scan_failed_already_started));
        break;
      case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
        UIHelper.snackInfo(mActivity,
            mActivity.getString(R.string.ble_scan_failed_application_reg_failed));
        break;
      case SCAN_FAILED_FEATURE_UNSUPPORTED:
        UIHelper.snackInfo(mActivity,
            mActivity.getString(R.string.ble_scan_failed_feature_unsupported));
        break;
      default:
        UIHelper.snackInfo(mActivity,
            mActivity.getString(R.string.ble_scan_failed_internal_error));
        break;
    }
  }
}