package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.utils.NodeThingUtils;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    private Context mContext;

    private NodeThingUtils mNodeThingUtils;
    private IotIgniteHandler mIotIgniteHandler;


    private static DataManager instance = null;

    private DataManager(Context context) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Get DataManager...");
        }
        if (context != null) {
            mContext = context;

            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            mNodeThingUtils = NodeThingUtils.getInstance(mContext);
            ThreadManager.getInstance(mContext).threadManager();
            mIotIgniteHandler.updateListener();
        }
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }


    /**
     * "ParseData" is the function that sends the "Action Message" in
     * the "Configurator Thing" to the relevant "Manager" according to its content and
     * allows the specified operations to be performed in the message.
     */
    public void parseData(final String getThingCode, final String getValue) {
        Thread parseDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /**Incoming value control*/
                if (!TextUtils.isEmpty(getThingCode) && !TextUtils.isEmpty(getValue)) {
                    if (Constant.DEBUG) {
                        Log.i(TAG, "Get Id : " + getThingCode
                                + "\nGet Value : " + getValue);
                        Log.i(TAG, "All Saved Devices  Size : " + mNodeThingUtils.getSavedAllThing().size() +
                                "\nAll Saved Devices : " + mNodeThingUtils.getSavedAllThing());
                        ThreadManager.getInstance(mContext).threadStatusLog();
                    }

                    /**The "node & thing" names of those "thing code" subdirectories are thrown to the "getPreferencesKey" array*/
                    String[] getPreferencesKey = mNodeThingUtils.getThingID(getThingCode);

                    for (int i = 0; i < getPreferencesKey.length; i++) {
                        parseDataSend(getPreferencesKey[i],getValue);
                    }
                }
            }
        });
        parseDataThread.start();
    }

    private void parseDataSend(String getPreferencesKey , String getValue){
        if (!TextUtils.isEmpty(getPreferencesKey)) {
            String[] getKeySplit = getPreferencesKey.split(":");
            if (parseData(getKeySplit[0], getKeySplit[1], getPreferencesKey)) {
                /**The "getThreadOperation" function in the "ThreadManager" class will send the information of "node", "thing" and "value" to be sent to the registered "thread"*/
                ThreadManager.getInstance(mContext).getThreadOperation(getPreferencesKey).parseData(getKeySplit[0], getKeySplit[1], getValue);
            }
        }
    }

    private boolean parseData(String getKeySplit0, String getKeySplit1, String getPreferencesKey) {
        boolean status = false;
        if (!TextUtils.isEmpty(getKeySplit0) && !TextUtils.isEmpty(getKeySplit1)
                && ThreadManager.getInstance(mContext).controlThread(getPreferencesKey)) {
            status= true;
        }
        return status;
    }
}
