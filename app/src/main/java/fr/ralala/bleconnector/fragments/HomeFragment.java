package fr.ralala.bleconnector.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.fragments.tabs.GenericTabFragment;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentInspect;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentRead;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentScan;
import fr.ralala.bleconnector.fragments.tabs.TabFragmentWrite;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Home fragment
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class HomeFragment extends Fragment implements ViewPager.OnPageChangeListener {
  private static final int SCAN_PAGE_INDEX = 0;
  private static final int INSPECT_PAGE_INDEX = 1;
  private static final int READALL_PAGE_INDEX = 2;
  private MainActivity mActivity;
  private List<GenericTabFragment> mFragments = new ArrayList<>();
  private int oldPage = SCAN_PAGE_INDEX;
  private ViewPager mViewPager;
  private MenuItem mItemDisconnect;
  private MenuItem mItemScan;
  private MenuItem mItemReadAll;

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
    View root = inflater.inflate(R.layout.fragment_home, container, false);

    mViewPager = root.findViewById(R.id.viewpager);
    setupViewPager(mViewPager);
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
    updateMenuVisibility(mViewPager.getCurrentItem());
    super.onCreateOptionsMenu(menu, inflater);
  }

  private void setupViewPager(ViewPager viewPager) {
    ViewPagerAdapter adapter = new ViewPagerAdapter(mActivity.getSupportFragmentManager());
    addToViewPager(adapter, new TabFragmentScan(), getString(R.string.gatt_tab_scan));
    addToViewPager(adapter, new TabFragmentInspect(), getString(R.string.gatt_tab_inspect));
    addToViewPager(adapter, new TabFragmentRead(), getString(R.string.gatt_tab_read));
    addToViewPager(adapter, new TabFragmentWrite(), getString(R.string.gatt_tab_write));
    viewPager.setAdapter(adapter);
  }

  /**
   * Switch to scan fragment (in home fragment).
   */
  public void switchToScan() {
    mViewPager.setCurrentItem(SCAN_PAGE_INDEX);
  }

  /**
   * Switch to inspect fragment (in home fragment).
   */
  public void switchToInspect() {
    mViewPager.setCurrentItem(INSPECT_PAGE_INDEX);
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {
    for(GenericTabFragment fragment : mFragments)
      fragment.notifyServicesDiscovered();
  }

  /**
   * Requests for clear.
   */
  public void requestClear() {
    for(GenericTabFragment fragment : mFragments)
      try {
        fragment.requestClearUI();
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
  }

  private void addToViewPager(ViewPagerAdapter adapter, GenericTabFragment fragment, String title) {
    adapter.addFragment(fragment, title);
    fragment.setHomeFragment(this);
    mFragments.add(fragment);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(!mFragments.get(mViewPager.getCurrentItem()).onMenuClicked(item))
      return super.onOptionsItemSelected(item);
    return true;

  }

  private void updateMenuVisibility(int position) {
    switch (position) {
      case SCAN_PAGE_INDEX:
        mItemDisconnect.setVisible(false);
        mItemScan.setVisible(true);
        mItemReadAll.setVisible(false);
        break;
      case INSPECT_PAGE_INDEX:
        mItemDisconnect.setVisible(mActivity.getBluetoothGatt() != null);
        mItemScan.setVisible(false);
        mItemReadAll.setVisible(false);
        break;
      case READALL_PAGE_INDEX:
        mItemDisconnect.setVisible(false);
        mItemScan.setVisible(false);
        mItemReadAll.setVisible(true);
        break;
      default:
        mItemDisconnect.setVisible(false);
        mItemScan.setVisible(false);
        mItemReadAll.setVisible(false);
        break;
    }
  }

  @Override
  public void onPageSelected(int position) {
    updateMenuVisibility(position);
    if(position != oldPage) {
      if(mFragments.get(oldPage).isLocked()) {
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

  private class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mFragmentList.size();
    }

    void addFragment(Fragment fragment, String title) {
      mFragmentList.add(fragment);
      mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitleList.get(position);
    }
  }
}
