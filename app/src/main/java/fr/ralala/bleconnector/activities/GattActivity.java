package fr.ralala.bleconnector.activities;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;
import fr.ralala.bleconnector.callbacks.GattCallback;
import fr.ralala.bleconnector.callbacks.GattServerCallback;
import fr.ralala.bleconnector.callbacks.LeAdvertiseCallback;
import fr.ralala.bleconnector.fragments.GattServerFragment;
import fr.ralala.bleconnector.fragments.GattGenericFragment;
import fr.ralala.bleconnector.fragments.GattInspectFragment;
import fr.ralala.bleconnector.fragments.GattReadFragment;
import fr.ralala.bleconnector.fragments.GattWriteFragment;
import fr.ralala.bleconnector.utils.UIHelper;
import fr.ralala.bleconnector.utils.gatt.cts.ServerCTS;


/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT activity
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattActivity extends AppCompatActivity {
  public static final String EXTRA = "ScanResult";
  private BluetoothGatt mBluetoothGatt;
  private AlertDialog mProgress;
  private GattCallback mGattCallback;
  private List<BluetoothGattService> mListDataHeader = new ArrayList<>();
  private HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> mListDataChild = new HashMap<>();
  private List<GattGenericFragment> mFragments = new ArrayList<>();
  private BluetoothGattServer mBluetoothGattServer = null;
  private GattServerCallback mGattServerCallback;
  private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
  private LeAdvertiseCallback mLeAdvertiseCallback;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gatt);
    mProgress = UIHelper.showCircularProgressDialog(this, (dialog) -> onBackPressed());
    progressShow();
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() != null)
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ViewPager viewPager = findViewById(R.id.viewpager);
    setupViewPager(viewPager);
    TabLayout tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);


    BluetoothManager manager = (BluetoothManager) getSystemService(GattActivity.BLUETOOTH_SERVICE);
    if(manager != null) {
      mBluetoothLeAdvertiser = manager.getAdapter().getBluetoothLeAdvertiser();
      if(mBluetoothLeAdvertiser != null) {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build();

        /* Adds all services */
        AdvertiseData data = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(new ParcelUuid(ServerCTS.TIME_SERVICE))
            .build();
        mLeAdvertiseCallback = new LeAdvertiseCallback();
        mBluetoothLeAdvertiser
            .startAdvertising(settings, data, mLeAdvertiseCallback);
      }
      mGattServerCallback = new GattServerCallback();
      mBluetoothGattServer = manager.openGattServer(this, mGattServerCallback);
      if (mBluetoothGattServer == null) {
        Log.e(getClass().getSimpleName(), "Unable to start GATT server");
      } else {
        mGattServerCallback.setGattServer(mBluetoothGattServer);
        if(BleConnectorApplication.getInstance().useCurrentTimeService())
          mGattServerCallback.getServerCTS().registerService(this);
      }
    } else
      Log.e(getClass().getSimpleName(), "Null BluetoothManager object");

    ScanResult scanResult = getIntent().getParcelableExtra(EXTRA);
    mGattCallback = new GattCallback(this);
    mBluetoothGatt = scanResult.getDevice().connectGatt(this, false, mGattCallback);
    mGattCallback.setBluetoothGatt(mBluetoothGatt);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if(mGattServerCallback != null)
      mGattServerCallback.getServerCTS().registerBroadcast(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if(mGattServerCallback != null)
      mGattServerCallback.getServerCTS().unregisterBroadcast(this);
  }


  public List<BluetoothGattService> getListDataHeader() {
    return mListDataHeader;
  }
  public HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> getListDataChild() {
    return mListDataChild;
  }
  public BluetoothGatt getBluetoothGatt() {
    return mBluetoothGatt;
  }

  private void setupViewPager(ViewPager viewPager) {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
    addToViewPager(adapter, new GattInspectFragment(), getString(R.string.gatt_tab_inspect));
    addToViewPager(adapter, new GattReadFragment(), getString(R.string.gatt_tab_read));
    addToViewPager(adapter, new GattWriteFragment(), getString(R.string.gatt_tab_write));
    addToViewPager(adapter, new GattServerFragment(), getString(R.string.gatt_tab_server));
    viewPager.setAdapter(adapter);
  }

  private void addToViewPager(ViewPagerAdapter adapter, GattGenericFragment fragment, String title) {
    adapter.addFragment(fragment, title);
    mFragments.add(fragment);
  }

  private void close() {
    if (mBluetoothGatt == null) {
      return;
    }
    mBluetoothGatt.close();
    mBluetoothGatt = null;
    if (mBluetoothGattServer != null) {
      mBluetoothGattServer.close();
      mBluetoothGattServer = null;
    }
    if(mBluetoothLeAdvertiser != null)
      mBluetoothLeAdvertiser.stopAdvertising(mLeAdvertiseCallback);
  }



  public GattCallback getGattCallback() {
    return mGattCallback;
  }
  public GattServerCallback getGattServerCallback() {
    return mGattServerCallback;
  }

  public void progressShow() {
    mProgress.show();
    Window window = mProgress.getWindow();
    if(window != null) {
      window.setLayout(350, 350);
      View v = window.getDecorView();
      v.setBackgroundResource(R.drawable.rounded_border);
    }
  }

  public void progressDismiss() {
    mProgress.dismiss();
  }

  public void notifyServicesDiscovered() {
    for(GattGenericFragment fragment : mFragments)
      fragment.notifyServicesDiscovered();
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if(mGattCallback != null)
    mGattCallback.abort();
    close();
    if(mProgress != null && mProgress.isShowing())
      mProgress.dismiss();
    super.onBackPressed();
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return false;
  }

  class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mFragmentList.size();
    }

    void addFragment(Fragment fragment, String title) {
      mFragmentList.add(fragment);
      mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitleList.get(position);
    }
  }
}
