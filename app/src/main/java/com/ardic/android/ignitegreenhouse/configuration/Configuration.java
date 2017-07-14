package com.ardic.android.ignitegreenhouse.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.managers.DataManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/7/17.
 */

public class Configuration {

    private static final String TAG = Configuration.class.getSimpleName();
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
    private static final String REMOVE_DEVICE_STRING = "removeAllDevice";
    private static final String GET_NODE_ID_STRING = "nodeId";
    private static final String GET_THINGS_STRING = "things";
    private static final String GET_THING_CODE_STRING = "thingCode";
    private static final String GET_THING_LABEL_STRING = "thingId";

    private static final String CONFIGURATION_NODE_STRING = "Configurator";
    private static final String CONFIGURATION_THING_STRING = "Configurator Thing";

    /**
     * Returning notifications
     */
    private static final String RESPONSE_CREATE_MESSAGE_TRUE = "{\"responseAddDevice\":{\"createNode\":true,\"createThing\":true}}";
    private static final String RESPONSE_CREATE_MESSAGE_FALSE = "{\"responseAddDevice\": {\"createNode\": false,\"createThing\": false,\"descriptions\":\"The received data have already been registered. Please delete first !\"}}";

    public static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;


    private static Configuration INSTANCE = null;

    private Configuration(Context context) {
        mContext = context;

        if (context != null) {
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            sensors = PreferenceManager.getDefaultSharedPreferences(mContext);
            sensorsEditor = sensors.edit();
        }
    }

