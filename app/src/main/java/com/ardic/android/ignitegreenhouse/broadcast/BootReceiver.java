package com.ardic.android.ignitegreenhouse.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ardic.android.ignitegreenhouse.activities.MainActivity;
import com.ardic.android.ignitegreenhouse.utils.LogUtils;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtils.logger(TAG, "Boot broadcast received!! Starting application");

            Intent applicationIntent = new Intent(context, MainActivity.class);
            context.startActivity(applicationIntent);
        }
    }
}
