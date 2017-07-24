package com.ardic.android.ignitegreenhouse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.managers.ThreadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/20/17.
 */

public class NodeThingUtils {
    private static final String TAG = NodeThingUtils.class.getSimpleName();

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;

    private SharedPreferences sensors;
    private SharedPreferences.Editor sensorsEditor;

    private static NodeThingUtils INSTANCE = null;

    private NodeThingUtils(Context context) {
        mContext = context;
        if (context != null) {
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            sensors = PreferenceManager.getDefaultSharedPreferences(mContext);
            sensorsEditor = sensors.edit();
        }
    }

    public static synchronized NodeThingUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new NodeThingUtils(context);
        }
        return INSTANCE;
    }

    public void parseAddJson(JSONObject getAddJson) {
        try {

            if (getAddJson.has(Constant.MESSAGE_ID_JSON_KEY)) {
                String getMessageId = getAddJson.getString(Constant.MESSAGE_ID_JSON_KEY);
                if (Constant.DEBUG) {
                    Log.e(TAG, "Message Key : " + getMessageId);
                }

                /**The number of "nodes" that include the "things" to be added is taken here.*/
                int getAddDataSize = getAddJson.getJSONArray(Constant.ADD_NEW_NODE_THING_JSON_KEY).length();
                for (int nodeNumber = 0; nodeNumber < getAddDataSize; nodeNumber++) {

                    /**Node Selected*/
                    JSONObject addDeviceArray = new JSONObject(String.valueOf(getAddJson.getJSONArray(Constant.ADD_NEW_NODE_THING_JSON_KEY).get(nodeNumber)));

                    if (Constant.DEBUG) {
                        Log.e(TAG, "GET Node Id : " + addDeviceArray.getString(Constant.NODE_ID_JSON_KEY));
                    }

                    /**The number of "things" under the selected node is taken here.*/
                    int getAddThingSize = addDeviceArray.getJSONArray(Constant.THINGS_ARRAY_JSON_KEY).length();

                    for (int thingNumber = 0; thingNumber < getAddThingSize; thingNumber++) {

                        /**Thing Selected*/
                        JSONObject addThingArray = new JSONObject(String.valueOf(addDeviceArray.getJSONArray(Constant.THINGS_ARRAY_JSON_KEY).get(thingNumber)));
                        if (Constant.DEBUG) {
                            Log.e(TAG, "GET Thing Code : " + addThingArray.getString(Constant.THING_CODE_JSON_KEY));
                            Log.e(TAG, "GET Thing Label : " + addThingArray.getString(Constant.THING_LABEL_JSON_KEY));
                        }
                        /**Send selected node and thing information to addJsonControl function*/
                        addJsonControl(addDeviceArray.getString(Constant.NODE_ID_JSON_KEY), addThingArray.getString(Constant.THING_CODE_JSON_KEY), addThingArray.getString(Constant.THING_LABEL_JSON_KEY), getMessageId);
                    }
                }
            } else {
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject()
                        .put(Constant.RESPONSE_CREATE_SENSOR_JSON_KEY, new JSONObject()
                                .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY, Constant.RESPONSE_NULL_MESSAGE_ID_JSON_VALUE))));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void addJsonControl(String getNode, String getThing, String getThingLabel, String messageId) {
        if (!TextUtils.isEmpty(getNode) && !TextUtils.isEmpty(getThing) && !TextUtils.isEmpty(getThingLabel) && !TextUtils.isEmpty(messageId)) {
            if (Constant.DEBUG) {
                Log.e(TAG, "Node : " + getNode +
                        "\nThing : " + getThing +
                        "\nThing Label : " + getThingLabel);
            }
            getNode = getNode.replace(" ", "");
            getThingLabel = getThingLabel.replace(" ", "");

            if (!sensors.contains(getNode + ":" + getThingLabel) && SensorTypeUtils.getInstance(mContext).getSensorType(getThing) != null) {
                sensorsEditor.putString(getNode + ":" + getThingLabel, getThing);
                sensorsEditor.commit();
                if (Constant.DEBUG) {
                    Log.i(TAG, getNode + " Node has been added " + getThing + " with the number " + getThingLabel + " thing.");
                }
                mIotIgniteHandler.registerNode(getNode);
                mIotIgniteHandler.registerThing(getThingLabel);
                try {
                    JSONObject returnCreateTrue = new JSONObject().put(Constant.RESPONSE_CREATE_SENSOR_JSON_KEY, new JSONObject().put(Constant.RESPONSE_CREATE_NODE_JSON_KEY, true).put(Constant.RESPONSE_CREATE_THING_JSON_KEY, true));
                    returnCreateTrue.put(Constant.NODE_ID_JSON_KEY, getNode);
                    returnCreateTrue.put(Constant.THING_LABEL_JSON_KEY, getThingLabel);
                    returnCreateTrue.put(Constant.THING_CODE_JSON_KEY, getThing);
                    returnCreateTrue.put(Constant.MESSAGE_ID_JSON_KEY, messageId);

                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateTrue));
                    ThreadManager.getInstance(mContext).threadManager();
                    mIotIgniteHandler.updateListener();
                } catch (JSONException e) {
                    try {
                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.RESPONSE_ERROR,new JSONObject()
                                .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY,Constant.RESPONSE_CREATE_MESSAGE_FORMAT_ERROR)
                                .put(Constant.MESSAGE_ID_JSON_KEY, messageId))));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            } else {
                try {
                    if (sensors.contains(getNode + ":" + getThingLabel)) {
                        JSONObject returnCreateFalse = new JSONObject().put(Constant.RESPONSE_CREATE_SENSOR_JSON_KEY, new JSONObject()
                                .put(Constant.RESPONSE_CREATE_NODE_JSON_KEY, false)
                                .put(Constant.RESPONSE_CREATE_THING_JSON_KEY, false)
                                .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY, Constant.RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_BEEN_REGISTERED_JSON_VALUE));

                        returnCreateFalse.put(Constant.NODE_ID_JSON_KEY, getNode);
                        returnCreateFalse.put(Constant.THING_LABEL_JSON_KEY, getThingLabel);
                        returnCreateFalse.put(Constant.THING_LABEL_JSON_KEY, getThing);
                        returnCreateFalse.put(Constant.MESSAGE_ID_JSON_KEY, messageId);

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateFalse));
                    }
                    if (SensorTypeUtils.getInstance(mContext).getSensorType(getThing) == null) {
                        JSONObject returnCreateFalse = new JSONObject().put(Constant.RESPONSE_CREATE_SENSOR_JSON_KEY, new JSONObject()
                                .put(Constant.RESPONSE_CREATE_NODE_JSON_KEY, false)
                                .put(Constant.RESPONSE_CREATE_THING_JSON_KEY, false)
                                .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY, Constant.RESPONSE_CREATE_MESSAGE_DESCRIPTIONS_SENSOR_TYPE_JSON_VALUE));

                        returnCreateFalse.put(Constant.NODE_ID_JSON_KEY, getNode);
                        returnCreateFalse.put(Constant.THING_LABEL_JSON_KEY, getThingLabel);
                        returnCreateFalse.put(Constant.THING_LABEL_JSON_KEY, getThing);
                        returnCreateFalse.put(Constant.MESSAGE_ID_JSON_KEY, messageId);

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateFalse));
                    }
                } catch (JSONException e) {
                    try {
                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.RESPONSE_ERROR,new JSONObject()
                                .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY,Constant.RESPONSE_CREATE_MESSAGE_FORMAT_ERROR)
                                .put(Constant.MESSAGE_ID_JSON_KEY, messageId))));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                if (Constant.DEBUG) {
                    Log.i(TAG, "The received thing have already been registered. Please delete first !");
                }
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

            ThreadManager.getInstance(mContext).killThread(getNode, getThingLabel);

            mIotIgniteHandler.unRegister(getNode, getThingLabel);

            if (Constant.DEBUG) {
                Log.e(TAG, "Removed Node : " + getNode +
                        "\nThing : " + getThingLabel);
            }
            try {
                JSONObject removeThingJson = new JSONObject().put(Constant.RESPONSE_REMOVE_THING_JSON_KEY, new JSONObject()
                        .put(Constant.NODE_ID_JSON_KEY, getNode)
                        .put(Constant.THING_LABEL_JSON_KEY, getThingLabel)
                        .put(Constant.MESSAGE_ID_JSON_KEY, ""));
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeThingJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void removeSavedAllThing() {
        ThreadManager.getInstance(mContext).killAllThread();

        sensorsEditor.clear();
        sensorsEditor.commit();
        mIotIgniteHandler.clearAllThing();

        if (Constant.DEBUG) {
            Log.e(TAG, "Removed All Saved ...");
        }
        try {
            JSONObject removeAllThing = new JSONObject()
                    .put(Constant.RESPONSE_REMOVE_THING_JSON_KEY, Constant.RESPONSE_REMOVE_ALL_COMPONENT_JSON_VALUE)
                    .put(Constant.MESSAGE_ID_JSON_KEY, "");
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

    // todo : burada unutma
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

    public void removeSavedNode(String getNode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(getNode)) {
                sensorsEditor.remove(key);
                sensorsEditor.commit();
                if (ThreadManager.getInstance(mContext).isThread(key)) {
                    ThreadManager.getInstance(mContext).killThread(split[0], split[1]);
                }
                if (Constant.DEBUG) {
                    Log.e(TAG, "Removed Node : " + getNode);
                }
                try {
                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject()
                            .put(Constant.RESPONSE_REMOVE_NODE_JSON_KEY, getNode)
                            .put(Constant.MESSAGE_ID_JSON_KEY, "")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
