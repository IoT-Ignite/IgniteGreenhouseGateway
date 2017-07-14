package com.ardic.android.ignitegreenhouse.model;

/**
 * Created by acel on 7/14/17.
 */

public class SensorType {

    private static final String TAG = SensorType.class.getSimpleName();
    private static SensorType INSTANCE = null;

    private SensorType() {
    }

    public static synchronized SensorType getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SensorType();
        }
        return INSTANCE;
    }

    //TODO : Burada gelen sensor code göre type döndürecek
    public String getSensorType(String sensorCode) {
    return sensorCode;
    }
}
