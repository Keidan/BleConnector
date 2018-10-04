package fr.ralala.bleconnector.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.GattServerService;
import fr.ralala.bleconnector.R;

/**
 * ******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Server fragment
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ServerFragment extends Fragment implements View.OnClickListener {
  private Switch mSwStartServer;
  private Switch mSwCurrentTime;
  private Switch mSwBattery;


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_server, container, false);
    mSwStartServer = root.findViewById(R.id.swStartServer);
    mSwCurrentTime = root.findViewById(R.id.swCurrentTime);
    mSwBattery = root.findViewById(R.id.swBattery);
    mSwStartServer.setOnClickListener(this);
    mSwCurrentTime.setOnClickListener(this);
    mSwBattery.setOnClickListener(this);
    return root;
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    BleConnectorApplication app = BleConnectorApplication.getInstance();
    mSwStartServer.setChecked(app.isUseServer());
    boolean checked = mSwStartServer.isChecked();
    mSwCurrentTime.setEnabled(!checked);
    mSwBattery.setEnabled(!checked);
    mSwCurrentTime.setChecked(app.isUseCurrentTimeService());
    mSwBattery.setChecked(app.isUseBatteryService());
  }

  /**
   * Called when switch is toggled - starts or stops advertising.
   */
  @Override
  public void onClick(View v) {
    BleConnectorApplication app = BleConnectorApplication.getInstance();
    switch (v.getId()) {
      case R.id.swStartServer:
        boolean checked = mSwStartServer.isChecked();
        mSwCurrentTime.setEnabled(!checked);
        mSwBattery.setEnabled(!checked);
        if (checked)
          app.startService(GattServerService.getIntent());
        else
          app.stopService(GattServerService.getIntent());
        app.setUseServer(checked);
        break;
      case R.id.swCurrentTime:
        app.setUseCurrentTimeService(mSwCurrentTime.isChecked());
        break;
      case R.id.swBattery:
        app.setUseBatteryService(mSwBattery.isChecked());
        break;
    }
  }
}