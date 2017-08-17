package com.ardic.android.ignitegreenhouse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mert Acel on 7/14/17.
 */

public class DynamicSensorTypeUtils {

    private static final String TAG = DynamicSensorTypeUtils.class.getSimpleName();
    private static DynamicSensorTypeUtils INSTANCE = null;

    private SharedPreferences sensorCodePreferences;
    private SharedPreferences.Editor sensorCodeEditor;

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;

    private DynamicSensorTypeUtils(Context context) {
        if (context != null) {
            mContext = context;
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            sensorCodePreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (sensorCodePreferences != null) {
                sensorCodeEditor = sensorCodePreferences.edit();
            }
        }
    }

    public static synchronized DynamicSensorTypeUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DynamicSensorTypeUtils(context);
        }
        return INSTANCE;
    }

    public void addSensorType(JSONObject addObject) {
        LogUtils.logger(TAG, "Get new Sensor " + addObject.toString());

        try {
            if (addObject != null && !TextUtils.isEmpty(addObject.toString()) && addObject.has(Constant.ThingType.ADD_NEW_THING_TYPE)) {
                JSONArray addThingArray = addObject.getJSONArray(Constant.ThingType.ADD_NEW_THING_TYPE);
                LogUtils.logger(TAG, "Thing Array : " + addThingArray.toString());

                int newSensorSize = addThingArray.length();
                for (int sensorNumber = 0; sensorNumber < newSensorSize; sensorNumber++) {
                    JSONObject addNewThing = new JSONObject(String.valueOf(addThingArray.getJSONObject(sensorNumber)));
                    if (!TextUtils.isEmpty(addNewThing.toString()) && addNewThing.has(Constant.ThingType.THING_CODE) && addNewThing.has(Constant.ThingType.THING_SPECIFIC)) {

                        JSONObject thingSpecific = addNewThing.getJSONObject(Constant.ThingType.THING_SPECIFIC);
                        String thingCode = addNewThing.getString(Constant.ThingType.THING_CODE);
                        String thingId = thingSpecific.getString(Constant.NodeThing.THING_LABEL);
                        String thingTypeString = thingSpecific.getString(Constant.ThingType.THING_TYPE_STRING);
                        String thingVendor = thingSpecific.getString(Constant.ThingType.THING_VENDOR);
                        String thingType = thingSpecific.getString(Constant.ThingType.THING_DATA_TYPE);

                        if (!TextUtils.isEmpty(thingCode) &&
                                !TextUtils.isEmpty(thingId) &&
                                !TextUtils.isEmpty(thingTypeString) &&
                                !TextUtils.isEmpty(thingVendor) &&
                                !TextUtils.isEmpty(thingType)) {

                            if (sensorCodePreferences != null && sensorCodeEditor != null && !sensorCodePreferences.contains(thingCode)) {
                                sensorCodeEditor.putString(thingCode, thingSpecific.toString());
                                sensorCodeEditor.commit();
                                LogUtils.logger(TAG, "Available Preferences : " + sensorCodePreferences.getAll());

                                JSONObject responseCreateNewSensorTrue = new JSONObject().put(Constant.ThingType.ADD_NEW_THING_TYPE, new JSONObject().put(Constant.ThingType.THING_CODE, thingCode).put(Constant.ThingType.THING_SPECIFIC, thingSpecific));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorTrue));
                            } else {
                                LogUtils.logger(TAG, "<" + thingCode + "> Value Available !");

                                JSONObject responseCreateNewSensorFalse = new JSONObject().put(Constant.ResponseJsonKey.NEW_THING_ERROR, new JSONObject()
                                        .put(Constant.ThingType.THING_CODE, thingCode)
                                        .put(Constant.ThingType.THING_SPECIFIC, thingSpecific)
                                        .put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.VALUE_AVAILABLE)
                                        .put(Constant.NodeThing.MESSAGE_ID, ""));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                            }
                        } else {
                            LogUtils.logger(TAG, "The submitted parameters can not be empty!");
                            JSONObject responseCreateNewSensorFalse = new JSONObject().put(Constant.ResponseJsonKey.NEW_THING_ERROR, new JSONObject()
                                    .put(Constant.ThingType.THING_CODE, thingCode)
                                    .put(Constant.ThingType.THING_SPECIFIC, thingSpecific)
                                    .put(Constant.ResponseJsonKey.DESCRIPTIONS, Constant.ResponseJsonValue.VALUE_EMPTY)
                                    .put(Constant.NodeThing.MESSAGE_ID, ""));
                            mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            try {
                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject().put(Constant.ThingType.ERROR, Constant.ThingType.ERROR_STRING)));
            } catch (JSONException e1) {
                Log.e(TAG, "addSensorType Error : " + e1);
            }
            Log.e(TAG, "addSensorType Error : " + e);
        }
    }


    public void removeThingType(JSONObject removeJson) {
        try {
            if (removeJson != null && removeJson.has(Constant.ThingType.REMOVE_THING_TYPE)) {
                JSONObject removeObject = removeJson.getJSONObject(Constant.ThingType.REMOVE_THING_TYPE);
                String removedTypeKey = removeObject.getString(Constant.ThingType.THING_CODE);
                if (sensorCodePreferences != null && sensorCodeEditor != null && removeObject.has(Constant.ThingType.THING_CODE) &&
                        !removeObject.isNull(Constant.ThingType.THING_CODE) &&
                        !TextUtils.isEmpty(removedTypeKey) &&
                        sensorCodePreferences.contains(removedTypeKey)) {

                    LogUtils.logger(TAG, "Remove Sensor Type : " + removedTypeKey);

                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject()
                            .put(Constant.ResponseJsonKey.REMOVED_THING_TYPE, new JSONObject()
                                    .put(Constant.ThingType.THING_CODE, removedTypeKey)
                                    .put(Constant.ThingType.THING_DATA_TYPE, String.valueOf(sensorCodePreferences.getString(removedTypeKey, "N/A")))
                                    .put(Constant.NodeThing.MESSAGE_ID, ""))));

                    sensorCodeEditor.remove(removedTypeKey);
                    sensorCodeEditor.commit();
                }

            }
        } catch (JSONException e) {
            Log.e(TAG, "removeObject Json Error : " + e);
        }

    }

    public String[] getSensorTypeByCode(String sensorCode) {
        if (!TextUtils.isEmpty(sensorCode) && sensorCode.length() == Constant.NUMBER_OF_CHARACTERS && sensorCode.matches(Constant.Regexp.SENSOR_CONTROL)) {
            String parseSensorCode = sensorCode.substring(2, 4);
            if (sensorCodePreferences != null && sensorCodePreferences.contains(parseSensorCode)) {
                try {
                    JSONObject nullSensorTypeJsonObject = new JSONObject();
                    nullSensorTypeJsonObject.put(Constant.ResponseJsonKey.NULL, true);

                    JSONObject getSensorType = new JSONObject(sensorCodePreferences.getString(parseSensorCode, String.valueOf(nullSensorTypeJsonObject)));
                    String[] sensorTypeCode = new String[]{getSensorType.getString(Constant.ThingType.THING_TYPE_STRING), getSensorType.getString(Constant.ThingType.THING_VENDOR), getSensorType.getString(Constant.ThingType.THING_DATA_TYPE)};
                    return sensorTypeCode;
                } catch (JSONException e) {
                    Log.e(TAG, "getSensorTypeByCode Json Error : " + e);
                }
            }
        }
        return null;
    }


}
