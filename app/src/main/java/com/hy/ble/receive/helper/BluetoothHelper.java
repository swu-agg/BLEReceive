package com.hy.ble.receive.helper;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.hy.ble.receive.base.Common;
import com.hy.ble.receive.bean.BluetoothDeviceBean;
import com.hy.ble.receive.utils.BleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/14
 *     desc      :
 *
 *  1、是否有权限
 *  2、是否支持蓝牙4.0
 *  3、蓝牙是否开启
 *  4、设备搜索
 *  5、设备连接
 *  6、蓝牙通信
 *
 *     reference :
 *     一个系列四篇文章：https://www.jianshu.com/p/dc67e6fe036b
 *     一系列几篇文章：http://a1anwang.com/post-47.html
 * </pre>
 */
public class BluetoothHelper {

    private static final String TAG = BluetoothHelper.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static BluetoothHelper bluetoothHelper;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt; // 作为中央来使用和处理数据
    private Common.OnDeviceCallBack onDeviceCallBack;
    private BluetoothGattCharacteristic characteristicNotification;
    private Common.OnConnectDeviceCallBack onConnectDeviceCallBack;
    private Common.OnCloseActivity onCloseActivity;
    private Handler mainHandler;
    private Context context;

    private BluetoothHelper() {
    }

    public static BluetoothHelper getInstance() {
        if (bluetoothHelper == null) {
            synchronized (BluetoothHelper.class) {
                if (bluetoothHelper == null) {
                    bluetoothHelper = new BluetoothHelper();
                }
            }
        }
        return bluetoothHelper;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        mainHandler = new Handler();

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mainHandler.post(() -> Toast.makeText(context, "蓝牙不支持BLE", Toast.LENGTH_SHORT).show());
            onCloseActivity.onClose();
        } else if (BluetoothAdapter.getDefaultAdapter() == null) {
            mainHandler.post(() -> Toast.makeText(context, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show());
            onCloseActivity.onClose();
        }
    }

    public void openBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean enable = bluetoothAdapter.enable();// 自动打开蓝牙
        if (!enable) {
            mainHandler.post(() -> Toast.makeText(context, "请打开蓝牙", Toast.LENGTH_SHORT).show());
            onCloseActivity.onClose();
        }
    }

    /**
     * 检测蓝牙是否已启用
     *
     * @return 启用为true，未启用为false
     */
    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void scanLeDevice() {
        // 参考：http://a1anwang.com/post-37.html
        if (bluetoothAdapter != null) {
            //  在10秒后停止搜索
            mainHandler.postDelayed(() -> {
                if (bluetoothAdapter.isEnabled()) bluetoothLeScanner.stopScan(scanCallback);
            }, Common.SCAN_PERIOD);
            // 在广播字段里加入特定的server uuid， app扫描的时候可以过滤其他BLE设备。
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(createScanFilter(), new ScanSettings.Builder().build(), scanCallback);
        } else {
            Log.i(TAG, "scanLeDevice: 蓝牙未初始化");
        }
    }

    private List<ScanFilter> createScanFilter() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(Common.PARCEL_UUID_1).build());
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(Common.PARCEL_UUID_2).build());
        return scanFilters;
    }

    public void stopScanLeDevice() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        } else {
            Log.i(TAG, "scanLeDevice: 蓝牙未初始化");
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // rssi：RSSI的值作为对远程蓝牙设备信号值，正常为负值; 值越大信号越强;
            // scanRecord：远程设备提供的配对号，一般用不到。
            Log.i(TAG, "scanRecord: " + BleUtil.toHexString(Objects.requireNonNull(result.getScanRecord()).getBytes())
                    + ",address：" + result.getDevice().getAddress());
            onDeviceCallBack.onItemClick(new BluetoothDeviceBean(result.getDevice(), result.getRssi()));
        }
    };

    public void connectGatt(BluetoothDeviceBean deviceBean) {
        // 实测发现，用false连接比较好，比较快， true会等个十几秒甚至几分钟才会连接上。  开发过程中一般都是用false，扫描到bluetoothdevice之后，直接用false连接即可。
        bluetoothGatt = deviceBean.getBluetoothDevice().connectGatt(context, false, bluetoothGattCallback);
    }

    // BluetoothGattCallback是返回中央的状态和周边提供的数据的一个重要的抽象类
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 当GATT客户端已连接到GATT服务器或者从GATT服务器断开连接
         * 时回调。
         *
         * @param gatt gatt
         * @param status 连接或断开操作的状态。BluetoothGatt.GATT_SUCCESS表示操作成功
         * @param newState 返回新的连接状态。 如 BluetoothProfile.STATE_DISCONNECTED或BluetoothProfile.STATE_CONNECTED
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "连接成功");
                onConnectDeviceCallBack.onConnect(gatt.getDevice());
                // 查找服务,在成功连接到远程设备时调用，不调用此方法，无法与远程设备进行后续的通信。
                // 但是这个方法是异步操作，在回调函数onServicesDiscovered中得到status，通过判断status是否等于BluetoothGatt.GATT_SUCCESS来判断查找Service是否成功.
                gatt.discoverServices();
                // 有可能自动重连
