package fr.ralala.bleconnector.utils.gatt;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  private static final String CHARACTERISTICS_FOLDER = "characteristics";
  private static final String SERVICES_FOLDER = "services";
  private static final String JSON_FILE_EXTENSION = ".json";
  private static final SparseArray<String> mAppearances = new SparseArray<>();
  private static final HashMap<String, String> mCharacteristics = new HashMap<>();
  private static final HashMap<String, String> mServices = new HashMap<>();

  public GattHelper loadFromAssets() {
    mCharacteristics.clear();
    mServices.clear();
    loadAppearance();
    listFilesJSON(CHARACTERISTICS_FOLDER, mCharacteristics);
    listFilesJSON(SERVICES_FOLDER, mServices);
    return this;
  }

  /**
   * Converts the byte data to a readable string.
   *
   * @param uuid  Associated uuid.
   * @param bytes bytes to convert.
   * @return Readable string.
   */
  public String convert(String uuid, byte[] bytes) {
    String data = "";
    String file = mCharacteristics.get(uuid.toLowerCase());
    if (file != null) {
      InputStream is = null;
      try {
        Context c = BleConnectorApplication.getInstance();
        is = c.getAssets().open(file);
        JSONObject object = new JSONObject(read(is));
        String format = object.getString("format");
        String unit = object.has("unit") ? (" " + object.getString("unit")) : "";
        switch (format) {
          case "string":
            data = bytesToString(bytes) + unit;
            break;
          case "u8":
            data = String.format(Locale.US, "%02d ", toU8(bytes[0])) + unit;
            break;
          case "u32":
            data = String.format(Locale.US, "%02d ", toU32(bytes)) + unit;
            break;
          case "u16":
            data = String.format(Locale.US, "%02d ", toU16(bytes[0], bytes[1])) + unit;
            break;
          case "enum":
            data = getEnumJSON(object, bytes) + unit;
            break;
          case "hex":
            data = getHexJSON(object, bytes) + unit;
            break;
          case "appearance":
            int n = toU16(bytes[0], bytes[1]);
            String ap = mAppearances.get(n);
            data = ap != null ? ap : c.getString(R.string.ble_appearance_unsupported);
            data += "(" + n + ")";
            break;
          case "bits": {
            if (object.has("bits")) {
              JSONArray array = object.getJSONArray("bits");
              if (array != null) {
                StringBuilder sbData = new StringBuilder();
                boolean usb_PnP_found = false;
                for (int i = 0; i < array.length(); i++) {
                  JSONObject o = array.getJSONObject(i);
                  String name = o.getString("name");
                  unit = o.has("unit") ? (" " + o.getString("unit")) : "";
                  int start = o.getInt("start");
                  int length = o.getInt("length");
                  String fmt = o.getString("format");
                  String direction = object.has("direction") ? object.getString("direction") : "normal";
                  sbData.append(name).append(": ");
                  switch (fmt) {
                    case "enum": {
                      byte bs[] = copyBytes(bytes, length, start);
                      String s = getEnumJSON(o, bs);
                      if (s.equals("USB") && uuid.equals("00002a50-0000-1000-8000-00805f9b34fb"))
                        usb_PnP_found = true;
                      sbData.append(s);
                      break;
                    }
                    case "u8": {
                      byte bs[] = copyBytes(bytes, length, start);
                      sbData.append(String.format(Locale.US, "%02d", toU8(bs[0]))).append(unit);
                      break;
                    }
                    case "u16": {
                      byte bs[] = copyBytes(bytes, length, start);
                      if (usb_PnP_found && start == 1 && length == 2) {
                        usb_PnP_found = false;
                        String str = UsbVendorName.VALUES.get(toU16(bs[0], bs[1]));
                        sbData.append((str == null ? c.getString(R.string.unknown) : str));
                      } else
                        sbData.append(String.format(Locale.US, "%02d", (direction.equals("normal")) ?
                            toU16(bs[0], bs[1]) : toU16(bs[1], bs[0]))).append(unit);
                      break;
                    }
                    case "u32": {
                      byte bs[] = copyBytes(bytes, length, start);
                      sbData.append(String.format(Locale.US, "%02d", toU32(bs))).append(unit);
                      break;
                    }
                    case "u16_time": {
                      byte bs[] = copyBytes(bytes, length, start);
                      int u16 = (int) ((direction.equals("normal") ?
                          toU16(bs[0], bs[1]) : toU16(bs[1], bs[0])) * 1.25);
                      sbData.append(String.format(Locale.US, "%02d", u16)).append(unit);
                      break;
                    }
                    case "hex": {
                      byte bs[] = copyBytes(bytes, length, start);
                      sbData.append(getHexJSON(o, bs)).append(unit);
                      break;
                    }
                  }
                  if (i < array.length() - 1)
                    sbData.append("\n");
                }
                data = sbData.toString();
              } else
                data = decodeStringAndHex(bytes);
            } else
              data = decodeStringAndHex(bytes);
            break;
          }
          default:
            data = decodeStringAndHex(bytes);
            break;
        }
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      } finally {
        try {
          if (is != null)
            is.close();
        } catch (IOException ie) {
          Log.e(getClass().getSimpleName(), "IOException: " + ie.getMessage(), ie);
        }
      }
    } else
      data = decodeStringAndHex(bytes);
    return data;
  }

  /**
   * Lookups uuid to name.
   *
   * @param uuid        UUID to search.
   * @param defaultName Default name to use if not found.
   * @param isService   True if the UUID match with a service UUID.
   * @return The name of the default name.
   */
  public String lookup(String uuid, String defaultName, boolean isService) {
    final String lc_uuid = uuid.toLowerCase();
    String file = isService ? mServices.get(lc_uuid) : mCharacteristics.get(lc_uuid);
    String name = null;
    if (file != null) {
      InputStream is = null;
      try {
        is = BleConnectorApplication.getInstance().getAssets().open(file);
        JSONObject object = new JSONObject(read(is));
        name = object.getString("name");
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      } finally {
        try {
          if (is != null)
            is.close();
        } catch (IOException ie) {
          Log.e(getClass().getSimpleName(), "IOException: " + ie.getMessage(), ie);
        }
      }
    }
    return name == null ? defaultName : name;
  }

  public static String fixUUID(String uuid) {
    if (uuid.startsWith("0000") && uuid.endsWith("-0000-1000-8000-00805f9b34fb")) {
      uuid = uuid.substring(0, uuid.indexOf('-'));
      if (uuid.startsWith("0000"))
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
      if (in_s != null)
        try {
          in_s.close();
        } catch (Exception e) {
          Log.e("Gatt", "Exception: " + e.getMessage(), e);

        }
    }
  }

  /**
   * List JSON files from assets folder (or sub folder)
   *
   * @param path      The root path.
   * @param listFiles The output list [uuid, file_path]
   * @return false on error.
   */
  private boolean listFilesJSON(String path, HashMap<String, String> listFiles) {
    try {
      String[] list = BleConnectorApplication.getInstance().getAssets().list(path);
      if (list.length > 0) {
        // This is a folder
        for (String file : list) {
          if (!listFilesJSON(path + "/" + file, listFiles))
            return false;
          else {
            if (file.endsWith(JSON_FILE_EXTENSION)) {
              String uuid = file.substring(0, file.length() - JSON_FILE_EXTENSION.length()).toLowerCase();
              if (Pattern.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", uuid)) {
                listFiles.put(uuid, path + "/" + file);
              } else
                Log.e(getClass().getSimpleName(), "listFilesJSON: Invalid json UUID name: '" + file + "'");
            } else
              Log.e(getClass().getSimpleName(), "listFilesJSON: File ignored: '" + file + "'");
          }
        }
      }
    } catch (IOException e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  private static String read(InputStream input) throws IOException {
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  /**
   * Decodes the enum format.
   *
   * @param object Current JSON object.
   * @param bytes  Input bytes (from BLE).
   * @return The enum in string.
   * @throws Exception If an error occurs.
   */
  private String getEnumJSON(JSONObject object, byte[] bytes) throws Exception {
    String data = "";
    String defval = object.has("defval") ? object.getString("defval") : "ERROR";
    JSONArray array = object.has("enum") ? object.getJSONArray("enum") : null;
    if (array != null) {
      int n = toU32(bytes);
      for (int i = 0; i < array.length(); i++) {
        JSONObject o = array.getJSONObject(i);
        try {
          String name = o.getString("name");
          String value = o.getString("value");
          if (n == Integer.parseInt(value)) {
            data = name;
            break;
          }
        } catch (Exception e) {
          data = defval;
          break;
        }
      }
      if (data.isEmpty())
        data = defval;
    } else
      data = decodeStringAndHex(bytes);
    return data;
  }

  /**
   * Decodes the hex format.
   *
   * @param object Current JSON object.
   * @param bytes  Input bytes (from BLE).
   * @return The hex in string.
   * @throws Exception If an error occurs.
   */
  private String getHexJSON(JSONObject object, byte[] bytes) throws Exception {
    String data;
    String direction = object.has("direction") ? object.getString("direction") : "normal";
    int split = object.has("split") ? object.getInt("split") : 0;
    if (split == 0) {
      data = decodeHex(bytes, !direction.equals("normal"));
    } else {
      data = decodeHex(bytes, !direction.equals("normal")).replaceAll("0x", "");
    }
    return data;
  }

  /**
   * Copy bytes from one array to another.
   *
   * @param bytes  Input array.
   * @param length Number of bytes to copy.
   * @param start  Start position.
   * @return New array.
   */
  private byte[] copyBytes(byte[] bytes, int length, int start) {
    byte bs[] = new byte[length];
    for (int k = 0, j = start; j < start + length; k++, j++)
      bs[k] = bytes[j];
    return bs;
  }
}
