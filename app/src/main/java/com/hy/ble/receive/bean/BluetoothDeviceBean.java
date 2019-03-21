package com.hy.ble.receive.bean;

import android.bluetooth.BluetoothDevice;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/13
 *     desc      :
 *     reference :
 * </pre>
 */
public class BluetoothDeviceBean {

    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BluetoothDeviceBean(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
