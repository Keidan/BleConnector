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
  public static final int IDX_HOMME = 0;
  public static final int IDX_SERVER = 1;
  private Fragment mHomeFragment = null;
  private Fragment mServerFragment = null;
  private Fragment mCurrentFragment = null;
  private NavigationView mNavigationView = null;

  /**
   * Creates the fragments factory.
   * @param navigationView The application navigation view.
   */
  public AppFragmentsFactory(final NavigationView navigationView) {
    mNavigationView = navigationView;
    if(mHomeFragment == null)
      mHomeFragment = new HomeFragment();
    if(mServerFragment == null)
      mServerFragment = new ServerFragment();
    mCurrentFragment = mHomeFragment;
  }

  /**
   * Switch to inspect fragment (in home fragment).
   */
  public void switchToScan() {
    if(HomeFragment.class.isInstance(mCurrentFragment))
      ((HomeFragment)mCurrentFragment).switchToScan();
  }

  /**
   * Returns the default home id.
   * @return int
   */
  public int getDefaultHomeId() {
    return R.id.nav_home;
  }

  /**
   * Returns the default home index.
   * @return int
   */
  public int getDefaultHomeIndex() {
    return IDX_HOMME;
  }

  /**
   * Returns the default home fragment.
   * @return Fragment
   */
  private Fragment getDefaultHomeView() {
    return mHomeFragment;
  }

  /**
   * Fix menu selection
   */
  public void fixMenuSelection() {
    Fragment fragment = mCurrentFragment;
    if(HomeFragment.class.isInstance(fragment))
      mNavigationView.getMenu().getItem(IDX_HOMME).setChecked(true);
    else if(ServerFragment.class.isInstance(fragment))
      mNavigationView.getMenu().getItem(IDX_SERVER).setChecked(true);
  }


  public void notifyServicesDiscovered() {
    if(HomeFragment.class.isInstance(mCurrentFragment))
      ((HomeFragment)mCurrentFragment).notifyServicesDiscovered();
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
      case IDX_HOMME:
        mCurrentFragment = mHomeFragment;
        break;
      case IDX_SERVER:
        mCurrentFragment = mServerFragment;
        break;
      default:
        mCurrentFragment = getDefaultHomeView();
        break;
    }
  }
}
