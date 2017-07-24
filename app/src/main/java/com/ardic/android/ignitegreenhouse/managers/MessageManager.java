package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.utils.NodeThingUtils;
import com.ardic.android.ignitegreenhouse.utils.SensorTypeUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;
    private SensorTypeUtils mSensorTypeUtils;
    private static MessageManager INSTANCE = null;

    private MessageManager(Context context) {
        mContext = context;
        if (context != null) {
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            mSensorTypeUtils = SensorTypeUtils.getInstance(mContext);
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
        if (receivedNode.equals(Constant.CONFIGURATION_NODE_NAME)) {
            if (receivedThing.equals(Constant.CONFIGURATION_THING_NAME)) {
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
                if (mGetConfigurationJson.has(Constant.ADD_NEW_NODE_THING_JSON_KEY)) {
                    NodeThingUtils.getInstance(mContext).parseAddJson(mGetConfigurationJson);
                }

                /**Used to reset the information on the whole device.
                 * The user will not be open.
                 */
                if (mGetConfigurationJson.has(Constant.REMOVE_ALL_DEVICE_JSON_KEY)) {
                    if (mGetConfigurationJson.getBoolean(Constant.REMOVE_ALL_DEVICE_JSON_KEY)) {
                        NodeThingUtils.getInstance(mContext).removeSavedAllThing();
                    }
                }

                /**Is used to delete the "node" - "thing" information recorded in the wrong format.
                 * The user will not be open.
                 */
                if (mGetConfigurationJson.has(Constant.REMOVE_THING_JSON_KEY)) {
                    JSONObject removeThing = mGetConfigurationJson.getJSONObject(Constant.REMOVE_THING_JSON_KEY);
                    removeThing.getString(Constant.NODE_ID_JSON_KEY);
                    NodeThingUtils.getInstance(mContext).removeSavedThing(removeThing.getString(Constant.NODE_ID_JSON_KEY), removeThing.getString(Constant.THING_LABEL_JSON_KEY));
                }

                /**Used to add new sensor type.*/
                if (mGetConfigurationJson.has(Constant.REMOVE_THING_TYPE)) {
                    mSensorTypeUtils.removeThingType(mGetConfigurationJson);
                }

                /**Used to remove sensor type.*/
                if (mGetConfigurationJson.has(Constant.ADD_NEW_THING_TYPE_JSON_KEY)) {
                    mSensorTypeUtils.addSensorType(mGetConfigurationJson);
                }
            }

        } catch (JSONException e) {
            try {
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.RESPONSE_ERROR,new JSONObject().put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY,Constant.RESPONSE_CREATE_MESSAGE_FORMAT_ERROR))));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
