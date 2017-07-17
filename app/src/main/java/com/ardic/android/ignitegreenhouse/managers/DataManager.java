package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.configuration.Configuration;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.model.Constant;
import com.ardic.android.ignitegreenhouse.model.SensorType;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    private Context mContext;

    private Configuration mConfiguration;
    private IotIgniteHandler mIotIgniteHandler;

    private Map<String, ThreadManager> threadControl = new ArrayMap<>();

    private static DataManager INSTANCE = null;

    private DataManager(Context context) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Get DataManager...");
        }
        if (context != null) {
            mContext = context;
            mConfiguration = Configuration.getInstance(mContext);
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            threadManager();
            mIotIgniteHandler.updateListener();
            //mConfiguration.removeSavedAllDevices();
        }
    }

    public static synchronized DataManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataManager(context);
        }
        return INSTANCE;
    }


    public void parseData(final String getSensorCode, final String getValue) {
        Thread parseDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(getSensorCode) && !TextUtils.isEmpty(getValue)) {

                    if (Constant.DEBUG) {
                        Log.i(TAG, "Get Id : " + getSensorCode
                                + "\nGet Value : " + getValue);

                        Log.i(TAG, "All Saved Devices  Size : " + mConfiguration.getSavedAllDevices().size() +
                                "\nAll Saved Devices : " + mConfiguration.getSavedAllDevices());
                        Log.i(TAG, "All Run Thread Size : " + threadControl.size() +
                                "\nAll Run Thread : " + threadControl);
                    }
                    SensorType.getInstance(mContext).getSensorType("55443322");
                    //todo : bunu burada unutma
                    String[] getPreferencesKey = mConfiguration.getDeviceCodeThing(getSensorCode);
                    if (getPreferencesKey.length == 1) {
                        if (!TextUtils.isEmpty(getPreferencesKey[0])) {
                            String[] getKeySplit = getPreferencesKey[0].split(":");
                            if (!TextUtils.isEmpty(getKeySplit[0]) && !TextUtils.isEmpty(getKeySplit[1]) && !TextUtils.isEmpty(getValue) && threadControl.containsKey(getPreferencesKey[0])) {
                                threadControl.get(getPreferencesKey[0]).parseData(getKeySplit[0], getKeySplit[1], getValue);
                            }
                        }
                    } else {
                        for (int i = 0; i < getPreferencesKey.length; i++) {
                            if (!TextUtils.isEmpty(getPreferencesKey[i])) {
                                String[] getKeySplit = getPreferencesKey[i].split(":");
                                if (!TextUtils.isEmpty(getKeySplit[0]) && !TextUtils.isEmpty(getKeySplit[1]) && !TextUtils.isEmpty(getValue) && threadControl.containsKey(getPreferencesKey[i])) {
                                    threadControl.get(getPreferencesKey[i]).parseData(getKeySplit[0], getKeySplit[1], getValue);
                                }
                            }
                        }
                    }
                }
            }
        });
        parseDataThread.run();
    }


    public void threadManager() {
        Map<String, ?> getAllSensor = mConfiguration.getSavedAllDevices();
        Set keys = getAllSensor.keySet();
        if (!threadControl.containsKey(keys)) {
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                threadControl.put(key, new ThreadManager(mContext));
            }
        } else {
            if (Constant.DEBUG) {
                Log.e(TAG, "There is a thread named" + keys);
            }
        }
    }

    public void killThread(String nodeName, String thingName) {
        String threadKey = null;
        if (!TextUtils.isEmpty(nodeName) && !TextUtils.isEmpty(thingName)) {
            threadKey = nodeName + ":" + thingName;
        }

        if (!TextUtils.isEmpty(threadKey) && threadControl.containsKey(threadKey)) {
            threadControl.get(threadKey).stopThread();
            threadControl.remove(threadKey);
        }
    }

    public void killAllThread() {
        threadControl.clear();
    }

}
