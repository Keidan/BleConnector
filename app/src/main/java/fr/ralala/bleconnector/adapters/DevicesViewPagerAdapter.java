package fr.ralala.bleconnector.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.fragments.DevicesFragment;
import fr.ralala.bleconnector.fragments.tabs.GenericTabFragment;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Adapter used with the devices view pager.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class DevicesViewPagerAdapter extends FragmentPagerAdapter {

  private final List<GenericTabFragment> mFragmentList = new ArrayList<>();
  private final List<String> mFragmentTitleList = new ArrayList<>();
  private final DevicesFragment mDevices;

  public DevicesViewPagerAdapter(DevicesFragment devices, FragmentManager manager) {
    super(manager);
    mDevices = devices;
  }

  /**
   * Returns all fragments.
   * @return List<GenericTabFragment>
   */
  public List<GenericTabFragment> getFragments() {
    return mFragmentList;
  }
  /**
   * Returns all titles.
   * @return List<String>
   */
  public List<String> getTitles() {
    return mFragmentTitleList;
  }

  /**
   * Return the Fragment associated with a specified position.
   * @param position The specified position.
   * @return GenericTabFragment
   */
  @Override
  public GenericTabFragment getItem(int position) {
    return mFragmentList.get(position);
  }

  /**
   * Return the number of views available.
   * @return int
   */
  @Override
  public int getCount() {
    return mFragmentList.size();
  }

  /**
   * Adds a fragment.
   * @param fragment The fragment to add.
   * @param title The fragment title.
   */
  public void addFragment(GenericTabFragment fragment, String title) {
    mFragmentList.add(fragment);
    mFragmentTitleList.add(title);
    fragment.setDevicesFragment(mDevices);
    fragment.setActivity((MainActivity)mDevices.getActivity());
    notifyDataSetChanged();
  }

  /**
   * Removes a specified fragment.
   * @param index The fragment index.
   */
  public void removeFragment(int index) {
    if(getCount() > index) {
      mFragmentList.remove(index);
      mFragmentTitleList.remove(index);
      notifyDataSetChanged();
    }
  }

  /**
   * This method may be called by the ViewPager to obtain a title string
   * to describe the specified page.
   *
   * @param position The position of the title requested
   * @return A title for the requested page
   */
  @Override
  public CharSequence getPageTitle(int position) {
    return mFragmentTitleList.get(position);
  }
}
