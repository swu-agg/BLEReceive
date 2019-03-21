package com.hy.ble.receive.recyclerview;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hy.ble.receive.R;
import com.hy.ble.receive.bean.BluetoothDeviceBean;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/13
 *     desc      :
 *     reference :
 * </pre>
 */
public class LeDeviceListHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_info)
    TextView tvInfo;

    LeDeviceListHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @SuppressLint("SetTextI18n")
    public void init(int position, BluetoothDeviceBean bluetoothDeviceBean) {
        BluetoothDevice bluetoothDevice = bluetoothDeviceBean.getBluetoothDevice(); // rssi：接收信号强度指示器（Received Signal Strength Indicator）
        tvInfo.setText(position + 1 + "、设备名：" + bluetoothDevice.getName() + "----地址：" + bluetoothDevice.getAddress() + "----rssi：" + bluetoothDeviceBean.getRssi());
    }
}
