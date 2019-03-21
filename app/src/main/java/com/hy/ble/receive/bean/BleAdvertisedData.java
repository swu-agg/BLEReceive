package com.hy.ble.receive.bean;

import java.util.List;
import java.util.UUID;

/**
 * <pre>
 *     author : Agg
 *     blog   : https://blog.csdn.net/Agg_bin
 *     time   : 2019/03/13
 *     desc   :
 *     reference :
 * </pre>
 */
public class BleAdvertisedData {

    private List<UUID> mUuids;
    private String mName;

    public BleAdvertisedData(List<UUID> uuids, String name) {
        mUuids = uuids;
        mName = name;
    }

    public List<UUID> getUuids() {
        return mUuids;
    }

    public String getName() {
        return mName;
    }

}
