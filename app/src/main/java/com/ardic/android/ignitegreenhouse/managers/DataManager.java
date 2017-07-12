package com.ardic.android.ignitegreenhouse.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.configuration.Configuration;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();


    private static final String INTENT_FILTER_CONFIG = "getConfig";

    private static final String INTENT_NODE_NAME = "getConfigPutNodeName";
    private static final String INTENT_THING_NAME = "getConfigPutThingName";
    private static final String INTENT_THING_FREQUENCY = "getConfigPutFrequency";

    private Context mContext;

    private long getDelayTimeData = 10000L;
    private String getNodeName;
    private String getThingName;

    private ThreadManager mThreadManager;
    private Configuration mConfiguration;
    private IotIgniteHandler mIotIgniteHandler;


    /**
     * Receive cloud configuration data
     */
    private BroadcastReceiver getIgniteConfig = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getNodeName = intent.getStringExtra(INTENT_NODE_NAME);
            getThingName = intent.getStringExtra(INTENT_THING_NAME);
            getDelayTimeData = intent.getLongExtra(INTENT_THING_FREQUENCY, 10000L);
            // Log.i(TAG, "Get Config : " + getDelayTimeData + " sec");
            Log.i(TAG, "Get Config Node : " + getNodeName);
            Log.i(TAG, "Get Config Thing : " + getThingName);
            Log.i(TAG, "Get Config Delay : " + getDelayTimeData);
        }
    };
    // Todo : Preferences Ismine Göre

    public DataManager(Context context) {
        Log.i(TAG, "Get DataManager...");
        if (context != null) {
            mContext = context;
            LocalBroadcastManager.getInstance(context).registerReceiver(getIgniteConfig,
                    new IntentFilter(INTENT_FILTER_CONFIG));
            mConfiguration = Configuration.getInstance(mContext);
            mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
            mThreadManager = new ThreadManager(mContext);
            threadManager();
            //mThreadManager.run();
        }
    }

    public void parseData(String getSensorId, String getValue) {
        if (!TextUtils.isEmpty(getSensorId) && !TextUtils.isEmpty(getValue)) {
            Log.i(TAG, "Get Id : " + getSensorId
                    + "\nGet Value : " + getValue);

            Log.i(TAG, "All Saved Devices : " + mConfiguration.getSavedAllDevices());
            //mConfiguration.removeSavedAllDevices();

            String getPreferencesKey = mConfiguration.getDeviceCodeThing(getSensorId);
            if (!TextUtils.isEmpty(getPreferencesKey)) {
                String[] getKeySplit = getPreferencesKey.split(":");
                if (!TextUtils.isEmpty(getKeySplit[0]) && !TextUtils.isEmpty(getKeySplit[1]) && !TextUtils.isEmpty(getValue)) {
                    mIotIgniteHandler.sendData(getKeySplit[0], getKeySplit[1], getValue);
                    threadControl.get(getPreferencesKey).parseData(getKeySplit[0], getKeySplit[1], getValue, getDelayTimeData);
                }
            }
        }
    }

    private ThreadManager[] threads;
    private Map<String, ThreadManager> threadControl = new ArrayMap<>();
    public void threadManager() {
        int counter=0;
        int thingsValue =mConfiguration.getSavedAllDevices().size();
        threads=new ThreadManager[thingsValue+1];
        Map<String, ?> getAllSensor = mConfiguration.getSavedAllDevices();
        Set keys = getAllSensor.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            threads[counter]=new ThreadManager(mContext);
            threadControl.put(key,threads[counter]);
            counter++;
        }

    }
}
