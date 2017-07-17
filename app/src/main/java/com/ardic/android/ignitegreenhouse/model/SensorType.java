package com.ardic.android.ignitegreenhouse.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by acel on 7/14/17.
 */

public class SensorType {

    private static final String TAG = SensorType.class.getSimpleName();
    private static SensorType INSTANCE = null;
    private SharedPreferences sensorCode;
    private SharedPreferences.Editor sensorCodeEditor;

    private SensorType(Context context) {
        if (!context.equals(null)) {
            sensorCode = PreferenceManager.getDefaultSharedPreferences(context);
            sensorCodeEditor = sensorCode.edit();
            JSONObject thingType = new JSONObject();
            try {

                thingType.put("thingTypeString", "GHT-Temperature (Green House Type - Temperature)");
                thingType.put("thingVendor", "GHV - Temperature (Green House Vendor - Temperature)");
                thingType.put("thingType", "float");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "JsonObje : "+thingType.toString());
            // sensorCodeEditor.putString("01",thingType.toString() );
            //sensorCodeEditor.commit();
        }
    }

    public void addSensorType(JSONObject addObject){
        Log.e(TAG,"Get new Sensor " + addObject.toString());

    }

    /*
       * Thing ID : GH-DHT11-Temperature
           * Thing Type String : GHT-Temperature (Green House Type - Temperature)
           * Thing Vendor : GHV - Temperature (Green House Vendor - Temperature)
           * Thing Type : Float
     */

    public static synchronized SensorType getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SensorType(context);
        }
        return INSTANCE;
    }

    //f4030687
    //TODO : Burada gelen sensor code göre type döndürecek
    public String getSensorType(String sensorCode) {
        if (!TextUtils.isEmpty(sensorCode) && sensorCode.length() == Constant.NUMBER_OF_CHARACTERS && sensorCode.matches(Constant.REGEXP_ID)) {
            parseSensorCode(sensorCode.substring(2, 4));

        }
        return sensorCode;
    }

    private String parseSensorCode(String parseCode) {

        return parseCode;
    }
}
