package fr.ralala.bleconnector.utils.gatt;



/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * GATT attributes and services UUID.
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public interface GattUUID {

  class ItemService {
    public String name;
    public String uuid;

    private ItemService(String uuid, String name) {
      this.name = name;
      this.uuid = uuid;
    }

    public String toString() {
      return uuid;
    }
  }
  ItemService SERVICE_GENERIC_ACCESS = new ItemService("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
  ItemService SERVICE_GENERIC_ATTRIBUTE = new ItemService("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
  ItemService SERVICE_IMMEDIATE_ALERT = new ItemService("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
  ItemService SERVICE_LINK_LOSS = new ItemService("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
  ItemService SERVICE_CURRENT_TIME = new ItemService("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
  ItemService SERVICE_DEVICE_INFORMATION = new ItemService("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
  ItemService SERVICE_USER_DATA = new ItemService("0000181c-0000-1000-8000-00805f9b34fb", "User Data");
  ItemService SERVICE_HEART_RATE_SENSOR = new ItemService("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
  ItemService SERVICE_BATTERY = new ItemService("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
  ItemService SERVICE_NORDIC_UART = new ItemService("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "Nordic UART Service");
  ItemService CHARACTERISTIC_DESCRIPTOR = new ItemService("00002902-0000-1000-8000-00805f9b34fb", "Descriptor");
  ItemService CHARACTERISTIC_DEVICE_NAME = new ItemService("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
  ItemService CHARACTERISTIC_APPEARANCE = new ItemService("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
  ItemService CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG = new ItemService("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
  ItemService CHARACTERISTIC_RECONNECTION_ADDRESS = new ItemService("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
  ItemService CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = new ItemService("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
  ItemService CHARACTERISTIC_SERVICE_CHANGED = new ItemService("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
  ItemService CHARACTERISTIC_ALERT_LEVEL = new ItemService("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
  ItemService CHARACTERISTIC_LOCAL_TIME_INFORMATION = new ItemService("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
  ItemService CHARACTERISTIC_BATTERY_LEVEL = new ItemService("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
  ItemService CHARACTERISTIC_SYSTEM_ID = new ItemService("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
  ItemService CHARACTERISTIC_MODEL_NUMBER_STRING = new ItemService("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
  ItemService CHARACTERISTIC_SERIAL_NUMBER_STRING = new ItemService("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
  ItemService CHARACTERISTIC_FIRMWARE_NUMBER_STRING = new ItemService("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
  ItemService CHARACTERISTIC_HARDWARE_REVISION_STRING = new ItemService("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
  ItemService CHARACTERISTIC_SOFTWARE_REVISION_STRING = new ItemService("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
  ItemService CHARACTERISTIC_MANUFACTURER_NAME_STRING = new ItemService("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
  ItemService CHARACTERISTIC_DATA_LIST = new ItemService("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
  ItemService CHARACTERISTIC_CURRENT_TIME = new ItemService("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
  ItemService CHARACTERISTIC_HEART_RATE_MEASUREMENT = new ItemService("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
  ItemService CHARACTERISTIC_BODY_SENSOR_LOCATION = new ItemService("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
  ItemService CHARACTERISTIC_HEART_RATE_CONTROL_POINT = new ItemService("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
  ItemService CHARACTERISTIC_PNP_ID = new ItemService("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
  ItemService CHARACTERISTIC_AGE = new ItemService("00002a80-0000-1000-8000-00805f9b34fb", "Age");
  ItemService CHARACTERISTIC_FIRST_NAME = new ItemService("00002a8a-0000-1000-8000-00805f9b34fb", "First Name");
  ItemService CHARACTERISTIC_GENDER = new ItemService("00002a8c-0000-1000-8000-00805f9b34fb", "Gender");
  ItemService CHARACTERISTIC_LAST_NAME = new ItemService("00002a90-0000-1000-8000-00805f9b34fb", "Last Name");
  ItemService CHARACTERISTIC_LANGUAGE = new ItemService("00002aa2-0000-1000-8000-00805f9b34fb", "Language");
  ItemService CHARACTERISTIC_CENTRAL_ADDRESS_RESOLUTION = new ItemService("00002aa6-0000-1000-8000-00805f9b34fb", "Central Address Resolution");
  ItemService CHARACTERISTIC_LONGITUDE = new ItemService("00002aaf-0000-1000-8000-00805f9b34fb", "Longitude");
  ItemService CHARACTERISTIC_ALTITUDE = new ItemService("00002ab3-0000-1000-8000-00805f9b34fb", "Altitude");
  ItemService CHARACTERISTIC_NORDIC_TX = new ItemService("6e400003-b5a3-f393-e0a9-e50e24dcca9e", "TX Characteristic");
  ItemService CHARACTERISTIC_NORDIC_RX = new ItemService("6e400002-b5a3-f393-e0a9-e50e24dcca9e", "RX Characteristic");

}
