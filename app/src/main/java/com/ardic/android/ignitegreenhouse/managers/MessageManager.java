package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.operations.SensorType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/7/17.
 */

public class MessageManager {

    private static final String TAG = MessageManager.class.getSimpleName();
    /**
     * Node - Thing Label - Thing Code used to save
     * This Format : KEY   = "NodeName:ThingLabel"
     * VALUE = "Thing Code"
     * Example     : "GreenHouse1:Temperature" - "f4030687"
     */
    private SharedPreferences sensors;
    private SharedPreferences.Editor sensorsEditor;

    /**
     * Statics :
     */

    /**
     * They declare the "key" values in the incoming "json" format
     */
    private static final String ADD_DEVICE_STRING = "addDevice";
    private static final String REMOVE_ALL_DEVICE_STRING = "removeAllDevice";
    private static final String REMOVE_THING_STRING = "removedThingResponse";
    private static final String REMOVE_ALL_COMPONENT_MESSAGE = "All Sensor - Type - Thread";

    private static final String GET_NODE_ID_STRING = "nodeId";
    private static final String GET_THINGS_STRING = "things";

    private static final String GET_THING_LABEL_STRING = "thingId";

    private static final String CONFIGURATION_NODE_STRING = "Configurator";
    private static final String CONFIGURATION_THING_STRING = "Configurator Thing";

    private static final String RESPONSE_CREATE_MESSAGE_STRING = "responseAddDevice";
    private static final String RESPONSE_CREATE_MESSAGE_NODE_STRING = "createNode";
    private static final String RESPONSE_CREATE_MESSAGE_THING_STRING = "createThing";
    private static final String RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_STRING = "descriptions";
    private static final String RESPONSE_CREATE_MESSAGE_DESCRIPTIONS = "The received data have already been registered. Please delete first !";

    /**
     * Returning notifications
     */
    private static final String RESPONSE_CREATE_MESSAGE_FORMAT_ERROR = "{\"error\":\"Error Format\"}";

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;
    private SensorType mSensorType;
    private static MessageManager INSTANCE = null;

