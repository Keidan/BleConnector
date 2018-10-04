package fr.ralala.bleconnector.fragments.tabs;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentDetailsListAdapter;
import fr.ralala.bleconnector.callbacks.GattCallback;
import fr.ralala.bleconnector.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Inspect fragment used in TabLayout view.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class TabFragmentDetails extends GenericTabFragment implements TabFragmentDetailsListAdapter.OnImageClick {
  private TabFragmentDetailsListAdapter mTabFragmentDetailsListAdapter;

  private class Item {
    int fmt;
    String name;

    public Item(String name, int fmt) {
      this.name = name;
      this.fmt = fmt;
    }

    public String toString() {
      return name;
    }
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_tab_details, container, false);
    ExpandableListView expandableListView = root.findViewById(R.id.listServices);
    mTabFragmentDetailsListAdapter = new TabFragmentDetailsListAdapter(mActivity, expandableListView, mActivity.getListDataHeader(), mActivity.getListDataChild(), this);
    expandableListView.setAdapter(mTabFragmentDetailsListAdapter);
    GattCallback gattCallback = mActivity.getGattCallback();
    gattCallback.setGattDetailsListAdapter(mTabFragmentDetailsListAdapter);
    return root;
  }


  /**
   * Requests for clear UI.
   */
  @Override
  public void requestClearUI() {
    if (mTabFragmentDetailsListAdapter != null) {
      mTabFragmentDetailsListAdapter.clear();
      mTabFragmentDetailsListAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Aborts the current operation.
   */
  @Override
  public void abortProcess() {
  }

  /**
   * Called when a menu is clicked.
   *
   * @param mi The menu.
   * @return true if consumed.
   */
  public boolean onMenuClicked(MenuItem mi) {
    switch (mi.getItemId()) {
      case R.id.expand_collapse_all: {
        MenuItem itemExpandCollapse = mDevicesFragment.getMenuItemExpandCollapseAll();
        if (itemExpandCollapse != null) {
          if (itemExpandCollapse.getTitle().equals(getString(R.string.expand_all))) {
            itemExpandCollapse.setTitle(getString(R.string.collapse_all));
            mTabFragmentDetailsListAdapter.expandAll();
          } else {
            itemExpandCollapse.setTitle(getString(R.string.expand_all));
            mTabFragmentDetailsListAdapter.collapseAll();
          }
        }
        return true;
      }
      case R.id.action_disconnect: {
        mi.setVisible(false);
        MenuItem itemExpandCollapse = mDevicesFragment.getMenuItemExpandCollapseAll();
        if (itemExpandCollapse != null)
          itemExpandCollapse.setTitle(getString(R.string.expand_all));
        mDevicesFragment.requestClear();
        mDevicesFragment.switchToScan(true);
        return true;
      }
      case R.id.action_read_all: {
        if (mActivity.getBluetoothGatt() == null) {
          Toast.makeText(mActivity, R.string.scan_and_connect_before, Toast.LENGTH_SHORT).show();
          mDevicesFragment.switchToScan(false);
          return true;
        }
        if (mTabFragmentDetailsListAdapter.getGroupCount() != 0) {
          mActivity.progressShow();
          List<BluetoothGattCharacteristic> list = new ArrayList<>();
          for (int i = 0; i < mTabFragmentDetailsListAdapter.getGroupCount(); i++)
            for (int j = 0; j < mTabFragmentDetailsListAdapter.getChildrenCount(i); j++) {
              BluetoothGattCharacteristic bc = (BluetoothGattCharacteristic) mTabFragmentDetailsListAdapter.getChild(i, j);
              if ((bc.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
                list.add(bc);
            }
          mActivity.getGattCallback().readCharacteristics(list);
          return true;
        }
        break;
      }
    }
    return false;
  }

  /**
   * Returns true if the fragment is locked and a switch can't be processed.
   *
   * @return boolean
   */
  @Override
  public boolean isLocked() {
    return false;
  }

  /**
   * Called when the services are discovered.
   */
  @Override
  public void notifyServicesDiscovered() {

  }

  /**
   * Called when the user clicks on an image.
   *
   * @param download      True if the download image is clicked, false for the upload image.
   * @param groupPosition The position of the group that the child resides in.
   * @param childPosition The position of the child with respect to other children in the group.
   */
  @Override
  public void onImageClick(boolean download, int groupPosition, int childPosition) {
    BluetoothGattCharacteristic item = (BluetoothGattCharacteristic) mTabFragmentDetailsListAdapter.getChild(groupPosition, childPosition);
    if (item == null) return;
    if (download) {
      mActivity.progressShow();
      mActivity.getGattCallback().readCharacteristics(Collections.singletonList(item));
    } else {
      createTextDialog(mActivity, getString(R.string.characteristic_tilte), R.layout.content_dialog_characteristic, (dialog, editText, spFormat) -> {
        String text = editText.getText().toString();
        if (!isValid(text, editText))
          return;
        Item spItem = (Item) spFormat.getSelectedItem();
        if (spItem != null) {
          int n = 0;
          if (spItem.fmt != Integer.MIN_VALUE) {
            text = text.replaceAll("0x", "");
            if (!isValid(text, editText))
              return;
            if (spItem.fmt != Integer.MAX_VALUE)
              n = Integer.parseInt(text, 16);
          }
          switch (spItem.fmt) {
            case Integer.MAX_VALUE: {
              try {
                String split[] = text.split(" ");
                byte bytes[] = new byte[split.length];
                for (int j = 0; j < bytes.length; j++) {
                  bytes[j] = Byte.parseByte(text, 16);
                }
                item.setValue(bytes);
              } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
                UIHelper.showAlertDialog(mActivity, R.string.error, getString(R.string.invalid_value), null);
              }
              break;
            }
            case BluetoothGattCharacteristic.FORMAT_UINT8:
              item.setValue(n, spItem.fmt, 0);
              break;
            case BluetoothGattCharacteristic.FORMAT_SINT8:
              item.setValue(n, spItem.fmt, 0);
              break;
            case BluetoothGattCharacteristic.FORMAT_UINT16:
              item.setValue(n, spItem.fmt, 0);
              break;
            case BluetoothGattCharacteristic.FORMAT_SINT16:
              item.setValue(n, spItem.fmt, 0);
              break;
            case BluetoothGattCharacteristic.FORMAT_UINT32:
              item.setValue(n, spItem.fmt, 0);
              break;
            case BluetoothGattCharacteristic.FORMAT_SINT32:
              item.setValue(n, spItem.fmt, 0);
              break;
            default:
              item.setValue(text);
              break;
          }
        } else
          item.setValue(text);
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
          imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        mActivity.progressShow();
        if (!mActivity.getGattCallback().writeCharacteristic(item)) {
          mActivity.progressDismiss();
          UIHelper.snackInfo(mActivity, getString(R.string.error_write_characteristic));
        }
        dialog.dismiss();
      });
    }
  }

  private boolean isValid(String text, EditText et) {
    if (text.trim().isEmpty()) {
      UIHelper.shakeError(et, getString(R.string.invalid_value));
      return false;
    }
    return true;
  }

  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, TextInputEditText editText, Spinner spFormat);
  }

  private void createTextDialog(Context c, String title, int contentId, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(c);
    final ViewGroup nullParent = null;
    builder.setView(factory.inflate(contentId, nullParent));
    final AlertDialog dialog = builder.create();
    dialog.show();
    TextInputEditText et = dialog.findViewById(R.id.editText);
    TextInputLayout layout = dialog.findViewById(R.id.editTextLayout);
    AppCompatSpinner spFormat = dialog.findViewById(R.id.spFormat);
    if (spFormat != null) {
      List<Item> list = new ArrayList<>();
      list.add(new Item("string", Integer.MIN_VALUE));
      list.add(new Item("uint8[]", Integer.MAX_VALUE));
      list.add(new Item("uint8", BluetoothGattCharacteristic.FORMAT_UINT8));
      list.add(new Item("sint8", BluetoothGattCharacteristic.FORMAT_SINT8));
      list.add(new Item("uint16", BluetoothGattCharacteristic.FORMAT_UINT16));
      list.add(new Item("sint16", BluetoothGattCharacteristic.FORMAT_SINT16));
      list.add(new Item("uint32", BluetoothGattCharacteristic.FORMAT_UINT32));
      list.add(new Item("sint32", BluetoothGattCharacteristic.FORMAT_SINT32));
      ArrayAdapter<Item> dataAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, list);
      dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spFormat.setAdapter(dataAdapter);
      spFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          Item item = (Item) spFormat.getSelectedItem();
          if (layout != null && item != null) {
            switch (item.fmt) {
              case Integer.MIN_VALUE:
                layout.setHint(getString(R.string.value_hint));
                break;
              case Integer.MAX_VALUE:
                layout.setHint(getString(R.string.value_hint_hex_array));
                break;
              default:
                layout.setHint(getString(R.string.value_hint_hex));
                break;
            }
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
      });
    }
    if (et != null) {
      et.setText("");
      et.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          positiveClick.onClick(dialog, et, spFormat);
          return true;
        }
        return false;
      });
    }
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, et, spFormat));
  }
}
