package com.ardic.android.ignitegreenhouse.constants;

/**
 * Created by Mert Acel on 7/17/17.
 */

public class Constant {

    public class ResponseJsonValue {
        /**
         * Response Value
         * 100 : The received data have already been registered. Please delete first !
         * 101 : We have not found your sensor system. Sensor type not found
         * 102 : Value Available. Not found Thing
         * 103 : The submitted parameters can not be empty
         * 104 : Removed all Sensor - Type - Thread
         * 105 : Format Error
         * 106 : Null Message ID
         */
        public static final String CREATE_MESSAGE_DESCRIPTIONS_BEEN_REGISTERED = "100";
        public static final String CREATE_MESSAGE_DESCRIPTIONS_SENSOR_TYPE = "101";
        public static final String VALUE_AVAILABLE = "102";
        public static final String VALUE_EMPTY = "103";
        public static final String REMOVE_ALL_COMPONENT = "104";
        public static final String CREATE_MESSAGE_FORMAT_ERROR = "105";
        public static final String NULL_MESSAGE_ID = "106";
    }

    public class ResponseJsonKey {
        /**
         * Response Key
         */
        public static final String REMOVE_THING = "removedThingResponse";
        public static final String CREATE_SENSOR = "responseAddDevice";
        public static final String CREATE_NODE = "createNode";
        public static final String CREATE_THING = "createThing";
        public static final String REMOVED_THING_TYPE = "removedThingType";
        public static final String DESCRIPTIONS = "descriptions";
        public static final String NEW_THING_ERROR = "errorNewThingResponse";
        public static final String REMOVE_NODE = "removedNodeResponse";
        public static final String ERROR = "error";
        public static final String NULL="null";
    }

    public class ThingType {
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
        public static final String ADD_NEW_THING_TYPE = "addNewThingType";

        public static final String THING_CODE = "thingCode";
        public static final String THING_SPECIFIC = "thingSpecific";
        public static final String THING_TYPE_STRING = "thingTypeString";
        public static final String THING_VENDOR = "thingVendor";
        public static final String THING_DATA_TYPE = "thingType";


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
/**public static final String THING_CODE = "thingCode";*/


        /**
         * It contains the types of embedded things that must occur at the beginning
         */
        public static final String ADD_STATIC_THING_TYPE = "{\"addNewThingType\":[{\"thingCode\":\"01\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Temperature\",\"thingTypeString\":\"GHT-Temperature\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}},{\"thingCode\":\"02\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Humidity\",\"thingTypeString\":\"GHT-Humidity\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}}]}";

        /**
         * New Thing Type data type
         */
        public static final String DATA_TYPE_FLOAT = "float";
        public static final String DATA_TYPE_INTEGER = "integer";
        public static final String DATA_TYPE_STRING = "string";

        public static final String ERROR = "error";
        public static final String ERROR_STRING="Error new thing";
    }


    public class NodeThing {
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
        public static final String MESSAGE_ID = "messageId";
        public static final String ADD_NEW_NODE_THING = "addDevice";
        public static final String NODE_ID = "nodeId";
        public static final String THINGS_ARRAY = "things";
        public static final String THING_LABEL = "thingId";
        public static final String THING_CODE = "thingCode";


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
        public static final String REMOVE_THING = "";
        /**public static final String NODE_ID = "nodeId";*/
        /**public static final String THING_LABEL = "thingId";*/


        /**
         * This "json key" deletes all node, thing, thing type found in the device and stops active threads.
         * It will not open out. It was written for testing.
         * {"removeAllDevice":true}
         */
        public static final String REMOVE_ALL_DEVICE = "removeAllDevice";

    }


    public class IntentName {
        /**
         * Intent Filters required when using Local Broadcast
         */
        public static final String IGNITE_STATUS_ACTION = "igniteConnect";
        public static final String CONFIG = "getConfig";
    }

    public class IntentFilter {
        /**
         * Intent Filters required when using Local Broadcast
         */
        public static final String IGNITE_STATUS = "igniteStatus";
        public static final String NODE_NAME = "getConfigPutNodeName";
        public static final String THING_NAME = "getConfigPutThingName";
        public static final String THING_FREQUENCY = "getConfigPutFrequency";
        public static final String CONFIGURATION_FLAG="configurationFlag";
    }


    public class Configurator {
        /**
         * All incoming data, this node and the thing must come. Otherwise it will not work at all
         */
        public static final String NODE_NAME = "Configurator";
        public static final String THING_NAME = "Configurator Thing";

        /**
         * Configurator Node & Thing creation information
         */
        public static final String THING_TYPE = "ARDIC Configurator";
        public static final String THING_VENDOR = "ARDIC";
    }


    public class Regexp {
        /**
         * Some verbal expressions used in the program are used for the format conformance we want
         */
        public static final String SENSOR_CONTROL = "[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]";
        public static final String THREAD_CONTROL = "[0-9a-fA-F][0-9a-fA-F]";
    }


    /**
     * This is defined for turning on and off the logs used in the program.
     */
    public static final boolean DEBUG = true;


    /**
     * Determines how many letters the incoming sensor number is in
     */
    public static final int NUMBER_OF_CHARACTERS = 8;


    /**
     * If the "preference" data is not available, this data is predefined
     */
    public static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";


    /**
     * String to appear when disconnect
     */
    public static final String APPLICATION_DESTROYED_STRING = "Application Destroyed";

    public static final int NOT_READ_CONFIGURATION = -5;
    public static final int NOT_READ_IGNITE_CONFIGURATION = -1;

    private Constant() {
// Default
    }
}
