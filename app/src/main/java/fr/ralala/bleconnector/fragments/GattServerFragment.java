package fr.ralala.bleconnector.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.activities.GattActivity;
import fr.ralala.bleconnector.callbacks.GattServerCallback;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT Server fragment
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattServerFragment extends GattGenericFragment implements View.OnClickListener {
  private Switch mSwStartServer;
  private Switch mSwCurrentTime;
  private Switch mSwBattery;
  private GattActivity mActivity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mActivity = (GattActivity)getActivity();
    assert mActivity != null;
    View root = inflater.inflate(R.layout.fragment_gatt_server, container, false);
    mSwStartServer = root.findViewById(R.id.swStartServer);
    mSwCurrentTime = root.findViewById(R.id.swCurrentTime);
    mSwBattery = root.findViewById(R.id.swBattery);
    mSwStartServer.setOnClickListener(this);
    mSwCurrentTime.setOnClickListener(this);
    mSwBattery.setOnClickListener(this);
    return root;
  }

  @Override
  public void onResume() {
    super.onResume();
    mSwStartServer.setChecked(mActivity.isServerStarted());
    boolean checked = mSwStartServer.isChecked();
    mSwCurrentTime.setEnabled(!checked);
    mSwBattery.setEnabled(!checked);
    mSwCurrentTime.setChecked(mActivity.getGattServerCallback().getService(GattServerCallback.CURRENT_TIME_SERVICE).isRegistered());
    mSwBattery.setChecked(mActivity.getGattServerCallback().getService(GattServerCallback.BATTERY_SERVICE).isRegistered());
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {

  }

  /**
   * Called when switch is toggled - starts or stops advertising.
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.swStartServer:
        boolean checked = mSwStartServer.isChecked();
        mSwCurrentTime.setEnabled(!checked);
        mSwBattery.setEnabled(!checked);
        if(checked)
          mActivity.startServer();
        else
          mActivity.stopServer();
      case R.id.swCurrentTime:
        if ( mSwCurrentTime.isChecked()) {
          mActivity.getGattServerCallback().getService(GattServerCallback.CURRENT_TIME_SERVICE).registerService(mActivity);
        } else {
          mActivity.getGattServerCallback().getService(GattServerCallback.CURRENT_TIME_SERVICE).unregisterService(mActivity);
        }
        break;
      case R.id.swBattery:
        if ( mSwBattery.isChecked()) {
          mActivity.getGattServerCallback().getService(GattServerCallback.BATTERY_SERVICE).registerService(mActivity);
        } else {
          mActivity.getGattServerCallback().getService(GattServerCallback.BATTERY_SERVICE).unregisterService(mActivity);
        }
        break;
    }
  }
}
