package fr.ralala.bleconnector.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.utils.gatt.GattHelper;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Adapter used with the services list view.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattInspectListAdapter extends BaseExpandableListAdapter {
  private final Context mContext;
  private List<BluetoothGattService> mListDataHeader; // header titles
  // child data in format of header title, child title
  private HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> mListDataChild;
  private LayoutInflater mInflater;
  private final ViewGroup mNullParent = null;

  private class ViewHolderHeader {
    TextView tvName;
    TextView tvUUID;
  }

  private class ViewHolderChild {
    TextView tvName;
    TextView tvUUID;
    TextView tvProperties;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   */
  public GattInspectListAdapter(final Context context, List<BluetoothGattService> listDataHeader,
                                HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> listDataChild) {
    mContext = context;
    mListDataChild = listDataChild;
    mListDataHeader = listDataHeader;
    mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public void clear() {
    mListDataHeader.clear();
    mListDataChild.clear();
  }

  public void add(BluetoothGattService service) {
    mListDataHeader.add(service);
    mListDataChild.put(service, service.getCharacteristics());
  }

  @Override
  public Object getChild(int groupPosition, int childPosititon) {
    return mListDataChild.get(mListDataHeader.get(groupPosition))
        .get(childPosititon);
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  @Override
  public View getChildView(int groupPosition, final int childPosition,
                           boolean isLastChild, View convertView, ViewGroup parent) {

    final BluetoothGattCharacteristic child = (BluetoothGattCharacteristic)getChild(groupPosition, childPosition);
    ViewHolderChild holder;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.gatt_item_content, mNullParent);
      holder = new ViewHolderChild();
      holder.tvName = convertView.findViewById(R.id.tvName);
      holder.tvUUID = convertView.findViewById(R.id.tvUUID);
      holder.tvProperties = convertView.findViewById(R.id.tvProperties);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolderChild) convertView.getTag();
    }
    String uuid = child.getUuid().toString();

    holder.tvName.setText(BleConnectorApplication.getInstance().getGattHelper().lookup(uuid, mContext.getString(R.string.unknown_characteristic), false));
    holder.tvUUID.setText(GattHelper.fixUUID(uuid));
    String properties;
    int prop = child.getProperties();
    if(child.getProperties() != 0) {
      properties = "";
      if((prop & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0)
        properties += mContext.getString(R.string.property_broadcast) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
        properties += mContext.getString(R.string.property_read) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
        properties += mContext.getString(R.string.property_write_no_response) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)
        properties += mContext.getString(R.string.property_write) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)
        properties += mContext.getString(R.string.property_notify) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0)
        properties += mContext.getString(R.string.property_indicate) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0)
        properties += mContext.getString(R.string.property_signed_write) + ",";
      if((prop & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0)
        properties += mContext.getString(R.string.property_extended_props) + ",";
      if(properties.endsWith(","))
        properties = properties.substring(0, properties.length() - 1);
    } else
      properties = mContext.getString(R.string.unknown);
    holder.tvProperties.setText(properties);
    return convertView;
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return mListDataChild.get(mListDataHeader.get(groupPosition))
        .size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return mListDataHeader.get(groupPosition);
  }

  @Override
  public int getGroupCount() {
    return mListDataHeader.size();
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    BluetoothGattService header = (BluetoothGattService) getGroup(groupPosition);
    ViewHolderHeader holder;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.gatt_item_header, mNullParent);
      holder = new ViewHolderHeader();
      holder.tvName = convertView.findViewById(R.id.tvName);
      holder.tvUUID = convertView.findViewById(R.id.tvUUID);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolderHeader)convertView.getTag();
    }

    String uuid = header.getUuid().toString();
    holder.tvName.setText(BleConnectorApplication.getInstance().getGattHelper().lookup(uuid, mContext.getString(R.string.unknown_service), true));
    holder.tvUUID.setText(GattHelper.fixUUID(uuid));
    return convertView;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
