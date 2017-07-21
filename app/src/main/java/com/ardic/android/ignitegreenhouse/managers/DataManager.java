package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.operations.NodeThingOperations;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    private Context mContext;

    private NodeThingOperations mNodeThingOperations;
    private IotIgniteHandler mIotIgniteHandler;


    private static DataManager INSTANCE = null;

    private DataManager(Context context) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Get DataManager...");
        }
        if (context != null) {
            mContext = context;

            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            mNodeThingOperations = NodeThingOperations.getInstance(mContext);
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


    public void parseData(final String getThingCode, final String getValue) {
        Thread parseDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(getThingCode) && !TextUtils.isEmpty(getValue)) {

                    if (Constant.DEBUG) {
                        Log.i(TAG, "Get Id : " + getThingCode
                                + "\nGet Value : " + getValue);

                        Log.i(TAG, "All Saved Devices  Size : " + mNodeThingOperations.getSavedAllThing().size() +
                                "\nAll Saved Devices : " + mNodeThingOperations.getSavedAllThing());
                        Log.i(TAG, "All Run Thread Size : " + ThreadManager.getInstance(mContext).getThreadControlSize() +
                                "\nAll Run Thread : " +  ThreadManager.getInstance(mContext).getRunThread());
                    }
                    // todo : thread kontrol getter

                    String[] getPreferencesKey = mNodeThingOperations.getThingID(getThingCode);
                    for (int i = 0; i < getPreferencesKey.length; i++) {
                        if (!TextUtils.isEmpty(getPreferencesKey[i])) {
                            String[] getKeySplit = getPreferencesKey[i].split(":");
                            if (!TextUtils.isEmpty(getKeySplit[0]) && !TextUtils.isEmpty(getKeySplit[1]) && !TextUtils.isEmpty(getValue) &&  ThreadManager.getInstance(mContext).isThread(getPreferencesKey[i])) {
                                ThreadManager.getInstance(mContext).getThreadOperation(getPreferencesKey[i]).parseData(getKeySplit[0], getKeySplit[1], getValue);
                            }
                        }
                    }
                }
            }
        });
        parseDataThread.run();
    }
}
