package fr.ralala.bleconnector.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.utils.gatt.GattHelper;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Adapter used with the GATT read list view.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattReadListAdapter extends ArrayAdapter<GattReadListAdapter.Item> {
  private final static int ID = R.layout.gatt_read_item;
  private final Context mContext;
  private final List<GattReadListAdapter.Item> mItems;

  private class ViewHolder {
    TextView tvNameService;
    TextView tvName;
    TextView tvUUID;
    TextView tvData;
  }

  public static class Item {
    public String srvName;
    public String srvUUID;
    public BluetoothGattCharacteristic characteristic;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param objects The objects list.
   */
  public GattReadListAdapter(final Context context, final List<GattReadListAdapter.Item> objects) {
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
  public GattReadListAdapter.Item getItem(final int i) {
    return mItems.get(i);
  }

  /**
   * Returns the position of the specified item in the array.
   * @param item The item to retrieve the position of.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(GattReadListAdapter.Item item) {
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
      holder.tvNameService = v.findViewById(R.id.tvNameService);
      holder.tvName = v.findViewById(R.id.tvName);
      holder.tvUUID = v.findViewById(R.id.tvUUID);
      holder.tvData = v.findViewById(R.id.tvData);
      v.setTag(holder);
    } else {
      holder = (ViewHolder)v.getTag();
    }
    final GattReadListAdapter.Item o = getItem(position);
    if (o != null) {
      String uuid = o.characteristic.getUuid().toString();
      holder.tvNameService.setText(o.srvName == null ? o.srvUUID : o.srvName);
      holder.tvName.setText(BleConnectorApplication.getInstance().getGattHelper().lookup(uuid, mContext.getString(R.string.unknown_characteristic)));
      holder.tvUUID.setText(GattHelper.fixUUID(uuid));
      byte [] bytes = o.characteristic.getValue();
      holder.tvData.setText(bytes == null ? "" : BleConnectorApplication.getInstance().getGattHelper().convert(uuid, bytes));
    }
    return v;
  }
}
