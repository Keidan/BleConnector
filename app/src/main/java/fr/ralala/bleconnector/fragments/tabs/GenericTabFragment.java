package fr.ralala.bleconnector.fragments.tabs;

import android.support.v4.app.Fragment;

import fr.ralala.bleconnector.fragments.HomeFragment;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Generic fragment for the tab component.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class GenericTabFragment extends Fragment {
  protected HomeFragment mHomeFragment;

  /**
   * Sets the reference to the home fragment.
   * @param homeFragment HomeFragment
   */
  public void setHomeFragment(HomeFragment homeFragment) {
    mHomeFragment = homeFragment;
  }

  /**
   * Returns true if the fragment is locked and a switch can't be processed.
   * @return boolean
   */
  public abstract boolean isLocked();


  /**
   * Called when the services are discovered.
   */
  public abstract void notifyServicesDiscovered();

  /**
   * Requests for clear UI.
   */
  public abstract void requestClearUI();
}
