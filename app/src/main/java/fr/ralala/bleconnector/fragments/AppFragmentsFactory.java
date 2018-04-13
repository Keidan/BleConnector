package fr.ralala.bleconnector.fragments;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import fr.ralala.bleconnector.R;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Manage the application fragments
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class AppFragmentsFactory {
  public static final int IDX_DEVICES = 0;
  public static final int IDX_SERVER = 1;
  private Fragment mDevicesFragment = null;
  private Fragment mServerFragment = null;
  private Fragment mCurrentFragment = null;
  private NavigationView mNavigationView = null;

  /**
   * Creates the fragments factory.
   * @param navigationView The application navigation view.
   */
  public AppFragmentsFactory(final NavigationView navigationView) {
    mNavigationView = navigationView;
    if(mDevicesFragment == null)
      mDevicesFragment = new DevicesFragment();
    if(mServerFragment == null)
      mServerFragment = new ServerFragment();
    mCurrentFragment = mDevicesFragment;
  }

  /**
   * Switch to inspect fragment (in devices fragment).
   */
  public void switchToScan(boolean close) {
    if(DevicesFragment.class.isInstance(mCurrentFragment))
      ((DevicesFragment)mCurrentFragment).switchToScan(close);
  }

  /**
   * Returns the default devices id.
   * @return int
   */
  public int getDefaultDevicesId() {
    return R.id.nav_devices;
  }

  /**
   * Returns the default devices index.
   * @return int
   */
  public int getDefaultDevicesIndex() {
    return IDX_DEVICES;
  }

  /**
   * Returns the default devices fragment.
   * @return Fragment
   */
  private Fragment getDefaultDevicesView() {
    return mDevicesFragment;
  }

  /**
   * Fix menu selection
   */
  public void fixMenuSelection() {
    Fragment fragment = mCurrentFragment;
    if(DevicesFragment.class.isInstance(fragment))
      mNavigationView.getMenu().getItem(IDX_DEVICES).setChecked(true);
    else if(ServerFragment.class.isInstance(fragment))
      mNavigationView.getMenu().getItem(IDX_SERVER).setChecked(true);
  }


  public void notifyServicesDiscovered() {
    if(DevicesFragment.class.isInstance(mCurrentFragment))
      ((DevicesFragment)mCurrentFragment).notifyServicesDiscovered();
  }

  /**
   * Return the current fragment.
   * @return Fragment.
   */
  public Fragment getCurrentFragment() {
    return mCurrentFragment;
  }

  /**
   * Changes the current fragment based on its index in the navigation view.
   * @param idx The index.
   */
  public void setCurrentToFragment(int idx) {
    switch (idx) {
      case IDX_DEVICES:
        mCurrentFragment = mDevicesFragment;
        break;
      case IDX_SERVER:
        mCurrentFragment = mServerFragment;
        break;
      default:
        mCurrentFragment = getDefaultDevicesView();
        break;
    }
  }
}
