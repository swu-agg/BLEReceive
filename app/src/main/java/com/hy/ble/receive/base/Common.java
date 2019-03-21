package com.hy.ble.receive.base;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import com.hy.ble.receive.bean.BluetoothDeviceBean;

import java.util.UUID;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/14
 *     desc      :
 *     reference :
 *     remark    : UUID--128 bit(32位16进制)
 *                  蓝牙技术联盟定义UUID共用了一个基本的UUID：0x0000xxxx-0000-1000-8000-00805F9B34FB
 * </pre>
 */
public class Common {

    public static final ParcelUuid PARCEL_UUID_1 = ParcelUuid.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid PARCEL_UUID_2 = ParcelUuid.fromString("0000bbb0-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVICE_UUID_1 = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVICE_UUID_2 = UUID.fromString("0000bbb0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID_1 = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID_2 = UUID.fromString("0000ccc2-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID_3 = UUID.fromString("0000bbb1-0000-1000-8000-00805f9b34fb");
    public static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    public static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.

    public interface OnDeviceCallBack {
        void onItemClick(BluetoothDeviceBean deviceBean);
    }

    public interface OnConnectDeviceCallBack {
        void onConnect(BluetoothDevice device);
    }

    public interface OnCloseActivity {
        void onClose();
    }

}
