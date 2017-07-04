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
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

/**
 * Created by acel on 7/3/17.
 */

public class UartManager {

    private static final String TAG = UartManager.class.getSimpleName();
    private UartDevice mUartDevice;
    private PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

    // UART Configuration Parameters
    private static final int BAUD_RATE = 4800;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 5;
    private static String DEVICE_RPI3 = "UART0";
    private static final String REGEXP = "[0-9]+.+[0-9]";

    private String temperatureReadBuffer;
    private String getTemperatureMessage = "Temperature: ";

    private Context mContext;

    private long getDelayTimeData = 10000L;
    private Handler sendDataHandler = new Handler();

    private boolean getUartFlag = false;

    /**
     * To send a data cloud to the configuration without
     */
    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (getUartFlag) {
                transferUartData();
                getUartFlag = false;
            }
            sendDataHandler.postDelayed(this, getDelayTimeData);

        }
    };

    /**
     * Receive cloud configuration data
     */
    private BroadcastReceiver getIgniteConfig = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getDelayTimeData = intent.getLongExtra("getConfig", 10000L);
            Log.i(TAG, "Get Config : " + getDelayTimeData + " sec");
        }
    };


    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback getUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            getUartFlag = true;
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            if (sendDataRunnable != null) {
                Log.w(TAG, uart + ": Error event " + error);
                sendDataHandler.removeCallbacks(sendDataRunnable);
            }
        }
    };

    /**
     * "Constructor Method" is used to define "local broadcast"
     * , define context and start sendDataRunnable
     */
    public UartManager(Context context) {
        Log.i(TAG, "UartManager Open .");
        if (context != null) {
            mContext = context;
            LocalBroadcastManager.getInstance(mContext).registerReceiver(getIgniteConfig,
                    new IntentFilter("getConfig"));
            if (sendDataRunnable != null) {
                sendDataHandler.post(sendDataRunnable);
            }
        }
    }

    /**
     * Open Uart and settings
     */
    public void openUart() throws IOException {
        openUart(DEVICE_RPI3, BAUD_RATE);
    }

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name     Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     *                 such as 9600, 19200, 38400, 57600, 115200, etc.
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {
        mUartDevice = peripheralManagerService.openUartDevice(name);
        // Configure the UART
        mUartDevice.setBaudrate(baudRate);
        mUartDevice.setDataSize(DATA_BITS);
        mUartDevice.setParity(UartDevice.PARITY_NONE);
        mUartDevice.setStopBits(STOP_BITS);
        mUartDevice.registerUartDeviceCallback(getUartCallback);
    }

    /**
     * Close the UART device connection, if it exists
     */
    private void closeUart() throws IOException {
        if (mUartDevice != null) {
            mUartDevice.unregisterUartDeviceCallback(getUartCallback);
            try {
                mUartDevice.close();
            } finally {
                mUartDevice = null;
            }
        }
    }

    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     * <p>
     * Potentially long-running operation. Call from a worker thread.
     */
    private void transferUartData() {
        if (mUartDevice != null) {
            // Loop until there is no more data in the RX buffer.
            try {
                final byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = mUartDevice.read(buffer, buffer.length)) == CHUNK_SIZE) {
                    mUartDevice.write(buffer, read);
                    temperatureReadBuffer = new String(buffer);
                    getTemperatureMessage = "Temperature: " + temperatureReadBuffer + " °C";

                    if (!TextUtils.isEmpty(temperatureReadBuffer) && temperatureReadBuffer.matches(REGEXP)) {
                        Log.i(TAG, "Get Temperature : " + temperatureReadBuffer);
                        if (IotIgniteHandler.getInstance(mContext).sendData(Float.parseFloat(temperatureReadBuffer))) {
                            Log.i(TAG, "Send Data : OK");
                        } else {
                            Log.e(TAG, "Send Data : NO");
                        }
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
        }
    }
}
