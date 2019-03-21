package com.hy.ble.receive.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.ble.receive.R;
import com.hy.ble.receive.base.Common;
import com.hy.ble.receive.bean.BluetoothDeviceBean;
import com.hy.ble.receive.helper.BluetoothHelper;
import com.hy.ble.receive.recyclerview.LeDeviceListAdapter;
import com.hy.ble.receive.utils.RxUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <pre>
 *     author : Agg
 *     blog   : https://blog.csdn.net/Agg_bin
 *     time   : 2019/03/13
 *     desc   :
 *     reference :
 * </pre>
 */
public class MainActivity extends RxAppCompatActivity {

    private LeDeviceListAdapter leDeviceListAdapter;
    @BindView(R.id.rv_devices)
    RecyclerView rvDevices;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.et_info)
    EditText etInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        askPermission();
        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Common.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "未授予模糊定位权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Common.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    @SuppressLint("CheckResult")
    private void init() {
        etInfo.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etInfo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                BluetoothHelper.getInstance().writeData(etInfo.getText().toString().trim());
                hideSoftInput();
                etInfo.setText("");
                return true;
            }
            return false;
        });
        leDeviceListAdapter = new LeDeviceListAdapter();
        leDeviceListAdapter.setItemClickListener(deviceBean -> {
            BluetoothHelper.getInstance().stopScanLeDevice();
            BluetoothHelper.getInstance().release();
            BluetoothHelper.getInstance().connectGatt(deviceBean);
        });
        rvDevices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvDevices.setAdapter(leDeviceListAdapter);
        RxUtils.filterClick(tvScan, Common.SCAN_PERIOD)
                .compose(this.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(o -> {
                    if (!BluetoothHelper.getInstance().isEnabled()) {
                        Toast.makeText(MainActivity.this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    leDeviceListAdapter.clearData();
                    leDeviceListAdapter.notifyDataSetChanged();
                    BluetoothHelper.getInstance().scanLeDevice();
                });
        // 设置回调需要在初始化之前哦
        BluetoothHelper.getInstance().setItemClickListener(
                deviceBean -> addDevice(deviceBean.getBluetoothDevice(), deviceBean.getRssi()),
                device -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "连接成功" + device.getName() + "----" + device.getAddress(), Toast.LENGTH_SHORT).show()),
                this::finish);
        BluetoothHelper.getInstance().init(this);
        BluetoothHelper.getInstance().openBluetooth();
    }

    private void addDevice(BluetoothDevice bluetoothDevice, int rssi) {
        runOnUiThread(() -> {
            BluetoothDeviceBean bluetoothDeviceBean = new BluetoothDeviceBean(bluetoothDevice, rssi);
            boolean tag = false;
            for (BluetoothDeviceBean deviceBean : leDeviceListAdapter.deviceList) {
                if (deviceBean.getBluetoothDevice().equals(bluetoothDevice)) {
                    tag = true;
                    break;
                }
            }
            if (!tag) {
                leDeviceListAdapter.addDevice(bluetoothDeviceBean);
                leDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(etInfo.getWindowToken(), 0); // 强制隐藏键盘
    }

    @OnClick({R.id.tv_notify, R.id.tv_write, R.id.tv_read})
    public void onClick(View view) {
        if (!BluetoothHelper.getInstance().isEnabled()) {
            Toast.makeText(MainActivity.this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.tv_notify:
                boolean enableNotification = BluetoothHelper.getInstance().enableNotification(true);
                Toast.makeText(this, "打开通知：" + enableNotification, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_read:
                BluetoothHelper.getInstance().readData();
                break;
            case R.id.tv_write:
                if (etInfo.getVisibility() == View.VISIBLE) etInfo.setVisibility(View.INVISIBLE);
                else etInfo.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothHelper.getInstance().release();
    }

}
