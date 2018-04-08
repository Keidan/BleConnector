package fr.ralala.bleconnector.fragments.tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentInspectListAdapter;
import fr.ralala.bleconnector.callbacks.GattCallback;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Inspect fragment used in TabLayout view.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TabFragmentInspect extends GenericTabFragment {
  private MainActivity mActivity;
  private TabFragmentInspectListAdapter mTabFragmentInspectListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_tab_inspect, container, false);
    mTabFragmentInspectListAdapter = new TabFragmentInspectListAdapter(mActivity, mActivity.getListDataHeader(), mActivity.getListDataChild());
    ExpandableListView expandableListView = root.findViewById(R.id.listServices);
    expandableListView.setAdapter(mTabFragmentInspectListAdapter);
    GattCallback gattCallback = mActivity.getGattCallback();
    gattCallback.setGattInspectListAdapter(mTabFragmentInspectListAdapter);
    return root;
  }


  /**
   * Requests for clear UI.
   */
  @Override
  public void requestClearUI()  {
    if(mTabFragmentInspectListAdapter != null) {
      mTabFragmentInspectListAdapter.clear();
      mTabFragmentInspectListAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Called when a menu is clicked.
   * @param mi The menu.
   * @return true if consumed.
   */
  public boolean onMenuClicked(MenuItem mi) {
    switch (mi.getItemId()) {
      case R.id.action_disconnect:
        mActivity.closeGATT();
        mi.setVisible(false);
        mHomeFragment.requestClear();
        mHomeFragment.switchToScan();
        return true;
    }
    return false;
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

  }
}
