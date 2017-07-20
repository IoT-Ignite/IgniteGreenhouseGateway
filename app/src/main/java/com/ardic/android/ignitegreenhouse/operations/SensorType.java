package com.ardic.android.ignitegreenhouse.operations;

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

public class SensorType {
// todo : Sonuna operations ekle
    private static final String TAG = SensorType.class.getSimpleName();
    private static SensorType INSTANCE = null;

    private SharedPreferences sensorCodePreferences;
    private SharedPreferences.Editor sensorCodeEditor;

    private static final String THING_CODE_STRING = "thingCode";
    private static final String THING_SPECIFIC = "thingSpecific";
    private static final String ERROR_RESPONSE_STRING = "errorNewThingResponse";
    private static final String THING_ID_STRING = "thingId";
    private static final String THING_TYPE_STRING = "thingTypeString";
    private static final String THING_VENDOR_STRING = "thingVendor";
    private static final String THING_TYPE = "thingType";
    private static final String DESCRIPTION_STRING = "description";
    private static final String VALUE_AVAILABLE__ERROR_STRING = "Value Available";
    private static final String VALUE_EMPTY_ERROR_STRING = "The submitted parameters can not be empty";

    private Context mContext;
    private IotIgniteHandler mIotIgniteHandler;

    private SensorType(Context context) {
        if (!context.equals(null)) {
            mContext = context;
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            sensorCodePreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sensorCodeEditor = sensorCodePreferences.edit();
        }
    }

    public static synchronized SensorType getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SensorType(context);
        }
        return INSTANCE;
    }

    /**
     *
     */
    public void addSensorType(JSONObject addObject) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Get new Sensor " + addObject.toString());
        }
        try {
            if (!TextUtils.isEmpty(addObject.toString()) && addObject.has(Constant.ADD_NEW_THING_TYPE)) {
                JSONArray addThingArray = addObject.getJSONArray(Constant.ADD_NEW_THING_TYPE);
                if (Constant.DEBUG) {
                    Log.i(TAG, "Thing Array : " + addThingArray.toString());
                }
                int newSensorSize = addThingArray.length();
                for (int sensorNumber = 0; sensorNumber < newSensorSize; sensorNumber++) {
                    JSONObject addNewThing = new JSONObject(String.valueOf(addThingArray.getJSONObject(sensorNumber)));
                    if (!TextUtils.isEmpty(addNewThing.toString()) && addNewThing.has(THING_CODE_STRING) && addNewThing.has(THING_SPECIFIC)) {
                        JSONObject thingSpecific = addNewThing.getJSONObject(THING_SPECIFIC);
                        String thingCode = addNewThing.getString(THING_CODE_STRING);
                        String thingId = thingSpecific.getString(THING_ID_STRING);
                        String thingTypeString = thingSpecific.getString(THING_TYPE_STRING);
                        String thingVendor = thingSpecific.getString(THING_VENDOR_STRING);
                        String thingType = thingSpecific.getString(THING_TYPE);
                        if (!TextUtils.isEmpty(thingCode) && !TextUtils.isEmpty(thingId) && !TextUtils.isEmpty(thingTypeString) && !TextUtils.isEmpty(thingVendor) && !TextUtils.isEmpty(thingType)) {
                            if (!sensorCodePreferences.contains(thingCode)) {
                                sensorCodeEditor.putString(thingCode, thingSpecific.toString());
                                sensorCodeEditor.commit();
                                if (Constant.DEBUG) {
                                    Log.i(TAG, "Available Preferences : " + sensorCodePreferences.getAll());
                                }
                                JSONObject responseCreateNewSensorTrue = new JSONObject().put(Constant.ADD_NEW_THING_TYPE, new JSONObject().put(THING_CODE_STRING, thingCode).put(THING_SPECIFIC, thingSpecific));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorTrue));
                            } else {
                                if (Constant.DEBUG) {
                                    Log.e(TAG, "<" + thingCode + "> Value Available !");
                                }
                                JSONObject responseCreateNewSensorFalse = new JSONObject().put(ERROR_RESPONSE_STRING, new JSONObject().put(THING_CODE_STRING, thingCode).put(THING_SPECIFIC, thingSpecific).put(DESCRIPTION_STRING, VALUE_AVAILABLE__ERROR_STRING));
                                mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                            }
                        } else {
                            if (Constant.DEBUG) {
                                Log.e(TAG, "The submitted parameters can not be empty!");
                            }
                            JSONObject responseCreateNewSensorFalse = new JSONObject().put(ERROR_RESPONSE_STRING, new JSONObject().put(THING_CODE_STRING, thingCode).put(THING_SPECIFIC, thingSpecific).put(DESCRIPTION_STRING, VALUE_EMPTY_ERROR_STRING));
                            mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(responseCreateNewSensorFalse));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            mIotIgniteHandler.sendConfiguratorThingMessage("\"error\":\"Error new thing\"");
            e.printStackTrace();
        }

    }

    private static final String REMOVED_THING_TYPE_RESPONSE = "removedThingType";

    public void removeThingType(JSONObject removeJson) {
        try {
            if (removeJson.has(Constant.REMOVE_THING_TYPE)) {
                JSONObject removeObject = removeJson.getJSONObject(Constant.REMOVE_THING_TYPE);
                String removedTypeKey = removeObject.getString(Constant.GET_THING_CODE_STRING);
                if (removeObject.has(Constant.GET_THING_CODE_STRING) &&
                        !removeObject.isNull(Constant.GET_THING_CODE_STRING) &&
                        !TextUtils.isEmpty(removedTypeKey) &&
                        sensorCodePreferences.contains(removedTypeKey)) {

                    Log.e(TAG, "Remove Sensor Type : " + removedTypeKey);


                    mIotIgniteHandler.sendConfiguratorThingMessage(String.valueOf(new JSONObject()
                            .put(REMOVED_THING_TYPE_RESPONSE, new JSONObject()
                                    .put(Constant.GET_THING_CODE_STRING, removedTypeKey)
                                    .put(THING_TYPE, String.valueOf(sensorCodePreferences.getString(removedTypeKey, "N/A"))))));

                    sensorCodeEditor.remove(removedTypeKey);
                    sensorCodeEditor.commit();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String[] getSensorType(String sensorCode) {
        if (!TextUtils.isEmpty(sensorCode) && sensorCode.length() == Constant.NUMBER_OF_CHARACTERS && sensorCode.matches(Constant.REGEXP_ID)) {
            String parseSensorCode = sensorCode.substring(2, 4);
            if (sensorCodePreferences.contains(parseSensorCode)) {
                try {
                    JSONObject getSensorType = new JSONObject(sensorCodePreferences.getString(parseSensorCode, "{\"null\":true}"));
                    String[] sensorTypeArray = {getSensorType.getString("thingTypeString"), getSensorType.getString("thingVendor"), getSensorType.getString("thingType")};
                    return sensorTypeArray;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
