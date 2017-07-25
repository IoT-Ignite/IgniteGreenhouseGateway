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


    private static DataManager INSTANCE = null;

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
        if (INSTANCE == null) {
            INSTANCE = new DataManager(context);
        }
        return INSTANCE;
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
                        Log.i(TAG, "All Run Thread Size : " + ThreadManager.getInstance(mContext).getThreadControlSize() +
                                "\nAll Run Thread : " + ThreadManager.getInstance(mContext).getRunThread());
                    }

                    /**The "node & thing" names of those "thing code" subdirectories are thrown to the "getPreferencesKey" array*/
                    String[] getPreferencesKey = mNodeThingUtils.getThingID(getThingCode);
                    for (int i = 0; i < getPreferencesKey.length; i++) {
                        if (!TextUtils.isEmpty(getPreferencesKey[i])) {
                            String[] getKeySplit = getPreferencesKey[i].split(":");
                            if (!TextUtils.isEmpty(getKeySplit[0]) && !TextUtils.isEmpty(getKeySplit[1]) && !TextUtils.isEmpty(getValue) && ThreadManager.getInstance(mContext).isThread(getPreferencesKey[i])) {
                                /**The "getThreadOperation" function in the "ThreadManager" class will send the information of "node", "thing" and "value" to be sent to the registered "thread"*/
                                if (ThreadManager.getInstance(mContext).getThreadOperation(getPreferencesKey[i]) != null) {
                                    ThreadManager.getInstance(mContext).getThreadOperation(getPreferencesKey[i]).parseData(getKeySplit[0], getKeySplit[1], getValue);
                                }
                            }
                        }
                    }
                }
            }
        });
        parseDataThread.start();
    }
}
