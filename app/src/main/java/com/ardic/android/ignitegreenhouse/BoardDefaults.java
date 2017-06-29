package com.ardic.android.ignitegreenhouse;

import android.os.Build;

@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_JOULE = "joule";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX6UL_PICO = "imx6ul_pico";
    private static final String DEVICE_IMX6UL_VVDN = "imx6ul_iopb";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";

    /**
     * Return the UART for loopback.
     */
    public static String getUartName() {
        switch (Build.DEVICE) {
            case DEVICE_EDISON:
                return "UART1";
            case DEVICE_JOULE:
                return "UART1";
            case DEVICE_RPI3:
                return "UART0";
            case DEVICE_IMX6UL_PICO:
                return "UART3";
            case DEVICE_IMX6UL_VVDN:
                return "UART2";
            case DEVICE_IMX7D_PICO:
                return "UART6";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
