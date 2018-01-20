package fr.ralala.bleconnector.fragments;

import android.support.v4.app.Fragment;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT generic fragment
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public abstract class GattGenericFragment extends Fragment {

  /**
   * Called when the services are discovered.
   */
  public abstract void notifyServicesDiscovered();

}
