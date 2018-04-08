package fr.ralala.bleconnector.fragments.tabs;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentReadListAdapter;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Read fragment used in TabLayout view.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TabFragmentRead extends GenericTabFragment {
  private MainActivity mActivity;
  private List<TabFragmentReadListAdapter.Item> mList;
  private ListView mListRead;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_tab_read, container, false);
    mListRead = root.findViewById(R.id.listRead);
    return root;
  }

  @Override
  public void onResume() {
    super.onResume();
    notifyServicesDiscovered();
  }


  /**
   * Requests for clear UI.
   */
  @Override
  public void requestClearUI()  {
    if(mActivity != null) {
      mList = new ArrayList<>();
      TabFragmentReadListAdapter gattReadListAdapter = new TabFragmentReadListAdapter(mActivity, mList);
      mListRead.setAdapter(gattReadListAdapter);
    }
  }

  /**
   * Returns true if the fragment is locked and a switch can't be processed.
   * @return boolean
   */
  @Override
  public boolean isLocked() {
    return false;
  }

  /**
   * Called when the services are discovered.
   */
  @Override
  public void notifyServicesDiscovered() {
    if(mActivity == null || mActivity.getBluetoothGatt() == null)
      return;
    mList = new ArrayList<>();
    List<BluetoothGattService> services = mActivity.getBluetoothGatt().getServices();
    for(BluetoothGattService service : services) {
      String srvUUID = service.getUuid().toString();
      String srvName = BleConnectorApplication.getInstance().getGattHelper().lookup(srvUUID, null, true);
      List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
      for(BluetoothGattCharacteristic characteristic : characteristics) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
          TabFragmentReadListAdapter.Item item = new TabFragmentReadListAdapter.Item();
          item.srvName = srvName;
          item.srvUUID = srvUUID;
          item.characteristic = characteristic;
          mList.add(item);
        }
      }
    }

    TabFragmentReadListAdapter gattReadListAdapter = new TabFragmentReadListAdapter(mActivity, mList);
    mListRead.setAdapter(gattReadListAdapter);
    mActivity.getGattCallback().setGattReadListAdapter(gattReadListAdapter);
  }

  /**
   * Called when a menu is clicked.
   * @param mi The menu.
   * @return true if consumed.
   */
  public boolean onMenuClicked(MenuItem mi) {
    switch (mi.getItemId()) {
      case R.id.action_read_all:
        if(mActivity.getBluetoothGatt() == null) {
          Toast.makeText(mActivity, R.string.scan_and_connect_before, Toast.LENGTH_SHORT).show();
          mHomeFragment.switchToScan();
          return true;
        }
        if(!mList.isEmpty()) {
          mActivity.progressShow();
          mActivity.getGattCallback().readCharacteristics(mList);
          return true;
        }
        break;
    }
    return false;
  }
}
