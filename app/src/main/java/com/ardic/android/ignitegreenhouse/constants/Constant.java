package com.ardic.android.ignitegreenhouse.constants;

/**
 * Created by acel on 7/17/17.
 */

public class Constant {
    /**
     * This is defined for turning on and off the logs used in the program.
     */
    public static final boolean DEBUG = true;

    /**
     * Some verbal expressions used in the program are used for the format conformance we want
     */
    public static final String SENSOR_CONTROL_REGEXP = "[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]";
    public static final String THREAD_CONTROL_REGEXP = "[0-9a-fA-F][0-9a-fA-F]";

    /**
     * Determines how many letters the incoming sensor number is in
     */
    public static final int NUMBER_OF_CHARACTERS = 8;


    /**
     * Used for Thing Type operations. The following format is used to create a new thing.
     * {
     * "addNewThingType": [
     * {
     * "thingCode": "<String>",
     * "thingSpecific": {
     * "thingId": "<String>",
     * "thingTypeString": "<String>",
     * "thingVendor": "<String>",
     * "thingType": "<String>"
     * }
     * }
     * ]
     * }
     * <p>
     * thingCode       : "01" to "ff" is a hexadecimal number. If there is something
     * else with the same name before, it will not be valid.
     * It will not be registered
     * thingTypeString : Thing type name will be given
     * thingVendor     : Manufacturer name given
     * thingType       : The type of data that the grant will go to the cloud should be given.
     * For verbal expressions  : string
     * For numeric expressions : integer
     * For decimal expressions : float
     */
    public static final String ADD_NEW_THING_TYPE_JSON_KEY = "addNewThingType";
    public static final String THING_CODE_JSON_KEY = "thingCode";
    public static final String THING_SPECIFIC_JSON_KEY = "thingSpecific";
    public static final String THING_TYPE_STRING_JSON_KEY = "thingTypeString";
    public static final String THING_VENDOR_JSON_KEY = "thingVendor";
    public static final String THING_TYPE_JSON_KEY = "thingType";

    /**
     * Used to delete a saved thing
     * The following format is used to remove a new thing.
     * {
     * "removeThingType": {
     * "thingCode": "03"
     * }
     * }
     */
    public static final String REMOVE_THING_TYPE = "removeThingType";

    /**
     * It contains the types of embedded things that must occur at the beginning
     */
    public static final String ADD_STATIC_THING_TYPE = "{\"addNewThingType\":[{\"thingCode\":\"01\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Temperature\",\"thingTypeString\":\"GHT-Temperature\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}},{\"thingCode\":\"02\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Humidity\",\"thingTypeString\":\"GHT-Humidity\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}}]}";


    /**
     * Used to create new node and thing
     * The format used is as follows:
     * {
     * "addDevice": [
     * {
     * "nodeId": "GreenHouse1",
     * "things": [
     * {
     * "thingCode": "10010001",
     * "thingId": "Temperature"
     * }
     * ]
     * }
     * ]
     * }
     */
    public static final String ADD_NEW_NODE_THING_JSON_KEY = "addDevice";
    public static final String NODE_ID_JSON_KEY = "nodeId";
    public static final String THINGS_ARRAY_JSON_KEY = "things";
    public static final String THING_LABEL_JSON_KEY = "thingId";
    /**
     * public static final String THING_CODE_JSON_KEY = "thingCode";
     */

//TODO : açıklamalara devam et
    public static final String REMOVE_THING_JSON_KEY = "removeThing";


    public static final String INTENT_FILTER_IGNITE_STATUS = "igniteConnect";
    public static final String INTENT_FILTER_CONFIG = "getConfig";
    public static final String IGNITE_STATUS_BROADCAST = "igniteStatus";

    public static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";


    public static final String REMOVE_THING_STRING_RESPONSE = "removedThingResponse";
    public static final String REMOVE_ALL_COMPONENT_MESSAGE = "All Sensor - Type - Thread";


    /**
     * Returning notifications
     */
    public static final String RESPONSE_CREATE_MESSAGE_FORMAT_ERROR = "{\"error\":\"Error Format\"}";


}
