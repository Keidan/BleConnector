package fr.ralala.bleconnector.fragments.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.fragments.DevicesFragment;

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
  protected DevicesFragment mDevicesFragment;
  protected MainActivity mActivity;

  public void setActivity(MainActivity activity) {
    mActivity = activity;
  }

  /**
   * Called when a menu is clicked.
   * @param mi The menu.
   * @return true if consumed.
   */
  public abstract boolean onMenuClicked(MenuItem mi);

  /**
   * Sets the reference to the devices fragment.
   * @param devicesFragment DevicesFragment
   */
  public void setDevicesFragment(DevicesFragment devicesFragment) {
    mDevicesFragment = devicesFragment;
  }

  /**
   * Returns true if the fragment is locked and a switch can't be processed.
   * @return boolean
   */
  public abstract boolean isLocked();

  /**
   * Aborts the current operation.
   */
  public abstract void abortProcess();

  /**
   * Called when the services are discovered.
   */
  public abstract void notifyServicesDiscovered();

  /**
   * Requests for clear UI.
   */
  public abstract void requestClearUI();
}
