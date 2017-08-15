package com.ardic.android.ignitegreenhouse.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.constants.Constant;

/**
 * Created by Mert Acel on 7/7/17.
 */

public class ThreadUtils extends Thread {

    private static final String TAG = ThreadUtils.class.getSimpleName();
    private Handler sendDataHandler;
    private String getNodeName;
    private String getThingName;
    private String getValue;
    private long getDelayTime = Constant.NOT_READ_CONFIGURATION;

    private IotIgniteHandler mIotIgniteHandler;

    private boolean getMessageFlag = false;
    private boolean getRunnableFlag = true;
    private boolean broadCastFlag = false;

    private Context mContext;

    private static final long CONFIG_READ_DELAY_TIME = 60000;

    private Handler configReadHandler;

    private BroadcastReceiver getIgniteConfig = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadCastFlag=intent.getBooleanExtra(Constant.IntentFilter.CONFIGURATION_FLAG,false);
        }
    };

    private Runnable sendDataRunnable = new Runnable() {

        private void configurationDelayControl() {
            if (broadCastFlag) {
                getDelayTime = (IotIgniteHandler.getInstance(mContext).getConfigurationTime(getNodeName + ":" + getThingName));
                if (getDelayTime == Constant.NOT_READ_CONFIGURATION || getDelayTime == Constant.NOT_READ_IGNITE_CONFIGURATION) {
                    configReadHandler.postDelayed(configRead, CONFIG_READ_DELAY_TIME);
                }
                broadCastFlag = false;
            }
        }

        private boolean checkParameters() {
            boolean status = false;
            if (!TextUtils.isEmpty(getNodeName) && !TextUtils.isEmpty(getThingName) && !TextUtils.isEmpty(getValue)) {
                status = true;
            }
            return status;
        }
        @Override
        public void run() {
            if (getMessageFlag && checkParameters()) {
                mIotIgniteHandler.sendData(getNodeName, getThingName, getValue);
                getRunnableFlag = false;
                getMessageFlag = false;
                configurationDelayControl();
                if (Constant.DEBUG) {
                    Log.i(TAG, "Node : " + getNodeName + " - Thing : " + getThingName + " - Value : " + getValue + " - Delay : " + getDelayTime);
                }
            }
            if (sendDataHandler != null) {
                sendDataHandler.postDelayed(this, getDelayTime);
            }
        }
    };

    private Runnable configRead = new Runnable() {
        @Override
        public void run() {
            getDelayTime = (IotIgniteHandler.getInstance(mContext).getConfigurationTime(getNodeName + ":" + getThingName));
            if (getDelayTime == Constant.NOT_READ_CONFIGURATION || getDelayTime == Constant.NOT_READ_IGNITE_CONFIGURATION) {
                configReadHandler.postDelayed(this, CONFIG_READ_DELAY_TIME);
            }
        }
    };

    public ThreadUtils(Context context) {
        mContext = context;
        mIotIgniteHandler = IotIgniteHandler.getInstance(mContext);

        sendDataHandler = new Handler(Looper.getMainLooper());

        configReadHandler = new Handler(Looper.getMainLooper());
        LocalBroadcastManager.getInstance(context).registerReceiver(getIgniteConfig,
                new IntentFilter(Constant.IntentName.CONFIG));
        broadCastFlag = true;

        start();
    }



    public void parseData(String nodeName, String getThingName, String getValue) {
        if (Constant.DEBUG) {
            Log.i(TAG, "Parse Data Node Name : " + nodeName +
                    "\nThing Name : " + getThingName +
                    "\nMessage : " + getValue);
        }
        this.getNodeName = nodeName;
        this.getThingName = getThingName;
        this.getValue = getValue;
        this.getMessageFlag = true;
    }

    public void stopThread() {
        getDelayTime = 0;
        sendDataHandler.removeCallbacks(sendDataRunnable);
        sendDataHandler = null;
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        if (getRunnableFlag) {
            sendDataHandler.post(sendDataRunnable);
        }
    }

}
