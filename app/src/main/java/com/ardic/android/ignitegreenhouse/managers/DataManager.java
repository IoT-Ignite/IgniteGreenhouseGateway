package com.ardic.android.ignitegreenhouse.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.configuration.Configuration;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();


    private static final String INTENT_FILTER_CONFIG = "getConfig";

    private static final String INTENT_NODE_NAME = "getConfigPutNodeName";
    private static final String INTENT_THING_NAME = "getConfigPutThingName";
    private static final String INTENT_THING_FREQUENCY = "getConfigPutFrequency";

    private String getSensorId;
    private String getValue;

    private boolean getDataFlag = false;
    private long getDelayTimeData = 10000L;
    private Handler sendDataHandler = new Handler();
    private Context mContext;

    private String getNodeName;
    private String getThingName;

    ThreadManager mThreadManager = new ThreadManager();

    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (getDataFlag) {
                getDataFlag = false;
                //sendDataIgnite();
            }
            sendDataHandler.postDelayed(sendDataRunnable, getDelayTimeData);
        }
    };

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

    public DataManager(Context context) {
        Log.i(TAG, "Get DataManager...");
        mContext = context;
        LocalBroadcastManager.getInstance(context).registerReceiver(getIgniteConfig,
                new IntentFilter(INTENT_FILTER_CONFIG));
        sendDataHandler.post(sendDataRunnable);
    }

    public void parseData(String getSensorId, String getValue) {
        if (!TextUtils.isEmpty(getSensorId) && !TextUtils.isEmpty(getValue)) {
            getDataFlag = true;
            this.getSensorId = getSensorId;
            this.getValue = getValue;
            Log.i(TAG, "Get Id : " + getSensorId
                    + "\nGet Value : " + getValue);


            Log.i(TAG, "All Saved Devices : " + Configuration.getInstance(mContext).getSavedAllDevices());
            //Configuration.getInstance(mContext).removeSavedAllDevices();


            String getPreferencesKey = Configuration.getInstance(mContext).getDeviceCodeThing(getSensorId);
            if (!TextUtils.isEmpty(getPreferencesKey)) {
                String[] getKeySplit = getPreferencesKey.split(":");
                IotIgniteHandler.getInstance(mContext).sendData(getKeySplit[0], getKeySplit[1], getValue);
                //mThreadManager.parseData(getKeySplit[0],getKeySplit[1], getValue,getDelayTimeData);
            }

            //Configuration.getInstance(mContext).removeSavedAllDevices();
            //Log.i(TAG,"Get Node Name : " + Configuration.getInstance(mContext).getDeviceCodeThing("f4030687"));

            //Log.i(TAG,"Seratonin 1 : " +Configuration.getInstance(mContext).getSaveDevices("GreenHouse2","Temperature"));
            //TODO : THread Manager SOnraki adım
            /// / mThreadManager.parseData(getSensorId,getValue,getDelayTimeData);
            //mThreadManager.run();

            // Todo : COntrol DEvice
            if (Configuration.getInstance(mContext).matchDevice(getSensorId)) {

            }
        }
    }
    //TODO : Burada unutma

    private void sendDataIgnite(String thingId) {

      /*  if (getValue != null) {
            if (IotIgniteHandler.getInstance(mContext).sendData(Float.parseFloat(getValue))) {
                Log.i(TAG, "Send Data : OK");
            } else {
                Log.e(TAG, "Send Data : NO");
            }
        }*/
    }
}
