package fr.ralala.bleconnector.fragments.tabs;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.MainActivity;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.adapters.TabFragmentWriteListAdapter;
import fr.ralala.bleconnector.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Write fragment used in TabLayout view.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TabFragmentWrite extends GenericTabFragment {
  private MainActivity mActivity;
  private TabFragmentWriteListAdapter mGattWriteListAdapter;
  private ListView mListWrite;

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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_tab_read, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_tab_write, container, false);
    mListWrite = root.findViewById(R.id.listWrite);
    mListWrite.setOnItemClickListener((adapterView, view, i, l) -> {
      if(mGattWriteListAdapter == null)
        return;
      TabFragmentWriteListAdapter.Item item = mGattWriteListAdapter.getItem(i);
      if(item == null)
        return;
      int props = item.characteristic.getProperties();
      if((props & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
          (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ||
          (props & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {

        createTextDialog(mActivity, getString(R.string.characteristic_tilte), R.layout.content_dialog_characteristic, (dialog, editText, spFormat) -> {
          String text = editText.getText().toString();
          if(!isValid(text, editText))
            return;
          Item spItem = (Item)spFormat.getSelectedItem();
          if(spItem != null) {
            int n = 0;
            if(spItem.fmt != Integer.MIN_VALUE) {
              text = text.replaceAll("0x", "");
              if(!isValid(text, editText))
                return;
              if(spItem.fmt != Integer.MAX_VALUE)
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
                  item.characteristic.setValue(bytes);
                } catch (Exception e) {
                  Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
                  UIHelper.showAlertDialog(mActivity, R.string.error, getString(R.string.invalid_value), null);
                }
                break;
              }
              case BluetoothGattCharacteristic.FORMAT_UINT8:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              case BluetoothGattCharacteristic.FORMAT_SINT8:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              case BluetoothGattCharacteristic.FORMAT_UINT16:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              case BluetoothGattCharacteristic.FORMAT_SINT16:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              case BluetoothGattCharacteristic.FORMAT_UINT32:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              case BluetoothGattCharacteristic.FORMAT_SINT32:
                item.characteristic.setValue(n, spItem.fmt, 0);
                break;
              default:
                item.characteristic.setValue(text);
                break;
            }
          } else
            item.characteristic.setValue(text);
          InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
          if(imm != null)
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
          mActivity.progressShow();
          if(!mActivity.getGattCallback().writeCharacteristic(item)) {
            mActivity.progressDismiss();
            UIHelper.snackInfo(mActivity, getString(R.string.error_write_characteristic));
          }
          dialog.dismiss();
        });
      }
    });
    return root;
  }


  @Override
  public void onResume() {
    super.onResume();
    notifyServicesDiscovered();
  }


  /**
   * Requests for clear UI.
   */
  @Override
  public void requestClearUI() {
    if(mActivity != null) {
      mGattWriteListAdapter = new TabFragmentWriteListAdapter(mActivity, new ArrayList<>());
      mListWrite.setAdapter(mGattWriteListAdapter);
    }
  }


  /**
   * Returns true if the fragment is locked and a switch can't be processed.
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
    if(mActivity == null || mActivity.getBluetoothGatt() == null)
      return;
    List<TabFragmentWriteListAdapter.Item> list = new ArrayList<>();
    List<BluetoothGattService> services = mActivity.getBluetoothGatt().getServices();
    for(BluetoothGattService service : services) {
      String srvUUID = service.getUuid().toString();
      String srvName = BleConnectorApplication.getInstance().getGattHelper().lookup(srvUUID, null, true);
      List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
      for(BluetoothGattCharacteristic characteristic : characteristics) {
        int props = characteristic.getProperties();
        if((props & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
            (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ||
            (props & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {
          TabFragmentWriteListAdapter.Item item = new TabFragmentWriteListAdapter.Item();
          item.srvName = srvName;
          item.srvUUID = srvUUID;
          item.characteristic = characteristic;
          list.add(item);
        }
      }
    }

    mGattWriteListAdapter = new TabFragmentWriteListAdapter(mActivity, list);
    mListWrite.setAdapter(mGattWriteListAdapter);
    mActivity.getGattCallback().setGattWriteListAdapter(mGattWriteListAdapter);
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
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> { });
    LayoutInflater factory = LayoutInflater.from(c);
    final ViewGroup nullParent = null;
    builder.setView(factory.inflate(contentId, nullParent));
    final AlertDialog dialog = builder.create();
    dialog.show();
    TextInputEditText et = dialog.findViewById(R.id.editText);
    TextInputLayout layout = dialog.findViewById(R.id.editTextLayout);
    AppCompatSpinner spFormat = dialog.findViewById(R.id.spFormat);
    if(spFormat != null) {
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
          Item item = (Item)spFormat.getSelectedItem();
          if(layout != null && item != null) {
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
    if(et != null) {
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
