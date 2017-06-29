package com.ardic.android.ignitegreenhouse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dorukgezici on 22/06/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Boot broadcast received!! Starting application");
        Intent applicationIntent = new Intent(context, MainActivity.class);
        context.startActivity(applicationIntent);
    }
}
