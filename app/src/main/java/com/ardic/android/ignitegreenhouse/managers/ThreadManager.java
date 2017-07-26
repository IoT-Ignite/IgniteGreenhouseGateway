package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.utils.NodeThingUtils;
import com.ardic.android.ignitegreenhouse.utils.ThreadUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/21/17.
 */

public class ThreadManager {

    private static final String TAG = ThreadManager.class.getSimpleName();

    private static ThreadManager instance = null;
    private NodeThingUtils mNodeThingUtils;
    private Context mContext;

    /**It contains threads created for each sensor.
     * Format;
     * Key: "NodeName:ThingName"
     * Value: "Thread".
     * It provides control by "node name" and "thing name" in this view
     */
    private Map<String, ThreadUtils> threadControl = new ArrayMap<>();


    private ThreadManager(Context context) {
        mContext = context;
        if (context != null) {
            mNodeThingUtils = NodeThingUtils.getInstance(mContext);
        }
    }

    public static synchronized ThreadManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThreadManager(context);
        }
        return instance;
    }

    /**
     * "Preference" also records all sensors that are not in the "threadControl"
     */
    public void threadManager() {
        /**Retrieve all data stored in "preference"*/
        Map<String, String> getAllSensor = mNodeThingUtils.getSavedAllThing();
        Set<String> keys = getAllSensor.keySet();

        /**It checks to see if he has already been registered*/
        if (!threadControl.containsKey(String.valueOf(keys))) {
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                /**This control is used to separate the thread type from the sensor type*/
                if (!key.matches(Constant.THREAD_CONTROL_REGEXP) && key.length() != 2) {

                    /**Creates a new thread and registers it as "nodename: thingname"
                     * of type "map" so that it is easy to check later*/
                    threadControl.put(key, new ThreadUtils(mContext));
                }
            }
        } else {
            if (Constant.DEBUG) {
                Log.e(TAG, "There is a thread named" + keys);
            }
        }
    }

    /**"Node name" and "thing name", and delete the "thread" in this information.*/
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

    /**Delete all "threads"*/
    public void killAllThread() {
        threadControl.clear();
    }



    /**Gives the number of active threads and size*/
    public void threadStatusLog(){
        Log.i(TAG, "All Run Thread Size : " + threadControl.size() +
                "\nAll Run Thread : " + threadControl);

    }

    /**Return the active "thread" class*/
    public ThreadUtils getThreadOperation(String threadKey) {
        if (threadControl.containsKey(threadKey)) {
            return threadControl.get(threadKey);
        }
        return null;
    }

    /**Check whether the given name is a thread*/
    public boolean isThread(String threadKey) {
        return threadControl.containsKey(threadKey);
    }

    public boolean controlThread(String threadKey){
        boolean status = false;
        if (isThread(threadKey) && getThreadOperation(threadKey) != null){
            status= true;
        }
        return status;
    }
}
