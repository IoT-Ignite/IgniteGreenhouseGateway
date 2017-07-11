package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by acel on 7/3/17.
 */

public class UartManager {

    private static final String TAG = UartManager.class.getSimpleName();

    private UartDevice mUartDevice;
    private PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

    /** UART Configuration Parameters*/
    private static final int BAUD_RATE = 4800;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 35;
    private static final String DEVICE_RPI3 = "UART0";
    //
    // private static final String REGEXP_VALUE = "[0-9]+.+[0-9]";
    // private static final String REGEXP2 = "\"id\":[0-9a-fA-F]+,\"val\":[0-9]++";
    private static final String REGEXP_ID = "[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]";

    private static final String GET_ID_STRING = "id";
    private static final String GET_VALUE_STRING = "val";
    private static final String GET_BEGIN_CHARACTER="~##";
    private static final String GET_END_CHARACTER="!!~";

    private Handler sendDataHandler = new Handler();

    private boolean getUartFlag = false;

     private DataManager mDataManager;

    private long getSendDataTime = 3000L;
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
            sendDataHandler.postDelayed(this, getSendDataTime);

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
            mDataManager= new DataManager(context);
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

        /** Configure the UART*/
        mUartDevice.setBaudrate(baudRate);
        mUartDevice.setDataSize(DATA_BITS);
        mUartDevice.setParity(UartDevice.PARITY_NONE);
        mUartDevice.setStopBits(STOP_BITS);
        mUartDevice.registerUartDeviceCallback(getUartCallback);
    }

    /**
     * Close the UART device connection, if it exists
     */
    public void closeUart() throws IOException {
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
    public void transferUartData() {
        if (mUartDevice != null) {
            // Loop until there is no more data in the RX buffer.
            try {
                final byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = mUartDevice.read(buffer, buffer.length)) == CHUNK_SIZE) {
                    mUartDevice.write(buffer, read);
                    /** Read Data */
                    String incomingData = new String(buffer);
                    String controlComingData = null;

                    /** For Control True Data*/
                    int beginCharacterIndex = incomingData.indexOf(GET_BEGIN_CHARACTER);
                    int endCharacterIndex = incomingData.indexOf(GET_END_CHARACTER);

                    if (incomingData.contains(GET_BEGIN_CHARACTER) && incomingData.contains(GET_END_CHARACTER)) {
                        if (beginCharacterIndex < endCharacterIndex) {
                            /** Split */
                            controlComingData = incomingData.substring(beginCharacterIndex + 3, endCharacterIndex);
                        }
                    }

                    if (controlComingData != null) {
                        try {
                            /** Convert Json*/
                            JSONObject mDataObject = new JSONObject(controlComingData);
                            String getSensorId = null;
                            String getSensorValue = null;

                            if (mDataObject != null && mDataObject.has(GET_ID_STRING) && mDataObject.has(GET_VALUE_STRING)) {
                                getSensorId = mDataObject.getString(GET_ID_STRING);
                                getSensorValue = mDataObject.getString(GET_VALUE_STRING);

                                /** Control end Send Data Manager*/
                                if (!TextUtils.isEmpty(getSensorId) && !TextUtils.isEmpty(getSensorValue) && getSensorId.matches(REGEXP_ID)) {
                                    mDataManager.parseData(getSensorId,getSensorValue);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
