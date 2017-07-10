package com.ardic.android.ignitegreenhouse.DataBase;

/**
 * Created by acel on 7/10/17.
 */

public class DevicesItems {
    public static final String TABLE_NAME="DEVICES_TABLE";
    public static final String DEVICES_ID="DEVICE_ID";
    public static final String NODE_NAME="NODE_ID";
    public static final String THING_ID="THING_ID";
    public static final String THING_LABEL="THING_LABEL";

    public static String createTable = "CREATE TABLE "
                            + TABLE_NAME
                            + "("
                            + DEVICES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + NODE_NAME + " VARCHAR(30), "
                            + THING_ID + " VARCHAR(8), "
                            + THING_LABEL + " VARCHAR(30)"
                            + ")";
}
