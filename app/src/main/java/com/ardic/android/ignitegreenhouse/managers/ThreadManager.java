package com.ardic.android.ignitegreenhouse.managers;

import android.os.Handler;
import android.util.Log;

/**
 * Created by acel on 7/7/17.
 */

public class ThreadManager extends Thread {

    private static final String TAG = ThreadManager.class.getSimpleName();
    private Handler sendDataHandler=new Handler();
    private String getDeviceId;
    private String getValue;
    private long getDelayTime=10000L;


    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"Get New Thread Runnable" );
            sendDataHandler.postDelayed(this,getDelayTime);
        }
    };

    public ThreadManager(){
    }

    public void getData(String getDeviceId, String getValue,long getDelayTime){
        this.getDeviceId=getDeviceId;
        this.getValue=getValue;
        this.getDelayTime=getDelayTime;
    }


    @Override
    public void run() {
        super.run();
        sendDataHandler.post(sendDataRunnable);
    }

}
