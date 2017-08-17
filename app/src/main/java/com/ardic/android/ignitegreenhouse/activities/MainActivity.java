package com.ardic.android.ignitegreenhouse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.R;
import com.ardic.android.ignitegreenhouse.services.MainService;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Starting Main Service
         */
        startService(new Intent(this,MainService.class));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Main Activity onDestroy");
        super.onDestroy();
    }
}