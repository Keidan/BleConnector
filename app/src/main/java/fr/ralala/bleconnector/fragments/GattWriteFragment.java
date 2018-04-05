package fr.ralala.bleconnector.fragments;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.activities.GattActivity;
import fr.ralala.bleconnector.adapters.GattWriteListAdapter;
import fr.ralala.bleconnector.utils.UIHelper;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT read fragment
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattWriteFragment extends GattGenericFragment {
  private GattActivity mActivity;
  private ListView mListWrite;
  private GattWriteListAdapter mGattWriteListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mActivity = (GattActivity)getActivity();
    assert mActivity != null;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_gatt_write, container, false);
    mListWrite = root.findViewById(R.id.listWrite);
    mListWrite.setOnItemClickListener((adapterView, view, i, l) -> {
      if(mGattWriteListAdapter == null)
        return;
      GattWriteListAdapter.Item item = mGattWriteListAdapter.getItem(i);
      if(item == null)
        return;
      int props = item.characteristic.getProperties();
      if((props & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
          (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ||
          (props & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {

        createTextDialog(mActivity, getString(R.string.characteristic_tilte), R.layout.content_dialog_characteristic, (dialog, content) -> {
          final String text = content.getText().toString();
          if (text.trim().isEmpty()) {
            UIHelper.shakeError(content, getString(R.string.invalid_value));
            return ;
          }
          item.characteristic.setValue(text);
          InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
          if(imm != null)
            imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
          mActivity.progressShow();
          if(!mActivity.getGattCallback().writeCharacteristic(item)) {
            mActivity.progressDismiss();
            UIHelper.snackInfo(mActivity, getString(R.string.error_write_characteristic));
          }
          dialog.dismiss();
        });
      }
    });
    notifyServicesDiscovered();
    return root;
  }

  /**
   * Called when the services are discovered.
   */
  public void notifyServicesDiscovered() {
    if(mActivity == null)
      return;
    List<GattWriteListAdapter.Item> list = new ArrayList<>();
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
          GattWriteListAdapter.Item item = new GattWriteListAdapter.Item();
          item.srvName = srvName;
          item.srvUUID = srvUUID;
          item.characteristic = characteristic;
          list.add(item);
        }
      }
    }

    mGattWriteListAdapter = new GattWriteListAdapter(mActivity, list);
    mListWrite.setAdapter(mGattWriteListAdapter);
    mActivity.getGattCallback().setGattWriteListAdapter(mGattWriteListAdapter);
  }


  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, TextInputEditText editText);
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
    if(et != null) {
      et.setText("");
      et.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          positiveClick.onClick(dialog, et);
          return true;
        }
        return false;
      });
    }
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, et));
  }
}
