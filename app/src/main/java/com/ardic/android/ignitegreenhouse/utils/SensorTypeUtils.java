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
 * Created by acel on 7/14/17.
 */

public class SensorTypeUtils {

    private static final String TAG = SensorTypeUtils.class.getSimpleName();
    private static SensorTypeUtils INSTANCE = null;

    private SharedPreferences sensorCodePreferences;
    private SharedPreferences.Editor sensorCodeEditor;

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;

    private SensorTypeUtils(Context context) {
        if (context!=null) {
            mContext = context;
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            sensorCodePreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sensorCodeEditor = sensorCodePreferences.edit();
        }
    }

    public static synchronized SensorTypeUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SensorTypeUtils(context);
        }
        return INSTANCE;
    }

    public void addSensorType(JSONObject addObject) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Get new Sensor " + addObject.toString());
        }
        try {
            if (!TextUtils.isEmpty(addObject.toString()) && addObject.has(Constant.ADD_NEW_THING_TYPE_JSON_KEY)) {
                JSONArray addThingArray = addObject.getJSONArray(Constant.ADD_NEW_THING_TYPE_JSON_KEY);
                if (Constant.DEBUG) {
                    Log.i(TAG, "Thing Array : " + addThingArray.toString());
                }
                int newSensorSize = addThingArray.length();
                for (int sensorNumber = 0; sensorNumber < newSensorSize; sensorNumber++) {
                    JSONObject addNewThing = new JSONObject(String.valueOf(addThingArray.getJSONObject(sensorNumber)));
                    if (!TextUtils.isEmpty(addNewThing.toString()) && addNewThing.has(Constant.THING_CODE_JSON_KEY) && addNewThing.has(Constant.THING_SPECIFIC_JSON_KEY)) {

                        JSONObject thingSpecific = addNewThing.getJSONObject(Constant.THING_SPECIFIC_JSON_KEY);
                        String thingCode = addNewThing.getString(Constant.THING_CODE_JSON_KEY);
                        String thingId = thingSpecific.getString(Constant.THING_LABEL_JSON_KEY);
                        String thingTypeString = thingSpecific.getString(Constant.THING_TYPE_STRING_JSON_KEY);
                        String thingVendor = thingSpecific.getString(Constant.THING_VENDOR_JSON_KEY);
                        String thingType = thingSpecific.getString(Constant.THING_TYPE_JSON_KEY);

                        if (!TextUtils.isEmpty(thingCode) &&
                                !TextUtils.isEmpty(thingId) &&
                                !TextUtils.isEmpty(thingTypeString) &&
                                !TextUtils.isEmpty(thingVendor) &&
                                !TextUtils.isEmpty(thingType)) {

                            if (!sensorCodePreferences.contains(thingCode)) {
                                sensorCodeEditor.putString(thingCode, thingSpecific.toString());
                                sensorCodeEditor.commit();
                                if (Constant.DEBUG) {
                                    Log.i(TAG, "Available Preferences : " + sensorCodePreferences.getAll());
                                }
                                JSONObject responseCreateNewSensorTrue = new JSONObject().put(Constant.ADD_NEW_THING_TYPE_JSON_KEY, new JSONObject().put(Constant.THING_CODE_JSON_KEY, thingCode).put(Constant.THING_SPECIFIC_JSON_KEY, thingSpecific));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorTrue));
                            } else {
                                if (Constant.DEBUG) {
                                    Log.e(TAG, "<" + thingCode + "> Value Available !");
                                }
                                JSONObject responseCreateNewSensorFalse = new JSONObject().put(Constant.RESPONSE_NEW_THING_ERROR_JSON_KEY, new JSONObject()
                                        .put(Constant.THING_CODE_JSON_KEY, thingCode)
                                        .put(Constant.THING_SPECIFIC_JSON_KEY, thingSpecific)
                                        .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY, Constant.RESPONSE_VALUE_AVAILABLE_JSON_VALUE)
                                        .put(Constant.MESSAGE_ID_JSON_KEY,""));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                            }
                        } else {
                            if (Constant.DEBUG) {
                                Log.e(TAG, "The submitted parameters can not be empty!");
                            }
                            JSONObject responseCreateNewSensorFalse = new JSONObject().put(Constant.RESPONSE_NEW_THING_ERROR_JSON_KEY, new JSONObject()
                                    .put(Constant.THING_CODE_JSON_KEY, thingCode)
                                    .put(Constant.THING_SPECIFIC_JSON_KEY, thingSpecific)
                                    .put(Constant.RESPONSE_DESCRIPTIONS_JSON_KEY, Constant.RESPONSE_VALUE_EMPTY_JSON_VALUE)
                                    .put(Constant.MESSAGE_ID_JSON_KEY,""));
                            mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            mIotIgniteHandler.sendConfiguratorThingMessage("\"error\":\"Error new thing\"");
            Log.e(TAG,"Error : " + e.toString());
        }

    }


    public void removeThingType(JSONObject removeJson) {
        try {
            if (removeJson.has(Constant.REMOVE_THING_TYPE)) {
                JSONObject removeObject = removeJson.getJSONObject(Constant.REMOVE_THING_TYPE);
                String removedTypeKey = removeObject.getString(Constant.THING_CODE_JSON_KEY);
                if (removeObject.has(Constant.THING_CODE_JSON_KEY) &&
                        !removeObject.isNull(Constant.THING_CODE_JSON_KEY) &&
                        !TextUtils.isEmpty(removedTypeKey) &&
                        sensorCodePreferences.contains(removedTypeKey)) {

                    Log.e(TAG, "Remove Sensor Type : " + removedTypeKey);


                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject()
                            .put(Constant.RESPONSE_REMOVED_THING_TYPE_JSON_KEY, new JSONObject()
                                    .put(Constant.THING_CODE_JSON_KEY, removedTypeKey)
                                    .put(Constant.THING_TYPE_JSON_KEY, String.valueOf(sensorCodePreferences.getString(removedTypeKey, "N/A")))
                                    .put(Constant.MESSAGE_ID_JSON_KEY,""))));

                    sensorCodeEditor.remove(removedTypeKey);
                    sensorCodeEditor.commit();
                }

            }
        } catch (JSONException e) {
            Log.e(TAG,"Error : " + e.toString());
        }

    }

    public String[] getSensorType(String sensorCode) {
        if (!TextUtils.isEmpty(sensorCode) && sensorCode.length() == Constant.NUMBER_OF_CHARACTERS && sensorCode.matches(Constant.SENSOR_CONTROL_REGEXP)) {
            String parseSensorCode = sensorCode.substring(2, 4);
            if (sensorCodePreferences.contains(parseSensorCode)) {
                try {
                    JSONObject getSensorType = new JSONObject(sensorCodePreferences.getString(parseSensorCode, "{\"null\":true}"));
                    String[] sensorTypeArray = {getSensorType.getString("thingTypeString"), getSensorType.getString(Constant.THING_VENDOR_JSON_KEY), getSensorType.getString("thingType")};
                    return sensorTypeArray;
                } catch (JSONException e) {
                    Log.e(TAG,"Error : " + e.toString());
                }
            }
        }
        return null;
    }


}
