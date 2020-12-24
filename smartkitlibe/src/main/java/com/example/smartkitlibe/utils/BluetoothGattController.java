package com.example.smartkitlibe.utils;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.smartkitlibe.Configure;
import com.example.smartkitlibe.instructions.Instructions;

import java.util.List;
import java.util.UUID;

//蓝牙的操作管理类
//去暴露响应的属性和方法
public class BluetoothGattController {
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private final String TAB = "manwei";
    private Instructions instructions = new Instructions();
    private Context mContext;

    //初始化 mBluetoothGatt
    public BluetoothGattController(BluetoothDevice device, Context context) {
        if (device != null) {
            mContext = context;
            mBluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
            if (mBluetoothGatt.connect()) {
                Log.d(TAB, "====蓝牙连接====");
                if (mBluetoothGatt.discoverServices()) {
                    Log.d(TAB, "====服务已经启动了====");
                } else {
                    Log.d(TAB, "====服务没有启动====");
                }
            } else {
                Log.d(TAB, "=====服务启动失败======");
            }
        }
    }


    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //连接状态更改
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.d("onConnectionStateChange", "连接成功");
            } else {
                Log.d("onConnectionStateChange", "连接断开");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //服务
            Log.d("onServicesDiscovered", "写入成功");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> supportedGattServices = gatt.getServices();
                for (int i = 0; i < supportedGattServices.size(); i++) {
                    Log.i("success", "1:BluetoothGattService UUID=:" + supportedGattServices.get(i).getUuid());
                    List<BluetoothGattCharacteristic> listGattCharacteristic = supportedGattServices.get(i).getCharacteristics();
                    for (int j = 0; j < listGattCharacteristic.size(); j++) {
                        Log.i("success", "2:   BluetoothGattCharacteristic UUID=:" + listGattCharacteristic.get(j).getUuid());
                    }
                }
            } else {
                Log.e("fail", "onservicesdiscovered收到: " + status);
            }
            //设置serviceUUID,原型是：BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID.fromString(SERVICESUUID));
            bluetoothGattService = gatt.getService(UUID.fromString(Configure.SPP_UUID));
            //设置写入特征UUID,原型是：BluetoothGattCharacteristic writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITEUUID));
            writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(Configure.WRITE_UUID));
            //设置监听特征UUID,原型是：BluetoothGattCharacteristic notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFYUUID));
            notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(Configure.NOTICE_UUID));
            //开启监听
            boolean b1 = gatt.setCharacteristicNotification(notifyCharacteristic, true);
            if (b1) {
                List<BluetoothGattDescriptor> descriptors = notifyCharacteristic.getDescriptors();
                if (descriptors != null && descriptors.size() > 0) {
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
            Log.d("uuidconnectsuccess", "uuid连接成功==b1==" + b1);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAB, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //第一步   写入成功了
            byte[] value = characteristic.getValue();
            Log.e(TAB, "=====点击的数据======" + BytesHexStrTranslate.bytesToHexFun1(value));

        }

        //接受数据回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            Log.e("ReceiveSuccess", BytesHexStrTranslate.bytesToHexFun1(value));
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", BytesHexStrTranslate.bytesToHexFun1(value));
            msg.setData(data);
            msg.what = 1;
            //发消息到主线程
//            handler.sendMessage(msg);
        }
    };


    //第一步   绑定请求
    public void getBindRequest() {
        if (mBluetoothGatt == null || writeCharacteristic == null) {
            return;
        }
        assert mBluetoothGatt != null;
        assert writeCharacteristic != null;
        writeCharacteristic.setValue(instructions.getBindRequestOne());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    //第二部 绑定成功反馈  完成绑定
    public void getBindingSuccessFeedback() {
        assert mBluetoothGatt != null;
        assert writeCharacteristic != null;
        writeCharacteristic.setValue(instructions.getBindRequestThree());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    //寻找药盒
    public void getLookingForTheKit() {
        assert mBluetoothGatt != null;
        assert writeCharacteristic != null;
        writeCharacteristic.setValue(instructions.getLookingForTheKit());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }


}
