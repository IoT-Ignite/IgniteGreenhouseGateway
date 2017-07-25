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


    /**All incoming data, this node and the thing must come. Otherwise it will not work at all*/
    public static final String CONFIGURATION_NODE_NAME = "Configurator";
    public static final String CONFIGURATION_THING_NAME = "Configurator Thing";


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
     * "thingCode": "<00-ff>"
     * }
     * }
     */
    public static final String REMOVE_THING_TYPE = "removeThingType";
/**public static final String THING_CODE_JSON_KEY = "thingCode";*/


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
     * "nodeId": "<String>",
     * "things": [
     * {
     * "thingCode": "<String>",
     * "thingId": "<String>"
     * }
     * ]
     * }
     * ]
     * }
     */
    public static final String MESSAGE_ID_JSON_KEY ="messageId";
    public static final String ADD_NEW_NODE_THING_JSON_KEY = "addDevice";
    public static final String NODE_ID_JSON_KEY = "nodeId";
    public static final String THINGS_ARRAY_JSON_KEY = "things";
    public static final String THING_LABEL_JSON_KEY = "thingId";
    /**
     * public static final String THING_CODE_JSON_KEY = "thingCode";
     */


    /**
     * Used to create remove thing,
     * The format used is as follows:
     * {
     * "removeThing":{
     * "nodeId":"GreenHouse1",
     * "thingId":"Temperature"
     * }
     * }
     */
    public static final String REMOVE_THING_JSON_KEY = "";
    /**public static final String NODE_ID_JSON_KEY = "nodeId";*/
    /**public static final String THING_LABEL_JSON_KEY = "thingId";*/



    /**
     * Response Key
     */
    public static final String RESPONSE_REMOVE_THING_JSON_KEY = "removedThingResponse";
    public static final String RESPONSE_CREATE_SENSOR_JSON_KEY = "responseAddDevice";
    public static final String RESPONSE_CREATE_NODE_JSON_KEY = "createNode";
    public static final String RESPONSE_CREATE_THING_JSON_KEY = "createThing";
    public static final String RESPONSE_REMOVED_THING_TYPE_JSON_KEY = "removedThingType";
    public static final String RESPONSE_DESCRIPTIONS_JSON_KEY = "descriptions";
    public static final String RESPONSE_NEW_THING_ERROR_JSON_KEY = "errorNewThingResponse";
    public static final String RESPONSE_REMOVE_NODE_JSON_KEY="removedNodeResponse";
    public static final String RESPONSE_ERROR="error";

    /**
     * Response Value
     */
    public static final String RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_BEEN_REGISTERED_JSON_VALUE = "100";//"The received data have already been registered. Please delete first !";//100
    public static final String RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_SENSOR_TYPE_JSON_VALUE = "101";//"We have not found your sensor system"; Sensor typenot found
    public static final String RESPONSE_VALUE_AVAILABLE_JSON_VALUE = "102";//"Value Available"; // Not found Thing
    public static final String RESPONSE_VALUE_EMPTY_JSON_VALUE = "103"; //"The submitted parameters can not be empty";
    public static final String RESPONSE_REMOVE_ALL_COMPONENT_JSON_VALUE = "104";//"All Sensor - Type - Thread";
    public static final String RESPONSE_CREATE_MESSAGE_FORMAT_ERROR = "105"; // Format Error
    public static final String RESPONSE_NULL_MESSAGE_ID_JSON_VALUE ="106"; // Null Messaj Id

    /**
     * This "json key" deletes all node, thing, thing type found in the device and stops active threads.
     * It will not open out. It was written for testing.
     * {"removeAllDevice":true}
     */
    public static final String REMOVE_ALL_DEVICE_JSON_KEY = "removeAllDevice";

    /**
     * If the "preference" data is not available, this data is predefined
     */
    public static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";


    /**
     * Intent Filters required when using Local Broadcast
     */
    public static final String INTENT_FILTER_IGNITE_STATUS = "igniteConnect";
    public static final String INTENT_FILTER_CONFIG = "getConfig";
    public static final String INTENT_FILTER_IGNITE_STATUS_VALUE_NAME = "igniteStatus";

    public static final String INTENT_NODE_NAME = "getConfigPutNodeName";
    public static final String INTENT_THING_NAME = "getConfigPutThingName";
    public static final String INTENT_THING_FREQUENCY = "getConfigPutFrequency";

    /**Configurator Node & Thing creation information*/
    public static final String CONFIGURATOR_TYPE_THING = "ARDIC Configurator";
    public static final String CONFIGURATOR_TYPE_VENDOR = "ARDIC";

    /**New Thing Type data type*/
    public static final String THING_TYPE_FLOAT = "float";
    public static final String THING_TYPE_INTEGER = "integer";
    public static final String THING_TYPE_STRING = "string";

    /**String to appear when disconnect*/
    public static final String APPLICATION_DESTROYED_STRING = "Application Destroyed";

    private Constant(){
// Default
    }
}