//                bluetoothGatt = gatt;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "连接断开");
                gatt.connect();
            }
        }

        /**
         * 当远程设备的远程服务列表，特征和描述符已被更新，即已发现新服务时，调用回调。
         *
         * @param gatt gatt
         * @param status BluetoothGatt.GATT_SUCCESS 远程设备的远程服务列表可被发现
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "发现服务成功");
//                for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {
//                    Log.i(TAG, "connectGatt: " + bluetoothGattService.getUuid().toString());
//                }
            } else {
                Log.i(TAG, "发现服务失败");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String value = new String(characteristic.getValue());
                Log.i(TAG, "onCharacteristicWrite:Uuid= " + characteristic.getUuid() + ",value=" + value);
                mainHandler.post(() -> Toast.makeText(context, "发送值成功：" + "\"" + value + "\"", Toast.LENGTH_SHORT).show());
            }
        }

        /**
         * Read特性的操作回调
         *
         * @param gatt gatt
         * @param characteristic 从相关的远程设备读取的特性。
         * @param status BluetoothGatt.GATT_SUCCESS 操作成功
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 读取到值,根据UUID来判断读到的是什么值
                if (characteristic.getUuid().equals(Common.CHARACTERISTIC_UUID_2)) {
                    mainHandler.post(() -> Toast.makeText(context, "读取到值：" + "\"" + new String(characteristic.getValue()) + "\"", Toast.LENGTH_SHORT).show());
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mainHandler.post(() -> Toast.makeText(context, "可以收到BLE设备通知！", Toast.LENGTH_SHORT).show());
            }
        }

        /**
         * notification 特性的结果
         *
         * @param gatt gatt
         * @param characteristic 由于远程通知事件而更新的特性。
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //收到设备notify值 （设备上报值）
            Log.i(TAG, "onCharacteristicChanged: " + new String(characteristic.getValue()));
            mainHandler.post(() -> Toast.makeText(context, "收到通知：" + new String(characteristic.getValue()), Toast.LENGTH_SHORT).show());
        }
    };

    /**
     * 设置可以接收通知
     * 如果notificaiton方式对于某个Characteristic是enable的，那么当设备上的这个Characteristic改变时，手机上的onCharacteristicChanged()回调就会被促发。
     */
    public boolean enableNotification(boolean enable) {
        if (bluetoothGatt == null) return false;
        BluetoothGattService service = bluetoothGatt.getService(Common.SERVICE_UUID_2);
        if (service == null) return false;
        characteristicNotification = service.getCharacteristic(Common.CHARACTERISTIC_UUID_3);
        if (characteristicNotification == null) return false;
        if (!bluetoothGatt.setCharacteristicNotification(characteristicNotification, true))
            return false;//激活通知
        BluetoothGattDescriptor descriptor = characteristicNotification.getDescriptor(Common.DESCRIPTOR_UUID);
        if (descriptor == null) return false;
        if (enable) descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        else descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        return bluetoothGatt.writeDescriptor(descriptor); // 会回调bel设备的onDescriptorWriteRequest
    }

    /**
     * 发送数据
     * <p>
     * 发送数据给周边设备，会在onCharacteristicWrite()回调中返回
     *
     * @param data 数据
     */
    public void writeData(String data) {
        if (bluetoothGatt == null) {
            mainHandler.post(() -> Toast.makeText(context, "请先连接蓝牙！", Toast.LENGTH_LONG).show());
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(Common.SERVICE_UUID_1);
        if (service == null) return;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(Common.CHARACTERISTIC_UUID_1);
        if (characteristic == null) return;
        characteristic.setValue(data.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 读取数据
     * <p>
     * 读周边设备发来数据，会在onCharacteristicRead()回调中返回
     * SERVICE_UUID_1，characteristicUuid2是可以变化的（开发时需要询问硬件那边）
     */
    public void readData() {
        if (bluetoothGatt == null) {
            mainHandler.post(() -> Toast.makeText(context, "请先连接蓝牙！", Toast.LENGTH_LONG).show());
            return;
        }

        // 官方建议在进行read是如果有通知服务进行中，先关闭
        if (characteristicNotification != null)
            bluetoothGatt.setCharacteristicNotification(characteristicNotification, false);

        BluetoothGattService service = bluetoothGatt.getService(Common.SERVICE_UUID_1);
        if (service == null) return;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(Common.CHARACTERISTIC_UUID_2);
        if (characteristic == null) return;
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void setItemClickListener(Common.OnDeviceCallBack onDeviceCallBack, Common.OnConnectDeviceCallBack onConnectDeviceCallBack, Common.OnCloseActivity onCloseActivity) {
        this.onDeviceCallBack = onDeviceCallBack;
        this.onConnectDeviceCallBack = onConnectDeviceCallBack;
        this.onCloseActivity = onCloseActivity;
    }

    public void release() {
        if (bluetoothGatt == null) return;
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

}
