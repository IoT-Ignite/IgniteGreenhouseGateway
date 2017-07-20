package com.ardic.android.ignitegreenhouse.constants;

/**
 * Created by acel on 7/17/17.
 */

public class Constant {

    public static final boolean DEBUG = true;
    public static final String REGEXP_ID = "[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]";
    public static final int NUMBER_OF_CHARACTERS = 8;
    public static final String ADD_NEW_THING_TYPE = "addNewThingType";
    public static final String REMOVE_THING_TYPE = "removeThingType";
    public static final String GET_THING_CODE_STRING = "thingCode";
    public static final String REMOVE_THING_STRING = "removeThing";

    public static final String ADD_STATIC_THING_TYPE ="{\"addNewThingType\":[{\"thingCode\":\"01\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Temperature\",\"thingTypeString\":\"GHT-Temperature\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}},{\"thingCode\":\"02\",\"thingSpecific\":{\"thingId\":\"GH-DHT11-Humidity\",\"thingTypeString\":\"GHT-Humidity\",\"thingVendor\":\"Green House\",\"thingType\":\"Float\"}}]}";

    public static final String INTENT_FILTER_IGNITE_STATUS = "igniteConnect";
    public static final String INTENT_FILTER_CONFIG = "getConfig";
    public static final String IGNITE_STATUS_BROADCAST="igniteStatus";

    public static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";
}