    private MessageManager(Context context) {
        mContext = context;

        if (context != null) {
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);

            mSensorType = SensorType.getInstance(mContext);

            sensors = PreferenceManager.getDefaultSharedPreferences(mContext);
            sensorsEditor = sensors.edit();

            parseSensorJson(Constant.ADD_STATIC_THING_TYPE);
        }
    }

    public static synchronized MessageManager getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new MessageManager(context);
        }
        return INSTANCE;
    }

    /**
     * The value from "Configurator" checks if the "node" is in "Configurator Thing".
     * Send the incoming message to process
     */
    public void receivedConfigMessage(String receivedNode, String receivedThing, String receivedMessage) {
        if (receivedNode.equals(CONFIGURATION_NODE_STRING)) {
            if (receivedThing.equals(CONFIGURATION_THING_STRING)) {
                /**
                 * The incoming "node" and "thing" are checked.
                 * The data is being sent to the "parseSensorJson" function for processing
                 */
                parseSensorJson(receivedMessage);
            }
        }
    }


    /**
     * The "parseSensorJson" function classifies the incoming "Json" format
     * data according to the keys we specify and processes them according to the
     * data contained in them
     */
    private void parseSensorJson(String getAddJson) {
        try {
            /**Action message received as data*/
            JSONObject mGetConfigurationJson = new JSONObject(getAddJson);

            /**Empty control is performed. Checks if there is a parameter to
             * add a new "thing" in incoming data
             */
            if (mGetConfigurationJson != null) {
                if (mGetConfigurationJson.has(ADD_DEVICE_STRING)) {

                    /**The number of "nodes" that include the "things" to be added is taken here.*/
                    int getAddDataSize = mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).length();


                    for (int nodeNumber = 0; nodeNumber < getAddDataSize; nodeNumber++) {

                        /**Node Selected*/
                        JSONObject addDeviceArray = new JSONObject(String.valueOf(mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).get(nodeNumber)));

                        if (Constant.DEBUG) {
                            Log.e(TAG, "GET Node Id : " + addDeviceArray.getString(GET_NODE_ID_STRING));
                        }

                        /**The number of "things" under the selected node is taken here.*/
                        int getAddThingSize = addDeviceArray.getJSONArray(GET_THINGS_STRING).length();

                        for (int thingNumber = 0; thingNumber < getAddThingSize; thingNumber++) {

                            /**Thing Selected*/
                            JSONObject addThingArray = new JSONObject(String.valueOf(addDeviceArray.getJSONArray(GET_THINGS_STRING).get(thingNumber)));
                            if (Constant.DEBUG) {
                                Log.e(TAG, "GET Thing Code : " + addThingArray.getString(Constant.GET_THING_CODE_STRING));
                                Log.e(TAG, "GET Thing Label : " + addThingArray.getString(GET_THING_LABEL_STRING));
                            }
                            /**Send selected node and thing information to addJsonControl function*/
                            addJsonControl(addDeviceArray.getString(GET_NODE_ID_STRING), addThingArray.getString(Constant.GET_THING_CODE_STRING), addThingArray.getString(GET_THING_LABEL_STRING));
                        }
                    }
                }

                /**Used to reset the information on the whole device.
                 * The user will not be open.
                 */
                if (mGetConfigurationJson.has(REMOVE_ALL_DEVICE_STRING)) {
                    if (mGetConfigurationJson.getBoolean(REMOVE_ALL_DEVICE_STRING)) {
                        removeSavedAllThing();
                    }
                }

                /**Is used to delete the "node" - "thing" information recorded in the wrong format.
                 * The user will not be open.
                 */
                if (mGetConfigurationJson.has(Constant.REMOVE_THING_STRING)) {
                    JSONObject removeThing = mGetConfigurationJson.getJSONObject(Constant.REMOVE_THING_STRING);
                    removeThing.getString(GET_NODE_ID_STRING);
                    removeSavedThing(removeThing.getString(GET_NODE_ID_STRING), removeThing.getString(GET_THING_LABEL_STRING));
                }

                /**Used to add new sensor type.*/
                if (mGetConfigurationJson.has(Constant.REMOVE_THING_TYPE)) {
                    mSensorType.removeThingType(mGetConfigurationJson);
                }

                /**Used to remove sensor type.*/
                if (mGetConfigurationJson.has(Constant.ADD_NEW_THING_TYPE)) {
                    mSensorType.addSensorType(mGetConfigurationJson);
                }
            }

        } catch (JSONException e) {
            mIotIgniteHandler.sendConfiguratorThingMessage(RESPONSE_CREATE_MESSAGE_FORMAT_ERROR);
            e.printStackTrace();
        }
    }


    private void addJsonControl(String getNode, String getThing, String getThingLabel) {
        if (Constant.DEBUG) {
            Log.e(TAG, "Node : " + getNode +
                    "\nThing : " + getThing +
                    "\nThing Label : " + getThingLabel);
        }
        getNode = getNode.replace(" ", "");
        getThingLabel = getThingLabel.replace(" ", "");

        if (!sensors.contains(getNode + ":" + getThingLabel)) {
            sensorsEditor.putString(getNode + ":" + getThingLabel, getThing);
            sensorsEditor.commit();
            if (Constant.DEBUG) {
                Log.i(TAG, getNode + " Node has been added " + getThing + " with the number " + getThingLabel + " thing.");
            }
            mIotIgniteHandler.registerNode(getNode);
            mIotIgniteHandler.registerThing(getThingLabel);
            try {
                JSONObject returnCreateTrue = new JSONObject().put(RESPONSE_CREATE_MESSAGE_STRING, new JSONObject().put(RESPONSE_CREATE_MESSAGE_NODE_STRING, true).put(RESPONSE_CREATE_MESSAGE_THING_STRING, true));
                returnCreateTrue.put(GET_NODE_ID_STRING, getNode);
                returnCreateTrue.put(GET_THING_LABEL_STRING, getThingLabel);
                returnCreateTrue.put(GET_THING_LABEL_STRING, getThing);
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateTrue));
                DataManager.getInstance(mContext).threadManager();
                mIotIgniteHandler.updateListener();
            } catch (JSONException e) {
                mIotIgniteHandler.sendConfiguratorThingMessage(RESPONSE_CREATE_MESSAGE_FORMAT_ERROR);
                e.printStackTrace();
            }
        } else {

            try {
                JSONObject returnCreateFalse = new JSONObject().put(RESPONSE_CREATE_MESSAGE_STRING, new JSONObject()
                        .put(RESPONSE_CREATE_MESSAGE_NODE_STRING, false)
                        .put(RESPONSE_CREATE_MESSAGE_THING_STRING, false)
                        .put(RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_STRING, RESPONSE_CREATE_MESSAGE_DESCRIPTIONS));

                returnCreateFalse.put(GET_NODE_ID_STRING, getNode);
                returnCreateFalse.put(GET_THING_LABEL_STRING, getThingLabel);
                returnCreateFalse.put(GET_THING_LABEL_STRING, getThing);

                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateFalse));

            } catch (JSONException e) {
                mIotIgniteHandler.sendConfiguratorThingMessage(RESPONSE_CREATE_MESSAGE_FORMAT_ERROR);
                e.printStackTrace();
            }
            if (Constant.DEBUG) {
                Log.i(TAG, "The received thing have already been registered. Please delete first !");
            }
        }
    }


    /**
     * Thing Control
     */
    public String getSavedThing(String getNode, String getThingLabel) {
        return sensors.getString(getNode + ":" + getThingLabel, Constant.PREFERENCES_ADD_SENSOR_NOT_GET);

    }

    public Map<String, ?> getSavedAllThing() {
        return sensors.getAll();
    }

    public void removeSavedThing(String getNode, String getThingLabel) {
        if (!TextUtils.isEmpty(getNode) && !TextUtils.isEmpty(getThingLabel) && sensors.contains(getNode + ":" + getThingLabel)) {

            sensorsEditor.remove(getNode + ":" + getThingLabel);
            sensorsEditor.commit();

            DataManager.getInstance(mContext).killThread(getNode, getThingLabel);

            if (Constant.DEBUG) {
                Log.e(TAG, "Removed Node : " + getNode +
                        "\nThing : " + getThingLabel);
            }
            try {
                JSONObject removeThingJson = new JSONObject().put(REMOVE_THING_STRING, new JSONObject()
                        .put(GET_NODE_ID_STRING, getNode)
                        .put(GET_THING_LABEL_STRING, getThingLabel));
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeThingJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




    private void removeSavedAllThing() {
        DataManager.getInstance(mContext).killAllThread();

        sensorsEditor.clear();
        sensorsEditor.commit();
        mIotIgniteHandler.clearAllThing();

        if (Constant.DEBUG) {
            Log.e(TAG, "Removed All Saved ...");
        }
        try {
            JSONObject removeAllThing = new JSONObject().put(REMOVE_THING_STRING, REMOVE_ALL_COMPONENT_MESSAGE);
            mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeAllThing));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public String[] getThingID(String thingCode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();
        int counter = 0;
        String[] keysOther = new String[sensors.getAll().size()];
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) getAllSensor.get(key);
            if (value.equals(thingCode)) {
                keysOther[counter] = key;
                counter++;
            }
        }
        if (counter == 1) {
            return keysOther;
        } else {
            return keysOther;
        }

    }

    public String getSensorNode(String keyAll) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();

        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(keyAll)) {
                return key;
            }
        }
        return null;
    }

    // todo : unutma bunu
    public void removeSavedNode(String getNode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(getNode)) {
                sensorsEditor.remove(key);
                sensorsEditor.commit();
                if (Constant.DEBUG) {
                    Log.e(TAG, "Removed Node : " + getNode);
                }
                mIotIgniteHandler.sendConfiguratorThingMessage("{\"removedNodeResponse\":\"" + getNode + "\"}");
            }
        }
    }
}
