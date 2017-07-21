package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.operations.NodeThingOperations;
import com.ardic.android.ignitegreenhouse.operations.ThreadOperations;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/21/17.
 */

public class ThreadManager {

    private static final String TAG = ThreadManager.class.getSimpleName();

    private static ThreadManager INSTANCE = null;
    private NodeThingOperations mNodeThingOperations;
    private Context mContext;

    private Map<String, ThreadOperations> threadControl = new ArrayMap<>();


    private ThreadManager(Context context) {
        mContext = context;
        if (context != null) {
            mNodeThingOperations = NodeThingOperations.getInstance(mContext);
        }
    }

    public static synchronized ThreadManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ThreadManager(context);
        }
        return INSTANCE;
    }

    public void threadManager() {
        Map<String, ?> getAllSensor = mNodeThingOperations.getSavedAllThing();
        Set keys = getAllSensor.keySet();
        if (!threadControl.containsKey(keys)) {
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String key = (String) i.next();

                /**This control is used to separate the thread type from the sensor type*/
                if (!key.matches(Constant.THREAD_CONTROL_REGEXP) && key.length() != 2) {
                    threadControl.put(key, new ThreadOperations(mContext));
                }
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

    public int getThreadControlSize() {
        return threadControl.size();
    }

    public Map<String, ThreadOperations> getRunThread() {
        Map<String, ThreadOperations> threadControlKey = threadControl;
        return threadControlKey;
    }

    public ThreadOperations getThreadOperation(String threadKey) {
        return threadControl.get(threadKey);
    }

    public boolean isThread(String threadKey) {
        return threadControl.containsKey(threadKey);
    }
}
