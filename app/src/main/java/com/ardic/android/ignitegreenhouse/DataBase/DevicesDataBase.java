package com.ardic.android.ignitegreenhouse.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by acel on 7/10/17.
 */

public class DevicesDataBase extends SQLiteOpenHelper {
    private static final String DATA_BASE_NAME="Devices";
    private static final int DATA_BASE_VERSION=1;
    private static final String TAG =DevicesDataBase.class.getSimpleName() ;
    private Context mContext;

    private static DevicesDataBase INSTANCE = null;

    private DevicesDataBase(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        mContext = context;
    }

    public static synchronized DevicesDataBase getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new DevicesDataBase(context);
        }
        return INSTANCE;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DevicesItems.createTable);
        Log.i(TAG,"Created Data Base");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertDevices(String nodeID, String thingID, String thingLabel){
        SQLiteDatabase mDB = this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();

        contentValues.put(DevicesItems.NODE_NAME,nodeID);
        contentValues.put(DevicesItems.THING_ID,thingID);
        contentValues.put(DevicesItems.THING_LABEL,thingLabel);

        mDB.insert(DevicesItems.TABLE_NAME,null,contentValues);
        mDB.close();
        Log.i(TAG,"Node : " + nodeID
                + "\nThing : " + thingID
                + "\nThing Label : " + thingLabel
                + "\nAdd ...");
    }

    public String[] getItem(String getNodeName){
        String nodeName =null;
        String thingID =null;
        String thingLabel =null;
        SQLiteDatabase mDB = this.getWritableDatabase();
        String[] colums={DevicesItems.NODE_NAME,DevicesItems.THING_ID,DevicesItems.THING_LABEL};
        String[] selectionArgs={getNodeName};
        Cursor mCursor = mDB.query(DevicesItems.TABLE_NAME,colums,DevicesItems.NODE_NAME + " =?",selectionArgs,null,null,null );
        String[] deviceInf =new String[0];

        if (mCursor.getCount()>0){
            while (mCursor.moveToNext()){
                nodeName=mCursor.getString(mCursor.getColumnIndex(DevicesItems.NODE_NAME));
                thingID=mCursor.getString(mCursor.getColumnIndex(DevicesItems.THING_ID));
                thingLabel=mCursor.getString(mCursor.getColumnIndex(DevicesItems.THING_LABEL));
                deviceInf= new String[]{nodeName,thingID,thingLabel};
                    Log.i(TAG, "Get DATABASE Node Name : " + nodeName +
                            "\nThing Name : " + thingID +
                            "\nThing Label : " + thingLabel);
            }
        }else{
            deviceInf=new String[]{String.valueOf(mCursor.getCount())};
        }
        mCursor.close();
        mDB.close();
        return deviceInf;
    }
}
