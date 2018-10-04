package fr.ralala.bleconnector.utils.gatt.services;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT current time service TimeData
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class TimeData {
  private TimeData() {
  }

  // Adjustment Flags
  public static final byte ADJUST_NONE = 0x0;
  public static final byte ADJUST_MANUAL = 0x1;
  public static final byte ADJUST_EXTERNAL = 0x2;
  public static final byte ADJUST_TIMEZONE = 0x4;
  public static final byte ADJUST_DST = 0x8;

  public static byte[] exactTime256WithUpdateReason(Calendar time, byte updateReason) {
    // See https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.current_time.xml

    // Calendar: January = 0
    // CTS: January = 1
    int month = time.get(Calendar.MONTH) + 1;

    // Calendar: Monday = 2, Sunday = 1
    // CTS: Monday = 1, Sunday = 7
    int dayOfWeek = time.get(Calendar.DAY_OF_WEEK);
    if (dayOfWeek == Calendar.SUNDAY) {
      dayOfWeek = 7;
    } else {
      dayOfWeek = dayOfWeek - 1;
    }

    // 256ths of a second
    int fractionsOfSecond = (int) ((time).get(Calendar.MILLISECOND) * 0.255F);

    return ByteBuffer.allocate(10)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putShort((short) time.get(Calendar.YEAR))
        .put((byte) month)
        .put((byte) time.get(Calendar.DAY_OF_MONTH))
        .put((byte) time.get(Calendar.HOUR_OF_DAY))
        .put((byte) time.get(Calendar.MINUTE))
        .put((byte) time.get(Calendar.SECOND))
        .put((byte) dayOfWeek)
        .put((byte) fractionsOfSecond)
        .put(updateReason)
        .array();
  }

  public static byte[] timezoneWithDstOffset(Calendar time) {
    // See https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.local_time_information.xml
    // UTC-1:00 = -4
    // UTC+0:00 = 0
    // UTC+1:00 = 4
    int timezone = time.get(Calendar.ZONE_OFFSET) / 1000 / 60 / 15;
    int dstOffset = time.get(Calendar.DST_OFFSET) / 1000 / 60 / 15;

    return ByteBuffer.allocate(2)
        .order(ByteOrder.LITTLE_ENDIAN)
        .put((byte) timezone)
        .put((byte) dstOffset)
        .array();
  }
}
