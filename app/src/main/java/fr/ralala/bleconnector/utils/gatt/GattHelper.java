package fr.ralala.bleconnector.utils.gatt;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import fr.ralala.bleconnector.BleConnectorApplication;
import fr.ralala.bleconnector.R;

import static fr.ralala.bleconnector.utils.gatt.Helper.toU8;
import static fr.ralala.bleconnector.utils.gatt.Helper.toU16;
import static fr.ralala.bleconnector.utils.gatt.Helper.toU32;
import static fr.ralala.bleconnector.utils.gatt.Helper.bytesToString;
import static fr.ralala.bleconnector.utils.gatt.Helper.decodeStringAndHex;
import static fr.ralala.bleconnector.utils.gatt.Helper.decodeHex;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Map GATT attributes with names.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class GattHelper {
  private static final String APPEARANCE_XML_NAME = "appearances.xml";

  private static final HashMap<String, GattUUID.ItemService> mAttributes = new HashMap<>();
  private static final SparseArray<String> mAppearances = new SparseArray<>();

  static {
    /* managed services */
    mAttributes.put(GattUUID.SERVICE_GENERIC_ACCESS.uuid, GattUUID.SERVICE_GENERIC_ACCESS);
    mAttributes.put(GattUUID.SERVICE_GENERIC_ATTRIBUTE.uuid, GattUUID.SERVICE_GENERIC_ATTRIBUTE);
    mAttributes.put(GattUUID.SERVICE_IMMEDIATE_ALERT.uuid, GattUUID.SERVICE_IMMEDIATE_ALERT);
    mAttributes.put(GattUUID.SERVICE_LINK_LOSS.uuid, GattUUID.SERVICE_LINK_LOSS);
    mAttributes.put(GattUUID.SERVICE_CURRENT_TIME.uuid, GattUUID.SERVICE_CURRENT_TIME);
    mAttributes.put(GattUUID.SERVICE_DEVICE_INFORMATION.uuid, GattUUID.SERVICE_DEVICE_INFORMATION);
    mAttributes.put(GattUUID.SERVICE_USER_DATA.uuid, GattUUID.SERVICE_USER_DATA);
    mAttributes.put(GattUUID.SERVICE_HEART_RATE_SENSOR.uuid, GattUUID.SERVICE_HEART_RATE_SENSOR);
    mAttributes.put(GattUUID.SERVICE_BATTERY.uuid, GattUUID.SERVICE_BATTERY);
    mAttributes.put(GattUUID.SERVICE_NORDIC_UART.uuid, GattUUID.SERVICE_NORDIC_UART);

    /* managed characteristics */
    mAttributes.put(GattUUID.CHARACTERISTIC_DESCRIPTOR.uuid, GattUUID.CHARACTERISTIC_DESCRIPTOR);
    mAttributes.put(GattUUID.CHARACTERISTIC_DEVICE_NAME.uuid, GattUUID.CHARACTERISTIC_DEVICE_NAME);
    mAttributes.put(GattUUID.CHARACTERISTIC_APPEARANCE.uuid, GattUUID.CHARACTERISTIC_APPEARANCE);
    mAttributes.put(GattUUID.CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG.uuid, GattUUID.CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG);
    mAttributes.put(GattUUID.CHARACTERISTIC_RECONNECTION_ADDRESS.uuid, GattUUID.CHARACTERISTIC_RECONNECTION_ADDRESS);
    mAttributes.put(GattUUID.CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS.uuid, GattUUID.CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS);
    mAttributes.put(GattUUID.CHARACTERISTIC_SERVICE_CHANGED.uuid, GattUUID.CHARACTERISTIC_SERVICE_CHANGED);
    mAttributes.put(GattUUID.CHARACTERISTIC_ALERT_LEVEL.uuid, GattUUID.CHARACTERISTIC_ALERT_LEVEL);
    mAttributes.put(GattUUID.CHARACTERISTIC_LOCAL_TIME_INFORMATION.uuid, GattUUID.CHARACTERISTIC_LOCAL_TIME_INFORMATION);
    mAttributes.put(GattUUID.CHARACTERISTIC_BATTERY_LEVEL.uuid, GattUUID.CHARACTERISTIC_BATTERY_LEVEL);
    mAttributes.put(GattUUID.CHARACTERISTIC_SYSTEM_ID.uuid, GattUUID.CHARACTERISTIC_SYSTEM_ID);
    mAttributes.put(GattUUID.CHARACTERISTIC_MODEL_NUMBER_STRING.uuid, GattUUID.CHARACTERISTIC_MODEL_NUMBER_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_SERIAL_NUMBER_STRING.uuid, GattUUID.CHARACTERISTIC_SERIAL_NUMBER_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_FIRMWARE_NUMBER_STRING.uuid, GattUUID.CHARACTERISTIC_FIRMWARE_NUMBER_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_HARDWARE_REVISION_STRING.uuid, GattUUID.CHARACTERISTIC_HARDWARE_REVISION_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_SOFTWARE_REVISION_STRING.uuid, GattUUID.CHARACTERISTIC_SOFTWARE_REVISION_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_MANUFACTURER_NAME_STRING.uuid, GattUUID.CHARACTERISTIC_MANUFACTURER_NAME_STRING);
    mAttributes.put(GattUUID.CHARACTERISTIC_DATA_LIST.uuid, GattUUID.CHARACTERISTIC_DATA_LIST);
    mAttributes.put(GattUUID.CHARACTERISTIC_CURRENT_TIME.uuid, GattUUID.CHARACTERISTIC_CURRENT_TIME);
    mAttributes.put(GattUUID.CHARACTERISTIC_HEART_RATE_MEASUREMENT.uuid, GattUUID.CHARACTERISTIC_HEART_RATE_MEASUREMENT);
    mAttributes.put(GattUUID.CHARACTERISTIC_BODY_SENSOR_LOCATION.uuid, GattUUID.CHARACTERISTIC_BODY_SENSOR_LOCATION);
    mAttributes.put(GattUUID.CHARACTERISTIC_HEART_RATE_CONTROL_POINT.uuid, GattUUID.CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
    mAttributes.put(GattUUID.CHARACTERISTIC_PNP_ID.uuid, GattUUID.CHARACTERISTIC_PNP_ID);
    mAttributes.put(GattUUID.CHARACTERISTIC_AGE.uuid, GattUUID.CHARACTERISTIC_AGE);
    mAttributes.put(GattUUID.CHARACTERISTIC_FIRST_NAME.uuid, GattUUID.CHARACTERISTIC_FIRST_NAME);
    mAttributes.put(GattUUID.CHARACTERISTIC_GENDER.uuid, GattUUID.CHARACTERISTIC_GENDER);
    mAttributes.put(GattUUID.CHARACTERISTIC_LAST_NAME.uuid, GattUUID.CHARACTERISTIC_LAST_NAME);
    mAttributes.put(GattUUID.CHARACTERISTIC_LANGUAGE.uuid, GattUUID.CHARACTERISTIC_LANGUAGE);
    mAttributes.put(GattUUID.CHARACTERISTIC_CENTRAL_ADDRESS_RESOLUTION.uuid, GattUUID.CHARACTERISTIC_CENTRAL_ADDRESS_RESOLUTION);
    mAttributes.put(GattUUID.CHARACTERISTIC_LONGITUDE.uuid, GattUUID.CHARACTERISTIC_LONGITUDE);
    mAttributes.put(GattUUID.CHARACTERISTIC_ALTITUDE.uuid, GattUUID.CHARACTERISTIC_ALTITUDE);
    mAttributes.put(GattUUID.CHARACTERISTIC_NORDIC_TX.uuid, GattUUID.CHARACTERISTIC_NORDIC_TX);
    mAttributes.put(GattUUID.CHARACTERISTIC_NORDIC_RX.uuid, GattUUID.CHARACTERISTIC_NORDIC_RX);
  }

  public GattHelper loadFromAssets() {
    loadAppearance();
    return this;
  }

  /**
   * Converts the byte data to readable string.
   * @param uuid Associated uuid.
   * @param bytes bytes to convert.
   * @return Readable string.
   */
  public String convert(String uuid, byte [] bytes) {
    GattUUID.ItemService item = mAttributes.get(uuid);
    String data;
    if(item == null)
      data = decodeStringAndHex(bytes);
    else {
      if(uuid.equals(GattUUID.CHARACTERISTIC_DESCRIPTOR.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_RECONNECTION_ADDRESS.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_LOCAL_TIME_INFORMATION.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_CURRENT_TIME.toString())) {
        data = decodeHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_DEVICE_NAME.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_MODEL_NUMBER_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_SERIAL_NUMBER_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_FIRMWARE_NUMBER_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_HARDWARE_REVISION_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_SOFTWARE_REVISION_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_MANUFACTURER_NAME_STRING.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_DATA_LIST.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_FIRST_NAME.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_GENDER.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_LANGUAGE.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_LAST_NAME.toString())) {
        data = bytesToString(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_AGE.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_BATTERY_LEVEL.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_HEART_RATE_MEASUREMENT.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_BODY_SENSOR_LOCATION.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_HEART_RATE_CONTROL_POINT.toString())) {
        data = String.format(Locale.US, "%02d ", toU32(bytes));
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_ALTITUDE.toString()) ||
          uuid.equals(GattUUID.CHARACTERISTIC_LONGITUDE.toString())) {
        data = String.valueOf(toU16(bytes[0], bytes[1]));
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_APPEARANCE.toString())) {
        Context c = BleConnectorApplication.getInstance();
        int n = toU16(bytes[0], bytes[1]);
        String ap = mAppearances.get(n);
        data = ap != null ? ap : c.getString(R.string.ble_appearance_unsupported);
        data += "("+n+")";
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_SYSTEM_ID.toString())) {
        if(bytes.length >= 8) {
          Context c = BleConnectorApplication.getInstance();
          data = c.getString(R.string.system_id_manufacturer_identifier) + ": " +
              String.format(Locale.US, "%02x %02x %02x %02x %02x",
                  toU8(bytes[4]), toU8(bytes[3]), toU8(bytes[2]), toU8(bytes[1]), toU8(bytes[0])) + "\n";
          data +=  c.getString(R.string.system_id_organizationally_unique_identifier) + ": " +
              String.format(Locale.US, "%02x %02x %02x", toU8(bytes[7]), toU8(bytes[6]), toU8(bytes[5]));
        } else
          data = decodeStringAndHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_PNP_ID.toString())) {
        if (bytes.length >= 7) {
          Context c = BleConnectorApplication.getInstance();
          data = c.getString(R.string.pnp_id_vendor_id_source) + ": ";
          if (toU8(bytes[0]) == 1) {
            data += "Bluetooth\n";
            data += c.getString(R.string.pnp_id_vendor_id) + ": " + (toU16(bytes[1], bytes[2])) + "\n";
          } else {
            data += "USB\n";
            String str = UsbVendorName.VALUES.get(toU16(bytes[1], bytes[2]));
            data += c.getString(R.string.pnp_id_vendor_id) + ": " + (str == null ? c.getString(R.string.unknown) : str) + "\n";
          }
          data += c.getString(R.string.pnp_id_product_id) + ": " + (toU16(bytes[3], bytes[4])) + "\n";
          data += c.getString(R.string.pnp_id_product_version) + ": " + (toU16(bytes[5], bytes[6]));
        } else
          data = decodeStringAndHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG.toString())) {
        Context c = BleConnectorApplication.getInstance();
        int n = toU32(bytes);
        if(n == 0)
          data = c.getString(R.string.disabled);
        else if(n == 1)
          data = c.getString(R.string.enabled);
        else
          data = decodeStringAndHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_CENTRAL_ADDRESS_RESOLUTION.toString())) {
        Context c = BleConnectorApplication.getInstance();
        int n = toU32(bytes);
        if(n == 0)
          data = c.getString(R.string.supported);
        else if(n == 1)
          data = c.getString(R.string.not_supported);
        else
          data = c.getString(R.string.na);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS.toString())) {
        Context c = BleConnectorApplication.getInstance();
        if(bytes.length >= 8) {
          data = c.getString(R.string.ppcp_min_connection_interval) + ": " + ((toU16(bytes[0], bytes[1])) * 1.25) + "ms\n";
          data += c.getString(R.string.ppcp_max_connection_interval) + ": " + ((toU16(bytes[2], bytes[3])) * 1.25) + "ms\n";
          data += c.getString(R.string.ppcp_slave_latency) + ": " + (toU16(bytes[4], bytes[5])) + "ms\n";
          data += c.getString(R.string.ppcp_connection_supervision_timeout_multiplier) + ": " + (toU16(bytes[6], bytes[7]));
        } else
          data = decodeStringAndHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_ALERT_LEVEL.toString())) {
        Context c = BleConnectorApplication.getInstance();
        int n = toU32(bytes);
        if(n == 0) {
          data = c.getString(R.string.alert_level_no);
        } else if(n == 1) {
          data = c.getString(R.string.alert_level_medium);
        } else if(n == 2) {
          data = c.getString(R.string.alert_level_high);
        } else
          data = decodeStringAndHex(bytes);
      } else if(uuid.equals(GattUUID.CHARACTERISTIC_SERVICE_CHANGED.toString())) {
        Context c = BleConnectorApplication.getInstance();
        if(bytes.length >= 4) {
          data = c.getString(R.string.start_of_affected_attribute_handle_range) + ": " + (toU16(bytes[0], bytes[1])) + "\n";
          data += c.getString(R.string.end_of_affected_attribute_handle_range) + ": " + (toU16(bytes[2], bytes[3]));
        } else
          data = decodeStringAndHex(bytes);
      } else
        data = decodeStringAndHex(bytes);
    }
    return data;
  }

  /**
   * Lookups uuid to name.
   * @param uuid UUID to search.
   * @param defaultName Default name to use if not found.
   * @return The name of the default name.
   */
  public String lookup(String uuid, String defaultName) {
    GattUUID.ItemService item =  mAttributes.get(uuid);
    return item == null || item.name == null ? defaultName : item.name;
  }

  public static String fixUUID(String uuid) {
    if(uuid.startsWith("0000") && uuid.endsWith("-0000-1000-8000-00805f9b34fb")) {
      uuid = uuid.substring(0, uuid.indexOf('-'));
      if(uuid.startsWith("0000"))
        uuid = uuid.substring(4);
      uuid = "0x" + uuid;
    }
    return uuid;
  }

  /**
   * Load the Appearance XML
   */
  private void loadAppearance() {
    InputStream in_s = null;
    try {
      XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
      pullParserFactory.setNamespaceAware(true);
      XmlPullParser parser = pullParserFactory.newPullParser();
      in_s = BleConnectorApplication.getInstance().getAssets().open(APPEARANCE_XML_NAME);
      parser.setInput(in_s, null);
      String text = "";
      int eventType = parser.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        String tagname = parser.getName();
        switch (eventType) {
          case XmlPullParser.TEXT:
            text = parser.getText();
            break;
          case XmlPullParser.END_TAG:
            if (tagname.toLowerCase().startsWith("idx_")) {
              mAppearances.put(Integer.parseInt(tagname.substring(4)), text);
            }
            break;

          default:
            break;
        }
        eventType = parser.next();
      }
    } catch (Exception e) {
      String msg = "Exception: " + e.getMessage();
      Log.e("Gatt", msg, e);
      Toast.makeText(BleConnectorApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    } finally {
      if(in_s != null)
        try {
          in_s.close();
        } catch (Exception e) {
          Log.e("Gatt", "Exception: " + e.getMessage(), e);

        }
    }
  }
}
