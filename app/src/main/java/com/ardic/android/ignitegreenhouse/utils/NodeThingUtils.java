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
 * Created by Mert Acel on 7/20/17.
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
            if (sensors != null) {
                sensorsEditor = sensors.edit();
            }
        }
    }

    public static synchronized NodeThingUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new NodeThingUtils(context);
        }
        return INSTANCE;
    }

    /**
     * The incoming value content is checked.
     * It sends the data for the "addJsonControl" function for
     * recording after it is recognized
     */
    public void parseRegisterThing(JSONObject getAddJson) {
        try {
            if (getAddJson != null && getAddJson.has(Constant.NodeThing.MESSAGE_ID)) {
                String getMessageId = getAddJson.getString(Constant.NodeThing.MESSAGE_ID);

                LogUtils.logger(TAG, "Message Key : " + getMessageId);

                if (getAddJson != null && getAddJson.has(Constant.NodeThing.ADD_NEW_NODE_THING)) {
                    /**The number of "nodes" that include the "things" to be added is taken here.*/
                    int getAddDataSize = getAddJson.getJSONArray(Constant.NodeThing.ADD_NEW_NODE_THING).length();

                    for (int nodeNumber = 0; nodeNumber < getAddDataSize; nodeNumber++) {

                        if (getAddJson != null && getAddJson.has(Constant.NodeThing.ADD_NEW_NODE_THING)) {
                            /**Node Selected*/
                            JSONObject addDeviceArray = new JSONObject(String.valueOf(getAddJson.getJSONArray(Constant.NodeThing.ADD_NEW_NODE_THING).get(nodeNumber)));
                            LogUtils.logger(TAG, "GET Node Id : " + addDeviceArray.getString(Constant.NodeThing.NODE_ID));

                            /**The number of "things" under the selected node is taken here.*/

                            if (addDeviceArray != null && addDeviceArray.has(Constant.NodeThing.THINGS_ARRAY)) {
                                int getAddThingSize = addDeviceArray.getJSONArray(Constant.NodeThing.THINGS_ARRAY).length();

                                for (int thingNumber = 0; thingNumber < getAddThingSize; thingNumber++) {
                                    /**Thing Selected*/
                                    JSONObject addThingArray = new JSONObject(String.valueOf(addDeviceArray.getJSONArray(Constant.NodeThing.THINGS_ARRAY).get(thingNumber)));

                                    LogUtils.logger(TAG, "GET Thing Code : " + addThingArray.getString(Constant.NodeThing.THING_CODE));
                                    LogUtils.logger(TAG, "GET Thing Label : " + addThingArray.getString(Constant.NodeThing.THING_LABEL));

                                    /**Send selected node and thing information to addJsonControl function*/
                                    addJsonControl(addDeviceArray.getString(Constant.NodeThing.NODE_ID), addThingArray.getString(Constant.NodeThing.THING_CODE), addThingArray.getString(Constant.NodeThing.THING_LABEL), getMessageId);
                                }
                            }
                        }
                    }
                }
            } else {
                JSONObject nullMessageJsonObject = new JSONObject();
                nullMessageJsonObject.put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.NULL_MESSAGE_ID);

                JSONObject createSensorJsonObject = new JSONObject();
                createSensorJsonObject.put(Constant.ResponseJsonKey.CREATE_SENSOR, nullMessageJsonObject);

                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(createSensorJsonObject));
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseRegisterThing Error : " + e);
        }
    }

    /**
     * RegisterNode "and" registerThing "functions found in the
     * " IotIgniteHandler "class to register" node "and" thing "
     */
    private void addJsonControl(String getNode, String getThing, String getThingLabel, String messageId) {
        if (!TextUtils.isEmpty(getNode) && !TextUtils.isEmpty(getThing) && !TextUtils.isEmpty(getThingLabel) && !TextUtils.isEmpty(messageId)) {

            LogUtils.logger(TAG, "Node : " + getNode +
                    "\nThing : " + getThing +
                    "\nThing Label : " + getThingLabel);

            String getNodeNotSpace = getNode.replace(" ", "");
            String getThingLabelNotSpace = getThingLabel.replace(" ", "");

            if (sensors != null && sensorsEditor != null && !sensors.contains(getNodeNotSpace + ":" + getThingLabelNotSpace) && DynamicSensorTypeUtils.getInstance(mContext).getSensorTypeByCode(getThing).length > 0) {
                sensorsEditor.putString(getNodeNotSpace + ":" + getThingLabelNotSpace, getThing);
                sensorsEditor.commit();
                LogUtils.logger(TAG, getNodeNotSpace + " Node has been added " + getThing + " with the number " + getThingLabelNotSpace + " thing.");

                mIotIgniteHandler.registerNode(getNodeNotSpace);
                mIotIgniteHandler.registerThing(getThingLabelNotSpace);
                try {
                    JSONObject responseCreate = new JSONObject().put(Constant.ResponseJsonKey.CREATE_SENSOR, new JSONObject().put(Constant.ResponseJsonKey.CREATE_NODE, true).put(Constant.ResponseJsonKey.CREATE_THING, true));
                    responseCreate.put(Constant.NodeThing.NODE_ID, getNodeNotSpace);
                    responseCreate.put(Constant.NodeThing.THING_LABEL, getThingLabelNotSpace);
                    responseCreate.put(Constant.NodeThing.THING_CODE, getThing);
                    responseCreate.put(Constant.NodeThing.MESSAGE_ID, messageId);

                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreate));
                    ThreadManager.getInstance(mContext).threadManager();
                    mIotIgniteHandler.updateListener();
                } catch (JSONException e) {
                    try {
                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.ResponseJsonKey.ERROR, new JSONObject()
                                .put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.CREATE_MESSAGE_FORMAT_ERROR)
                                .put(Constant.NodeThing.MESSAGE_ID, messageId))));
                    } catch (JSONException e1) {
                        Log.e(TAG, "sendConfiguratorThingMessage Json Error : " + e1);
                    }
                    Log.e(TAG, "returnCreateTrue Json Error : " + e);
                }
            } else {
                try {
                    if (sensors != null && sensors.contains(getNodeNotSpace + ":" + getThingLabelNotSpace)) {
                        JSONObject returnCreateFalse = new JSONObject().put(Constant.ResponseJsonKey.CREATE_SENSOR, new JSONObject()
                                .put(Constant.ResponseJsonKey.CREATE_NODE, false)
                                .put(Constant.ResponseJsonKey.CREATE_THING, false)
                                .put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.CREATE_MESSAGE_DESCRIPTIONS_BEEN_REGISTERED));

                        returnCreateFalse.put(Constant.NodeThing.NODE_ID, getNodeNotSpace);
                        returnCreateFalse.put(Constant.NodeThing.THING_LABEL, getThingLabelNotSpace);
                        returnCreateFalse.put(Constant.NodeThing.THING_LABEL, getThing);
                        returnCreateFalse.put(Constant.NodeThing.MESSAGE_ID, messageId);

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateFalse));
                    }
                    if (DynamicSensorTypeUtils.getInstance(mContext).getSensorTypeByCode(getThing).length > 0) {
                        JSONObject returnCreateFalse = new JSONObject().put(Constant.ResponseJsonKey.CREATE_SENSOR, new JSONObject()
                                .put(Constant.ResponseJsonKey.CREATE_NODE, false)
                                .put(Constant.ResponseJsonKey.CREATE_THING, false)
                                .put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.CREATE_MESSAGE_DESCRIPTIONS_SENSOR_TYPE));

                        returnCreateFalse.put(Constant.NodeThing.NODE_ID, getNodeNotSpace);
                        returnCreateFalse.put(Constant.NodeThing.THING_LABEL, getThingLabelNotSpace);
                        returnCreateFalse.put(Constant.NodeThing.THING_LABEL, getThing);
                        returnCreateFalse.put(Constant.NodeThing.MESSAGE_ID, messageId);

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(returnCreateFalse));
                    }
                } catch (JSONException e) {
                    try {
                        JSONObject descriptionsJsonObject = new JSONObject();
                        descriptionsJsonObject.put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.CREATE_MESSAGE_FORMAT_ERROR);
                        descriptionsJsonObject.put(Constant.NodeThing.MESSAGE_ID, messageId);

                        JSONObject errorJsonObject = new JSONObject();
                        errorJsonObject.put(Constant.ResponseJsonKey.ERROR, descriptionsJsonObject);

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(errorJsonObject));

                    } catch (JSONException e1) {
                        Log.e(TAG, "sendConfiguratorThingMessage Json Error : " + e1);
                    }
                    Log.e(TAG, "returnCreateFalse Json Error : " + e);
                }

                LogUtils.logger(TAG, "The received thing have already been registered. Please delete first !");

            }
        }
    }


    /**
     * "Node" and "thing" get the name and return the record in that information
     */
    public String getSavedThing(String getNode, String getThingLabel) {
        if (sensors != null) {
            return sensors.getString(getNode + ":" + getThingLabel, Constant.PREFERENCES_ADD_SENSOR_NOT_GET);
        }
        return null;
    }

    /**
     * Return all saved data
     */
    public Map<String, String> getAllSavedThing() {
        if (sensors != null) {
            return (Map<String, String>) sensors.getAll();
        }
        return null;
    }


    /**
     * "Node" and "thing" take the name. Delete "thing"
     */
    public void removeSavedThing(String getNode, String getThingLabel) {
        if (sensors != null && sensorsEditor != null && !TextUtils.isEmpty(getNode) && !TextUtils.isEmpty(getThingLabel) && sensors.contains(getNode + ":" + getThingLabel)) {

            sensorsEditor.remove(getNode + ":" + getThingLabel);
            sensorsEditor.commit();

            ThreadManager.getInstance(mContext).killThread(getNode, getThingLabel);

            mIotIgniteHandler.unRegisterThing(getNode, getThingLabel);


            LogUtils.logger(TAG, "Removed Node : " + getNode +
                    "\nThing : " + getThingLabel);

            try {
                JSONObject removeThingJson = new JSONObject().put(Constant.ResponseJsonKey.REMOVE_THING, new JSONObject()
                        .put(Constant.NodeThing.NODE_ID, getNode)
                        .put(Constant.NodeThing.THING_LABEL, getThingLabel)
                        .put(Constant.NodeThing.MESSAGE_ID, ""));
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeThingJson));
            } catch (JSONException e) {
                Log.e(TAG, "removeThingJson Json Error : " + e);
            }
        }
    }

    /**
     * Delete all registered information
     */
    public void removeAllSavedThings() {
        if (sensorsEditor != null) {
            ThreadManager.getInstance(mContext).clearThreadControlList();
            sensorsEditor.clear();
            sensorsEditor.commit();
            mIotIgniteHandler.clearAllThing();

            LogUtils.logger(TAG, "Removed All Saved ...");

            try {
                JSONObject removeAllThing = new JSONObject()
                        .put(Constant.ResponseJsonKey.REMOVE_THING, Constant.ResponseJsonValue.REMOVE_ALL_COMPONENT)
                        .put(Constant.NodeThing.MESSAGE_ID, "");
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeAllThing));
            } catch (JSONException e) {
                Log.e(TAG, "removeAllThing Json Error : " + e);
            }
        }
    }

    public String[] getThingIdByCode(String thingCode) {
        if(sensors!=null) {
            Map<String, String> getAllSensor = (Map<String, String>) sensors.getAll();
            Set keys = getAllSensor.keySet();
            int counter = 0;
            String[] keysOther = new String[sensors.getAll().size()];
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String value = String.valueOf(getAllSensor.get(String.valueOf(key)));
                if (value.equals(thingCode)) {
                    keysOther[counter] = key;
                    counter++;
                }
            }
            return keysOther;
        }
        return null;
    }

    /**
     * Delete the registered "node" and the "things" in it
     */
    public void removeSavedNode(String getNode) {
        if (sensors != null && sensorsEditor!=null) {
            Map<String, String> getAllSensor = (Map<String, String>) sensors.getAll();
            Set keys = getAllSensor.keySet();
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String[] split = key.split(":");
                if (split[0].equals(getNode)) {
                    sensorsEditor.remove(key);
                    sensorsEditor.commit();
                    if (ThreadManager.getInstance(mContext).isThreadAvailable(key)) {
                        ThreadManager.getInstance(mContext).killThread(split[0], split[1]);
                    }

                    LogUtils.logger(TAG, "Removed Node : " + getNode);

                    try {
                        JSONObject removeNodeJsonObject = new JSONObject();
                        removeNodeJsonObject.put(Constant.ResponseJsonKey.REMOVE_NODE, getNode);
                        removeNodeJsonObject.put(Constant.NodeThing.MESSAGE_ID, "");

                        mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(removeNodeJsonObject));
                    } catch (JSONException e) {
                        Log.e(TAG, "sendConfiguratorThingMessage Error : " + e);
                    }
                }
            }
        }else{
            Log.e(TAG,"Sensor Preference Null!");
        }
    }
}
