package com.ardic.android.ignitegreenhouse.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

/**
 * Created by acel on 7/7/17.
 */

public class ThreadManager extends Thread {

    private static final String TAG = ThreadManager.class.getSimpleName();
    private Handler sendDataHandler = new Handler();
    private String getNodeName;
    private String getThingName;
    private String getValue;
    private long getDelayTime = 10000L;
    private boolean getMessageFlag = false;
    private IotIgniteHandler mIotIgniteHandler;
    private boolean getIgniteStatus = false;
    boolean getRunnableFlag = true;

    private Context mContext;
    private static final String INTENT_FILTER_IGNITE_STATUS = "igniteConnect";

    private BroadcastReceiver igniteStatusMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getIgniteStatus = intent.getBooleanExtra("igniteStatus", true);
        }
    };


    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (getMessageFlag && getIgniteStatus) {
                Log.i(TAG, "Get New Thread Runnable");
                if (!TextUtils.isEmpty(getNodeName) && !TextUtils.isEmpty(getThingName) && !TextUtils.isEmpty(getValue)) {
                    mIotIgniteHandler.sendData(getNodeName, getThingName, getValue);
                    getRunnableFlag = false;
                    getMessageFlag = false;
                    Log.i(TAG, "Hurraaaa");
                }
            }
            sendDataHandler.postDelayed(this, getDelayTime);

        }
    };

    public ThreadManager(Context context) {
        Log.i(TAG, "Thread Manager Enter ");
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(igniteStatusMessage,
                new IntentFilter(INTENT_FILTER_IGNITE_STATUS));
        mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);
        this.run();
    }

    public void parseData(String nodeName, String getThingName, String getValue, long getDelayTime) {
        Log.i(TAG, "Parse Data Node Name : " + nodeName +
                "\nThing Name : " + getThingName +
                "\nMessage : " + getValue +
                "\nDelay : " + getDelayTime);
        this.getNodeName = nodeName;
        this.getThingName = getThingName;
        this.getValue = getValue;
        this.getDelayTime = getDelayTime;
        this.getMessageFlag = true;


    }

    public void threadExample(String x) {
        Log.i(TAG, "threadExample Data : " + x);
    }

    @Override
    public void run() {
        super.run();
        if (getRunnableFlag) {
            sendDataHandler.post(sendDataRunnable);
            Log.i(TAG, "run1");
        }
        Log.i(TAG, "run2");
    }

}
