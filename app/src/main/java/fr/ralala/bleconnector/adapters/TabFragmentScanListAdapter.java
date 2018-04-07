package fr.ralala.bleconnector.adapters;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.ralala.bleconnector.R;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Adapter used with the scan list view.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class TabFragmentScanListAdapter extends ArrayAdapter<ScanResult> {
  private final static int ID = R.layout.itme_list_scan;
  private final Context mContext;
  private final List<ScanResult> mItems;

  private class ViewHolder {
    TextView tvName;
    TextView tvAddress;
    TextView tvRssi;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param objects The objects list.
   */
  public TabFragmentScanListAdapter(final Context context, final List<ScanResult> objects) {
    super(context, ID, objects);
    mContext = context;
    mItems = objects;
  }

  /**
   * Returns an items at a specific position.
   * @param i The item index.
   * @return The item.
   */
  @Override
  public ScanResult getItem(final int i) {
    return mItems.get(i);
  }

  /**
   * Returns the position of the specified item in the array.
   * @param item The item to retrieve the position of.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(ScanResult item) {
    return super.getPosition(item);
  }

  /**
   * How many items are in the data set represented by this Adapter.
   * @return Count of items.
   */
  @Override
  public int getCount() {
    return mItems.size();
  }

  /**
   * Get the row id associated with the specified position in the list.
   * @param position The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  /**
   * Adds the specified ScanResult at the end of the array or replace the entry if found.
   * @param sr The ScanResult to add or replace.
   */
  public void add(ScanResult sr) {
    for(int i = 0; i < mItems.size(); i++)
      if(mItems.get(i).getDevice().getAddress().equals(sr.getDevice().getAddress())) {
        mItems.set(i, sr);
        return;
      }
    mItems.add(sr);
  }

  /**
   * Returns the current view.
   * @param position The view position.
   * @param convertView The view to convert.
   * @param parent The parent.
   * @return The new view.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert inflater != null;
      v = inflater.inflate(ID, null);
      holder = new ViewHolder();
      holder.tvName = v.findViewById(R.id.tvName);
      holder.tvAddress = v.findViewById(R.id.tvAddress);
      holder.tvRssi = v.findViewById(R.id.tvRssi);
      v.setTag(holder);
    } else {
      holder = (ViewHolder)v.getTag();
    }
    final ScanResult o = getItem(position);
    if (o != null) {
      String name = o.getDevice().getName();
      if(name == null || name.isEmpty())
        holder.tvName.setText(R.string.unknown);
      else
        holder.tvName.setText(o.getDevice().getName());
      holder.tvAddress.setText(o.getDevice().getAddress());
      holder.tvRssi.setText(String.valueOf(o.getRssi()));
    }
    return v;
  }
}