    public static synchronized Configuration getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new Configuration(context);
        }
        return INSTANCE;
    }

    public void receivedConfigMessage(String recivedNode, String recivedThing, String receivedMessage) {
        if (recivedNode.equals(CONFIGURATION_NODE_STRING)) {
            if (recivedThing.equals(CONFIGURATION_THING_STRING)) {
                addSensorJson(receivedMessage);
            }
        }
    }


    /**
     * For create new Node and Thing
     */
    private void addSensorJson(String getAddJson) {
        try {
            JSONObject mGetConfigurationJson = new JSONObject(getAddJson);

            if (mGetConfigurationJson != null && mGetConfigurationJson.has(ADD_DEVICE_STRING)) {

                int getAddDataSize = mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).length();

                for (int nodeNumber = 0; nodeNumber < getAddDataSize; nodeNumber++) {
                    JSONObject addDeviceArray = new JSONObject(String.valueOf(mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).get(nodeNumber)));
                    Log.e(TAG, "GET Node Id : " + addDeviceArray.getString(GET_NODE_ID_STRING));
                    int getAddThingSize = addDeviceArray.getJSONArray(GET_THINGS_STRING).length();

                    for (int thingNumber = 0; thingNumber < getAddThingSize; thingNumber++) {
                        JSONObject addThingArray = new JSONObject(String.valueOf(addDeviceArray.getJSONArray(GET_THINGS_STRING).get(thingNumber)));
                        Log.e(TAG, "GET Thing Code : " + addThingArray.getString(GET_THING_CODE_STRING));
                        Log.e(TAG, "GET Thing Label : " + addThingArray.getString(GET_THING_LABEL_STRING));
                        addJsonControl(addDeviceArray.getString(GET_NODE_ID_STRING), addThingArray.getString(GET_THING_CODE_STRING), addThingArray.getString(GET_THING_LABEL_STRING));
                    }
                }
            }
            //todo : remove yap
            if (mGetConfigurationJson != null && mGetConfigurationJson.has(REMOVE_DEVICE_STRING)) {

                if (mGetConfigurationJson.getBoolean(REMOVE_DEVICE_STRING)) {
                    removeSavedAllDevices();
                }
            }

        } catch (JSONException e) {
            mIotIgniteHandler.sendConfiguratorThingMessage("{\"error\":\"Error Format\"}");
            e.printStackTrace();
        }
    }


    private void addJsonControl(String getNode, String getThing, String getThingLabel) {
        Log.e(TAG, "Node : " + getNode +
                "\nThing : " + getThing +
                "\nThing Label : " + getThingLabel);

        getNode = getNode.replace(" ", "");
        getThingLabel = getThingLabel.replace(" ", "");

        if (!sensors.contains(getNode + ":" + getThingLabel)) {
            sensorsEditor.putString(getNode + ":" + getThingLabel, getThing);
            sensorsEditor.commit();
            Log.i(TAG, getNode + " Node has been added " + getThing + " with the number " + getThingLabel + " thing.");
            mIotIgniteHandler.registerNode(getNode);
            mIotIgniteHandler.registerThing(getThingLabel, "Seratonin", "Seraton", "d");
            try {
                JSONObject returnCreateTrue = new JSONObject(RESPONSE_CREATE_MESSAGE_TRUE);
                returnCreateTrue.put("Node", getNode);
                returnCreateTrue.put("Thing", getThingLabel);
                returnCreateTrue.put("Thing Code", getThing);
                mIotIgniteHandler.sendConfiguratorThingMessage(returnCreateTrue.toString());
                DataManager.getInstance(mContext).threadManager();
                mIotIgniteHandler.updateListener();
            } catch (JSONException e) {
                mIotIgniteHandler.sendConfiguratorThingMessage("{\"error\":\"Error Format\"");
                e.printStackTrace();
            }
        } else {

            try {
                JSONObject returnCreateFalse = new JSONObject(RESPONSE_CREATE_MESSAGE_FALSE);
                returnCreateFalse.put("Node", getNode);
                returnCreateFalse.put("Thing", getThingLabel);
                returnCreateFalse.put("Thing Code", getThing);
                mIotIgniteHandler.sendConfiguratorThingMessage(returnCreateFalse.toString());
            } catch (JSONException e) {
                mIotIgniteHandler.sendConfiguratorThingMessage("{\"error\":\"Error Format\"");
                e.printStackTrace();
            }
            Log.i(TAG, "The received thing have already been registered. Please delete first !");
        }
    }


    public String getSavedDevices(String getNode, String getThingLabel) {
        return sensors.getString(getNode + ":" + getThingLabel, PREFERENCES_ADD_SENSOR_NOT_GET);

    }

    public Map<String, ?> getSavedAllDevices() {
        return sensors.getAll();
    }

    public void removeSavedThing(String getNode, String getThingLabel) {
        sensorsEditor.remove(getNode + ":" + getThingLabel);
        sensorsEditor.commit();
        // todo : debug true yaz
        Log.e(TAG, "Removed Node : " + getNode +
                "\nThing : " + getThingLabel);
        mIotIgniteHandler.sendConfiguratorThingMessage("{\"removedThingResponse\":{\"node\":\"" + getNode + "\",\"thing\":\"" + getThingLabel + "\"}}");
    }

    public void removeSavedNode(String getNode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(getNode)) {
                sensorsEditor.remove(key);
                sensorsEditor.commit();
                Log.e(TAG, "Removed Node : " + getNode);
                mIotIgniteHandler.sendConfiguratorThingMessage("{\"removedNodeResponse\":\"" + getNode + "\"}");
            }
        }
    }

    public void removeSavedAllDevices() {
        DataManager.getInstance(mContext).killAllThread();
        sensorsEditor.clear();
        sensorsEditor.commit();
        mIotIgniteHandler.clearAllThing();

        Log.e(TAG, "Removed All Saved ...");
    }

    public String[] getDeviceCodeThing(String deviceCode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();
        int counter = 0;
        String[] keysOther = new String[sensors.getAll().size()];
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) getAllSensor.get(key);
            if (value.equals(deviceCode)) {
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

    public String getDeviceNodeKey(String keyNode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();

        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(keyNode)) {
                return key;
            }
        }
        return null;
    }
}
