package fr.ralala.bleconnector.utils.gatt;

import java.util.Locale;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * Helpers functions.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class Helper {
  public static int toU8(byte b) {
    return b & 0xFF;
  }

  /**
   * Converts the input byte array to string.
   * @param bytes Bytes array.
   * @return int
   */
  public static String bytesToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append((b >= 0x20 && b < 0x7F) ? (char) b : '.');
    return sb.toString();
  }

  /**
   * Converts the input byte array to string hex.
   * @param bytes Bytes array.
   * @return String hex (eg: 0x00 0x01)
   */
  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append(String.format(Locale.US, "0x%02x ", b));
    return sb.toString();
  }

  /**
   * Converts the input byte array to int.
   * @param bytes Bytes array.
   * @return int
   */
  public static int bytesToInt(byte[] bytes) {
    int val = 0;
    if(bytes.length>4) throw new RuntimeException("Too big to fit in int");
    for (byte b : bytes) {
      val=val<<8;
      val=val|(b & 0xFF);
    }
    return val;
  }

  /**
   * Test if the array contains anything other than zeros.
   * @param bytes Bytes array.
   * @return boolean
   */
  public static boolean isNonZeroArray(byte [] bytes) {
    for(byte b : bytes)
      if(b != 0)
        return true;
    return false;
  }

  /**
   * Decodes the input array as string and hex values.
   * @param bytes Bytes array.
   * @return String\nHex
   */
  public static String decodeStringAndHex(byte [] bytes) {
    if(isNonZeroArray(bytes))
      return bytesToHex(bytes) + "\n" + bytesToString(bytes);
    else
      return "0x00";
  }

  /**
   * Decodes the input array as hex value.
   * @param bytes Bytes array.
   * @return nHex
   */
  public static String decodeHex(byte [] bytes) {
    if(isNonZeroArray(bytes))
      return bytesToHex(bytes);
    else
      return "0x00";
  }
}
