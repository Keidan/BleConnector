package fr.ralala.bleconnector.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.DevicesViewPagerAdapter;
import fr.ralala.bleconnector.fragments.tabs.GenericTabFragment;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentDetails;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentScan;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Devices fragment
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DevicesFragment extends Fragment implements ViewPager.OnPageChangeListener {
  private static final int SCAN_PAGE_INDEX = 0;
  private static final int DETAILS_PAGE_INDEX = 1;
  private MainActivity mActivity;
  private int oldPage = SCAN_PAGE_INDEX;
  private ViewPager mViewPager;
  private MenuItem mItemDisconnect;
  private MenuItem mItemScan;
  private MenuItem mItemReadAll;
  private MenuItem mItemExpandCollapse;
  private DevicesViewPagerAdapter mDevicesViewPagerAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
    View root = inflater.inflate(R.layout.fragment_devices, container, false);

    mViewPager = root.findViewById(R.id.viewpager);
    mDevicesViewPagerAdapter = new DevicesViewPagerAdapter(this, getChildFragmentManager());

    mDevicesViewPagerAdapter.addFragment(new TabFragmentScan(), getString(R.string.gatt_tab_scan));
    mDevicesViewPagerAdapter.addFragment(new TabFragmentDetails(), getString(R.string.gatt_tab_details));
    mViewPager.setAdapter(mDevicesViewPagerAdapter);
    /*if(mActivity.getBluetoothGatt() != null)
      mDevicesViewPagerAdapter.addFragment(new TabFragmentDetails(), getString(R.string.gatt_tab_details));*/

    TabLayout tabLayout = root.findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(mViewPager);
    mViewPager.addOnPageChangeListener(this);
    return root;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_home, menu);
    mItemDisconnect = menu.findItem(R.id.action_disconnect);
    mItemDisconnect.setVisible(mActivity.getBluetoothGatt() != null);
    mItemScan = menu.findItem(R.id.action_scan);
    mItemReadAll = menu.findItem(R.id.action_read_all);
    mItemExpandCollapse = menu.findItem(R.id.expand_collapse_all);
    mItemExpandCollapse.setTitle(getString(R.string.expand_all));
    updateMenuVisibility(mViewPager.getCurrentItem());
    super.onCreateOptionsMenu(menu, inflater);
  }

  public MenuItem getMenuItemExpandCollapseAll() {
    return mItemExpandCollapse;
  }

  /**
   * Switch to scan fragment (in home fragment).
   */
  public void switchToScan(boolean close) {
    oldPage = SCAN_PAGE_INDEX;
    mViewPager.setCurrentItem(SCAN_PAGE_INDEX);
    //mDevicesViewPagerAdapter.removeFragment(DETAILS_PAGE_INDEX);
    if(close)
      mActivity.closeGATT();
  }

  /**
   * Switch to details fragment (in home fragment).
   */
  public void switchToDetails() {
    oldPage = SCAN_PAGE_INDEX;
    //mDevicesViewPagerAdapter.removeFragment(DETAILS_PAGE_INDEX);
    //mDevicesViewPagerAdapter.addFragment(new TabFragmentDetails(), getString(R.string.gatt_tab_details));
    mViewPager.setCurrentItem(DETAILS_PAGE_INDEX);
  }

  /**
   * Aborts the current operation.
   */
  public void abortProcess() {
    for(GenericTabFragment fragment : mDevicesViewPagerAdapter.getFragments())
      fragment.abortProcess();
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {
    for(GenericTabFragment fragment : mDevicesViewPagerAdapter.getFragments())
      fragment.notifyServicesDiscovered();
  }

  /**
   * Requests for clear.
   */
  public void requestClear() {
    for(GenericTabFragment fragment : mDevicesViewPagerAdapter.getFragments())
      try {
        fragment.requestClearUI();
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mDevicesViewPagerAdapter.getItem(mViewPager.getCurrentItem()).onMenuClicked(item) || super.onOptionsItemSelected(item);
  }

  private void updateMenuVisibility(int position) {
    switch (position) {
      case SCAN_PAGE_INDEX: {
        mItemDisconnect.setVisible(false);
        mItemScan.setVisible(true);
        mItemReadAll.setVisible(false);
        mItemExpandCollapse.setVisible(false);
        mActivity.setSubTitle(null);
        break;
      }
      case DETAILS_PAGE_INDEX: {
        BluetoothGatt gatt = mActivity.getBluetoothGatt();
        boolean visible = gatt != null;
        if(visible) {
          BluetoothDevice bd = gatt.getDevice();
          String name = bd.getName();
          if(name != null && !name.isEmpty())
            mActivity.setSubTitle(name);
          else
            mActivity.setSubTitle(gatt.getDevice().getAddress());
        } else
          mActivity.setSubTitle(null);
        mItemDisconnect.setVisible(visible);
        mItemScan.setVisible(false);
        mItemExpandCollapse.setVisible(visible);
        mItemReadAll.setVisible(visible);
        break;
      }
      default: {
        mItemDisconnect.setVisible(false);
        mItemScan.setVisible(false);
        mItemReadAll.setVisible(false);
        mItemExpandCollapse.setVisible(false);
        mActivity.setSubTitle(null);
        break;
      }
    }
  }

  @Override
  public void onPageSelected(int position) {
    updateMenuVisibility(position);
    if(position != oldPage) {
      if(mDevicesViewPagerAdapter.getItem(oldPage).isLocked()) {
        mViewPager.setCurrentItem(oldPage);
        return;
      }
      oldPage = position;
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override
  public void onPageScrollStateChanged(int state) {
  }

}
