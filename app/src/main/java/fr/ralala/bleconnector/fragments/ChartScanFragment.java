package fr.ralala.bleconnector.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Chart scan fragment
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ChartScanFragment extends Fragment {
  private static final int MAX_DELAY_IN_SECONDS = 40;
  private boolean mScanning;
  private MainActivity mActivity;
  private LeScanCallback mLeScanCallback = new LeScanCallback();
  private Map<String, LineGraphSeries<DataPoint>> mSeries = new HashMap<>();
  private GraphView mGraphView;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mActivity = (MainActivity) getActivity();
    assert mActivity != null;
    View rootView = inflater.inflate(R.layout.fragment_chart_scan, container, false);
    mGraphView = rootView.findViewById(R.id.graph);
    mGraphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
      @Override
      public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
          return DateFormat.format("mm:ss", new Date((long) value)).toString();
        } else {
          return super.formatLabel(value, false);
        }
      }
    });
    mGraphView.getGridLabelRenderer().setNumHorizontalLabels(5);
    mGraphView.getViewport().setXAxisBoundsManual(true);
    return rootView;
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    mActivity.getScanResults().clear();
    mActivity.closeGATT();
    BluetoothLeScanner scanner = mActivity.getBluetoothLeScanner();
    if (scanner == null)
      UIHelper.snackInfo(mActivity, getString(R.string.ble_not_enabled));
    else {
      mActivity.getBluetoothLeScanner().startScan(mLeScanCallback);
      mScanning = true;
    }
  }

  /**
   * Called when the fragment is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    if (mScanning) {
      mScanning = false;
      mActivity.getBluetoothLeScanner().stopScan(mLeScanCallback);
      mGraphView.removeAllSeries();
    }
  }

  /**
   * Adds the specified ScanResult at the end of the array or replace the entry if found.
   *
   * @param sr The ScanResult to add or replace.
   */
  public synchronized void addScanResult(ScanResult sr, boolean lost) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getDefault());
    BluetoothDevice bd = sr.getDevice();
    /* adds series */
    LineGraphSeries<DataPoint> series = mSeries.get(bd.getAddress());
    if (series == null) {
      if (mSeries.isEmpty()) {
        mGraphView.getViewport().setMinX(calendar.getTimeInMillis());
        calendar.add(Calendar.SECOND, MAX_DELAY_IN_SECONDS);
        mGraphView.getViewport().setMaxX(calendar.getTimeInMillis());
      }
      series = new LineGraphSeries<>();
      String name = bd.getName();
      if (name != null && !name.isEmpty())
        series.setTitle(name);
      else
        series.setTitle(bd.getAddress());
      series.setColor(UIHelper.generateRandomColor());
      mGraphView.addSeries(series);
      mSeries.put(bd.getAddress(), series);
      mGraphView.getLegendRenderer().setVisible(true);
      mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }
    /* fill main list */
    List<ScanResult> results = mActivity.getScanResults();
    boolean found = false;
    for (int i = 0; i < results.size(); i++)
      if (results.get(i).getDevice().getAddress().equals(sr.getDevice().getAddress())) {
        if (lost)
          results.remove(i);
        else
          results.set(i, sr);
        found = true;
        break;
      }
    if (!found)
      results.add(sr);
    try {
      /* removes useless series */
      Map<String, LineGraphSeries<DataPoint>> dump = mSeries;
      calendar.setTime(new Date());
      for (Map.Entry<String, LineGraphSeries<DataPoint>> entry : mSeries.entrySet()) {
        String key = entry.getKey();
        LineGraphSeries<DataPoint> value = entry.getValue();
        found = false;
        for (ScanResult res : results)
          if (res.getDevice().getAddress().equals(key)) {
            value.appendData(new DataPoint(calendar.getTimeInMillis() + 1,
                res.getRssi()), true, MAX_DELAY_IN_SECONDS * 20);
            found = true;
            break;
          }
        if (!found) {
          dump.remove(key);
          mGraphView.removeSeries(value);
          Log.i(getClass().getSimpleName(), "Removes series: " + key);
        }
      }
      mSeries = dump;
    } catch (ConcurrentModificationException cme) {
      Log.e(getClass().getSimpleName(), "ConcurrentModificationException: " + cme, cme);
    }
  }

  private class LeScanCallback extends ScanCallback {

    /**
     * Callback when a BLE advertisement has been found.
     *
     * @param callbackType Determines how this callback was triggered. Could be one of
     *                     {@link android.bluetooth.le.ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
     *                     {@link android.bluetooth.le.ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
     *                     {@link android.bluetooth.le.ScanSettings#CALLBACK_TYPE_MATCH_LOST}
     * @param result       A Bluetooth LE scan result.
     */
    public void onScanResult(int callbackType, ScanResult result) {
      mActivity.runOnUiThread(() -> addScanResult(result, callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST));
    }

    /**
     * Callback when batch results are delivered.
     *
     * @param results List of scan results that are previously scanned.
     */
    public void onBatchScanResults(List<ScanResult> results) {
      mActivity.getScanResults().clear();
      mActivity.runOnUiThread(() -> {
        for (ScanResult sr : results)
          addScanResult(sr, false);
      });
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
}