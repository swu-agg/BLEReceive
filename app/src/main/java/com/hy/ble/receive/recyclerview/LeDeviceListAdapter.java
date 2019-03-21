package com.hy.ble.receive.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hy.ble.receive.R;
import com.hy.ble.receive.base.Common;
import com.hy.ble.receive.bean.BluetoothDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/13
 *     desc      :
 *     reference :
 * </pre>
 */
public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListHolder> {

    public List<BluetoothDeviceBean> deviceList = new ArrayList<>();
    private Common.OnDeviceCallBack onDeviceCallBack;

    public void addDevice(BluetoothDeviceBean deviceBean) {
        deviceList.add(deviceBean);
    }

    public void clearData() {
        deviceList.clear();
    }

    @NonNull
    @Override
    public LeDeviceListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LeDeviceListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_devices, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LeDeviceListHolder holder, int position) {
        holder.init(position, deviceList.get(position));
        holder.itemView.setOnClickListener(view -> {
            if (onDeviceCallBack != null) {
                onDeviceCallBack.onItemClick(deviceList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setItemClickListener(Common.OnDeviceCallBack onDeviceCallBack) {
        this.onDeviceCallBack = onDeviceCallBack;
    }

}
