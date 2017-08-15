package com.ardic.android.ignitegreenhouse.utils;

import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;

/**
 * Created by Mert Acel on 8/7/17.
 */

public class LogUtils {

    public static void logger(String tag, String message){
        if (Constant.DEBUG) {
            Log.i(tag, message);
        }
    }
}
