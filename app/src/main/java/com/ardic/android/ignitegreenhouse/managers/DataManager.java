package com.ardic.android.ignitegreenhouse.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.configuration.Configuration;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

/**
 * Created by acel on 7/7/17.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    private String getDeviceId;
    private String getValue;

    private boolean getDataFlag = false;
    private long getDelayTimeData = 10000L;
    private Handler sendDataHandler = new Handler();
    private Context mContext;

    ThreadManager mThreadManager=new ThreadManager();

    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (getDataFlag){
                getDataFlag=false;
                sendDataIgnite();
            }
            sendDataHandler.postDelayed(sendDataRunnable,getDelayTimeData);
        }
    };

    /**
     * Receive cloud configuration data
     */
    private BroadcastReceiver getIgniteConfig = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getDelayTimeData = intent.getLongExtra("getConfig", 10000L);
            Log.i(TAG, "Get Config : " + getDelayTimeData + " sec");
        }
    };

    public DataManager(Context context) {
        Log.i(TAG, "Get DataManager...");
        mContext=context;
        LocalBroadcastManager.getInstance(context).registerReceiver(getIgniteConfig,
                new IntentFilter("getConfig"));
        sendDataHandler.post(sendDataRunnable);
    }

    public void getData(String getDeviceId, String getValue) {
        if (getDeviceId != null && getValue != null) {
            getDataFlag=true;
            this.getDeviceId = getDeviceId;
            this.getValue = getValue;
            Log.i(TAG, "Get Id : " + getDeviceId
                    + "\nGet Value : " + getValue);

           //TODO : THread Manager SOnraki adım
            /// / mThreadManager.getData(getDeviceId,getValue,getDelayTimeData);
            //mThreadManager.run();

            // Todo : COntrol DEvice

            if (Configuration.getInstance(mContext).matchDevice(getDeviceId)){

            }
        }
    }
    //TODO : Burada unutma

    private void sendDataIgnite(){
        if (IotIgniteHandler.getInstance(mContext).sendData(Float.parseFloat(getValue))) {
            Log.i(TAG, "Send Data : OK");
        } else {
            Log.e(TAG, "Send Data : NO");
        }
    }



}
