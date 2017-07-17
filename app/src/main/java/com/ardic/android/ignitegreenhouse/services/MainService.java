package com.ardic.android.ignitegreenhouse.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;
import com.ardic.android.ignitegreenhouse.managers.UartManager;
import com.ardic.android.ignitegreenhouse.model.Constant;

import java.io.IOException;

public class MainService extends Service {
    /**
     * "MainService" was made to perform background processes even
     * if the interface of the program is closed.
     */

    private static final String TAG =MainService.class.getSimpleName();
    private static final String INTENT_FILTER_IGNITE_STATUS = "igniteConnect";
    private UartManager mUartManager;

    private boolean getIgniteStatus= false;

    /**
     *It takes the "connect" method broadcast from "IoT - Ignite".
     * If "IoT - Ignite" status is "Connect", it initiates "Uart" operations.
     */
    private BroadcastReceiver igniteStatusMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getIgniteStatus = intent.getBooleanExtra("igniteStatus", false);
            if (Constant.DEBUG) {
                Log.i(TAG, "Ignite Status : " + getIgniteStatus);
            }
            try {
                if (getIgniteStatus) {
                    mUartManager=new UartManager(getApplicationContext());
                    mUartManager.openUart();
                }
            } catch (IOException e) {
                if (Constant.DEBUG) {
                    Log.e(TAG, "Uart Open Failed : " + e);
                }
                return;
            }
        }
    };

    public MainService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(igniteStatusMessage,
                new IntentFilter(INTENT_FILTER_IGNITE_STATUS));
        IotIgniteHandler.getInstance(this).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mUartManager.closeUart();
        IotIgniteHandler.getInstance(this).shutdown();
        super.onDestroy();
    }
}
