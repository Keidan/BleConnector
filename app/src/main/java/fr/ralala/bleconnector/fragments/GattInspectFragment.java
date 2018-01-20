package fr.ralala.bleconnector.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.activities.GattActivity;
import fr.ralala.bleconnector.adapters.GattInspectListAdapter;
import fr.ralala.bleconnector.callbacks.GattCallback;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT inspector fragment
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattInspectFragment extends GattGenericFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    GattActivity activity = (GattActivity)getActivity();
    assert activity != null;
    View root = inflater.inflate(R.layout.fragment_gatt_inspect, container, false);
    GattInspectListAdapter gattListAdapter = new GattInspectListAdapter(activity, activity.getListDataHeader(), activity.getListDataChild());
    ExpandableListView listServices = root.findViewById(R.id.listServices);
    listServices.setAdapter(gattListAdapter);
    GattCallback gattCallback = activity.getGattCallback();
    gattCallback.setGattInspectListAdapter(gattListAdapter);
    return root;
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {

  }


}
