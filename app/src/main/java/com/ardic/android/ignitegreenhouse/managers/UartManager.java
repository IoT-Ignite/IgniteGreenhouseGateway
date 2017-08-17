package com.ardic.android.ignitegreenhouse.managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.utils.LogUtils;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Mert Acel on 7/3/17.
 */

public class UartManager {

    private static final String TAG = UartManager.class.getSimpleName();

    private UartDevice mUartDevice;
    private PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

    /**
     * UART MessageManager Parameters
     */
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 31;
    private static final String DEVICE_RPI3 = "UART0";

    private static final String GET_ID_STRING = "id";
    private static final String GET_VALUE_STRING = "val";
    private static final String GET_BEGIN_CHARACTER = "~##";
    private static final String GET_END_CHARACTER = "!!~";

    private DataManager mDataManager;

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback getUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            readUartData();
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.e(TAG, uart + ": Error event " + error);
        }
    };

    /**
     * "Constructor Method" is used to define "local broadcast"
     * , define context and start sendDataRunnable
     */
    public UartManager(Context context) {
        LogUtils.logger(TAG, "UartManager Open .");
        if (context != null) {
            mDataManager = DataManager.getInstance(context);
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
    public void closeUart() {
        if (mUartDevice != null) {
            mUartDevice.unregisterUartDeviceCallback(getUartCallback);
            try {
                mUartDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "closeUart Error : " + e);
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
    public void readUartData() {
        Thread transferUartDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mUartDevice != null) {
                    try {
                        final byte[] buffer = new byte[CHUNK_SIZE];
                        int read;
                        while ((read = mUartDevice.read(buffer, buffer.length)) == CHUNK_SIZE) {
                            mUartDevice.write(buffer, read);
                            /** Read Data */
                            String incomingData = new String(buffer);
                            /** For Control True Data*/
                            int beginCharacterIndex = incomingData.indexOf(GET_BEGIN_CHARACTER);
                            int endCharacterIndex = incomingData.indexOf(GET_END_CHARACTER);

                            dataInterpretation(incomingData, beginCharacterIndex, endCharacterIndex);
                        }
                    } catch (IOException e1) {
                        Log.e(TAG, "readUartData Error : " + e1);
                    }
                }
            }
        });
        transferUartDataThread.start();
    }

    private void dataInterpretation(String incomingData, int beginCharacterIndex, int endCharacterIndex) {
        String controlComingData = null;
        if (incomingData.contains(GET_BEGIN_CHARACTER)
                && incomingData.contains(GET_END_CHARACTER)
                && beginCharacterIndex < endCharacterIndex) {
            /** Split */
            controlComingData = incomingData.substring(beginCharacterIndex + 3, endCharacterIndex);
        }

        if (controlComingData != null) {
            controlUartData(controlComingData);
        }
    }

    private void controlUartData(String controlComingData) {
        try {
            /** Convert Json*/
            JSONObject mDataObject = new JSONObject(controlComingData);
            String getSensorId = null;
            String getSensorValue = null;
            if (mDataObject != null && mDataObject.has(GET_ID_STRING) && mDataObject.has(GET_VALUE_STRING)) {
                getSensorId = mDataObject.getString(GET_ID_STRING);
                getSensorValue = mDataObject.getString(GET_VALUE_STRING);
                sendUartData(getSensorId, getSensorValue);
            }
        } catch (JSONException e) {
            Log.e(TAG, "mDataObject Json Error : " + e);
        }
    }

    private void sendUartData(String getSensorId, String getSensorValue) {
        /** Control end Send Data Manager*/
        if (!TextUtils.isEmpty(getSensorId) && !TextUtils.isEmpty(getSensorValue) && getSensorId.matches(Constant.Regexp.SENSOR_CONTROL)) {
            mDataManager.parseData(getSensorId, getSensorValue);
            LogUtils.logger(TAG, "GET UART DATA : " + getSensorId + " - " + getSensorValue);

        }
    }
}
