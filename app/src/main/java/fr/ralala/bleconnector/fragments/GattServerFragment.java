package fr.ralala.bleconnector.fragments;

import android.bluetooth.BluetoothGattService;
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
  private Switch mSwCurrentTime;
  private GattActivity mActivity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mActivity = (GattActivity)getActivity();
    assert mActivity != null;
    View root = inflater.inflate(R.layout.fragment_gatt_server, container, false);
    mSwCurrentTime = root.findViewById(R.id.swCurrentTime);
    mSwCurrentTime.setOnClickListener(this);
    return root;
  }

  @Override
  public void onResume() {
    super.onResume();
    for(BluetoothGattService srv : mActivity.getBluetoothGattServer().getServices())
      if(srv.getUuid().toString().equals(GattServerCallback.CTS_SERVICE_UUID.toString())) {
        mSwCurrentTime.setChecked(true);
      }

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
      case R.id.swCurrentTime:
        if ( mSwCurrentTime.isChecked()) {
          mActivity.getBluetoothGattServer().addService(GattServerCallback.CTS_GATT_SERVICE);
        } else {
          mActivity.getBluetoothGattServer().removeService(GattServerCallback.CTS_GATT_SERVICE);
        }
        break;
    }
  }
}
