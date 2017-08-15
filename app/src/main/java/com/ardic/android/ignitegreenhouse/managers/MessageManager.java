package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.utils.DynamicSensorTypeUtils;
import com.ardic.android.ignitegreenhouse.utils.NodeThingUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mert Acel on 7/7/17.
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
    private DynamicSensorTypeUtils mDynamicSensorTypeUtils;
    private static MessageManager INSTANCE = null;

    private MessageManager(Context context) {
        mContext = context;
        if (context != null) {
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            mDynamicSensorTypeUtils = DynamicSensorTypeUtils.getInstance(mContext);
            parseThingJson(Constant.ThingType.ADD_STATIC_THING_TYPE);
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
        if (checkParameters(receivedNode, receivedThing, receivedMessage) &&
                Constant.Configurator.NODE_NAME.equals(receivedNode) &&
                Constant.Configurator.THING_NAME.equals(receivedThing)) {
            /**
             * The incoming "node" and "thing" are checked.
             * The data is being sent to the "parseThingJson" function for processing
             */
            parseThingJson(receivedMessage);

        }
    }

    private boolean checkParameters(String receivedNode, String receivedThing, String receivedMessage) {
        if (!TextUtils.isEmpty(receivedNode) && !TextUtils.isEmpty(receivedNode) && !TextUtils.isEmpty(receivedNode)) {
            return true;
        }
        return false;
    }

    /**
     * The "parseThingJson" function classifies the incoming "Json" format
     * data according to the keys we specify and processes them according to the
     * data contained in them
     */
    private void parseThingJson(String parseThingJson) {
        try {
            /**Action message received as data*/
            JSONObject mGetConfigurationJson = new JSONObject(parseThingJson);

            /**Empty control is performed. Checks if there is a parameter to
             * add a new "thing" in incoming data
             */
            if (mGetConfigurationJson != null) {
                if (mGetConfigurationJson.has(Constant.NodeThing.ADD_NEW_NODE_THING)) {
                    NodeThingUtils.getInstance(mContext).parseRegisterThing(mGetConfigurationJson);
                }

                /**Used to reset the information on the whole device.
                 * The user will not be open.
                 */
                if (mGetConfigurationJson.has(Constant.NodeThing.REMOVE_ALL_DEVICE) && mGetConfigurationJson.getBoolean(Constant.NodeThing.REMOVE_ALL_DEVICE)) {
                    NodeThingUtils.getInstance(mContext).removeAllSavedThings();
                }

                /**Is used to delete the "node" - "thing" information recorded in the wrong format.
                 * The user will not be open.
                 *///todo göster
                if (mGetConfigurationJson.has(Constant.NodeThing.REMOVE_THING)) {
                    JSONObject removeThing = mGetConfigurationJson.getJSONObject(Constant.NodeThing.REMOVE_THING);
                    if (removeThing.has(Constant.NodeThing.NODE_ID)) {
                        removeThing.getString(Constant.NodeThing.NODE_ID);
                        NodeThingUtils.getInstance(mContext).removeSavedThing(removeThing.getString(Constant.NodeThing.NODE_ID), removeThing.getString(Constant.NodeThing.THING_LABEL));
                    }
                }

                /**Used to add new sensor type.*/
                if (mGetConfigurationJson.has(Constant.ThingType.REMOVE_THING_TYPE)) {
                    mDynamicSensorTypeUtils.removeThingType(mGetConfigurationJson);
                }

                /**Used to remove sensor type.*/
                if (mGetConfigurationJson.has(Constant.ThingType.ADD_NEW_THING_TYPE)) {
                    mDynamicSensorTypeUtils.addSensorType(mGetConfigurationJson);
                }
            }

        } catch (JSONException e) {
            try {
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.ResponseJsonKey.ERROR, new JSONObject().put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.CREATE_MESSAGE_FORMAT_ERROR))));
            } catch (JSONException e1) {
                Log.e(TAG, "sendConfiguratorThingMessage Json Error : " + e1);
            }
            Log.e(TAG, "parseThingJson Error : " + e);
        }
    }
}
