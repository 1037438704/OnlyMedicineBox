package com.example.smartkitlibe.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 蓝牙适配器管理类
 */
public class BlueToothController {

    private BluetoothAdapter mAdapter;

    public BlueToothController() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //是否支持蓝牙
    //true 支持  false 不支持
    public boolean isSupportBlueTooth() {
        return mAdapter != null;
    }

    /**
     * 判断当前蓝牙状态
     * true 打开  false关闭
     */
    public boolean getBlueToothStatus() {
        //断言 拦截为空的作用吧
        assert (mAdapter != null);
        return mAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 扫描指定的
     */
    @SuppressLint("MissingPermission")
    public void findUUIDDevice(UUID[] uuids, BluetoothAdapter.LeScanCallback mLeScanCallback) {
        assert (mAdapter != null);
        mAdapter.startLeScan(uuids, mLeScanCallback);
    }

    /**
     * 关闭蓝牙
     */
    @SuppressLint("MissingPermission")
    public void turnOffBlueTooth() {
        mAdapter.disable();
    }
    /**
     * 停止扫描
     */
    @SuppressLint("MissingPermission")
    public void cancelDiscovery() {
        assert (mAdapter != null);
        mAdapter.cancelDiscovery();
    }

    /**
     * 查找设备
     */
    @SuppressLint("MissingPermission")
    public void findDevice() {
        assert (mAdapter != null);
        mAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void outSearchBlueTooth() {
        mAdapter.cancelDiscovery();
    }

    /**
     * 获取绑定设备
     */
    public List<BluetoothDevice> getBondedDeviceList() {
        return new ArrayList<>(mAdapter.getBondedDevices());
    }


}
