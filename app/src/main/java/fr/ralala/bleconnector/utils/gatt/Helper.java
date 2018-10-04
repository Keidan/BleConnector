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
  static int toU8(byte b) {
    return b & 0xFF;
  }

  static int toU16(byte[] b) {
    return (toU8(b[0]) | toU8(b[1]) << 8);
  }

  static int toU16(byte b1, byte b2) {
    return toU16(new byte[]{b1, b2});
  }

  static int toU32(byte[] b) {
    switch (b.length) {
      case 1:
        return toU8(b[0]);
      case 2:
        return toU16(b);
      case 3:
        return (toU8(b[0]) | toU8(b[1]) << 8 | toU8(b[2]) << 16);
      case 4:
        return (toU8(b[0]) | toU8(b[1]) << 8 | toU8(b[2]) << 16 | toU8(b[3]) << 24);
      default:
        return 0;
    }
  }

  /**
   * Converts the input byte array to string.
   *
   * @param bytes Bytes array.
   * @return int
   */
  static String bytesToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append((b >= 0x20 && b < 0x7F) ? (char) b : '.');
    return sb.toString();
  }

  /**
   * Converts the input byte array to string hex.
   *
   * @param bytes  Bytes array.
   * @param revert Revert the output.
   * @return String hex (eg: 0x00 0x01)
   */
  private static String bytesToHex(byte[] bytes, boolean revert) {
    StringBuilder sb = new StringBuilder();
    if (!revert)
      for (byte b : bytes)
        sb.append(String.format(Locale.US, "0x%02x ", toU8(b)));
    else
      for (int i = bytes.length - 1; i >= 0; i--)
        sb.append(String.format(Locale.US, "0x%02x ", toU8(bytes[i])));
    return sb.toString();
  }

  /**
   * Test if the array contains anything other than zeros.
   *
   * @param bytes Bytes array.
   * @return boolean
   */
  private static boolean isNonZeroArray(byte[] bytes) {
    for (byte b : bytes)
      if (b != 0)
        return true;
    return false;
  }

  /**
   * Decodes the input array as string and hex values.
   *
   * @param bytes Bytes array.
   * @return String\nHex
   */
  static String decodeStringAndHex(byte[] bytes) {
    if (isNonZeroArray(bytes))
      return bytesToHex(bytes, false) + "\n" + bytesToString(bytes);
    else
      return "0x00";
  }

  /**
   * Decodes the input array as hex value.
   *
   * @param bytes  Bytes array.
   * @param revert Revert the output.
   * @return nHex
   */
  static String decodeHex(byte[] bytes, boolean revert) {
    if (isNonZeroArray(bytes))
      return bytesToHex(bytes, revert);
    else
      return "0x00";
  }
}
