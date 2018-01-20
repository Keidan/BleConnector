package fr.ralala.bleconnector.fragments;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.activities.GattActivity;
import fr.ralala.bleconnector.adapters.GattReadListAdapter;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT read fragment
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattReadFragment extends GattGenericFragment {
  private GattActivity mActivity;
  private List<GattReadListAdapter.Item> mList;
  private ListView mListRead;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mActivity = (GattActivity)getActivity();
    assert mActivity != null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_gatt_read, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {



    View root = inflater.inflate(R.layout.fragment_gatt_read, container, false);
    mListRead = root.findViewById(R.id.listRead);
    notifyServicesDiscovered();
    return root;
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {
    if(mActivity == null)
      return;
    mList = new ArrayList<>();
    List<BluetoothGattService> services = mActivity.getBluetoothGatt().getServices();
    for(BluetoothGattService service : services) {
      String srvUUID = service.getUuid().toString();
      String srvName = BleConnectorApplication.getInstance().getGattHelper().lookup(srvUUID, null);
      List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
      for(BluetoothGattCharacteristic characteristic : characteristics) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
          GattReadListAdapter.Item item = new GattReadListAdapter.Item();
          item.srvName = srvName;
          item.srvUUID = srvUUID;
          item.characteristic = characteristic;
          mList.add(item);
        }
      }
    }

    GattReadListAdapter gattReadListAdapter = new GattReadListAdapter(mActivity, mList);
    mListRead.setAdapter(gattReadListAdapter);
    mActivity.getGattCallback().setGattReadListAdapter(gattReadListAdapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_read_all:
        if(!mList.isEmpty()) {
          mActivity.progressShow();
          mActivity.getGattCallback().readCharacteristic(mList);
        }
        break;
    }
    return true;

  }
}
