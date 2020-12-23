package com.example.onlymedicinebox;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartkitlibe.instructions.Instructions;
import com.example.smartkitlibe.utils.BlueToothController;
import com.example.smartkitlibe.utils.BytesHexStrTranslate;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private Toast mToast;
    private BlueToothController mController;
    private String TAB = "zdl";
    private Instructions instructions;
    private BytesHexStrTranslate bytesHexStrTranslate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        //初始化组件
        initView();
        // 广播监听
        registerBluetoothReceiver();
        //点击事件
        initEvent();
    }

    private void initEvent() {
    }

    private void initView() {
        instructions = new Instructions();
        mController = new BlueToothController();
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //初始化适配器
//        bluetoothAdp = new BluetoothAdp(R.layout.item_bluetooth_list, list);
//        recyclerView.setAdapter(bluetoothAdp);
    }


    //查找设备
    public void startBluetooth(View view) {
        UUID[] uuids = new UUID[]{
                UUID.fromString("0000FF00-0000-1000-8000-00805F9B34FB"),
                UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB"),
        };
//        mController.findUUIDDevice(uuids,mLeScanCallback);
        mController.findDevice();
    }

    //注册广播监听搜索结果
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    Log.d(TAB, "======" + device.getName());
                    if (device.getName().equalsIgnoreCase("PB1-CE1BDF")) {
                        // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                        mController.outSearchBlueTooth();
//                        device.getBondState();
                        // 状态改变的广播
                        bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
                        if (bluetoothGatt.connect()) {
                            Log.d(">>>蓝牙连接", "成功");
                            if (bluetoothGatt.discoverServices()) {
                                Log.d(">>>蓝牙连接", "==discoverServices已经开始了");
                            } else {
                                Log.d(">>>蓝牙连接", "==discoverServices没有开始");
                            }
                        } else {
                            Log.d(">>>蓝牙连接", "==失败");
                        }

                    }
                }

            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {  //此处作用待细查
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    setProgressBarIndeterminateVisibility(true);
                } else {
                    setProgressBarIndeterminateVisibility(false);
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (remoteDevice == null) {
                    showToast("无设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if (status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDevice.getName());
                }
            }
        }
    };


    //广播
    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(receiver, filter);

        //软件运行时直接申请打开蓝牙
        mController.turnOnBlueTooth(this, REQUEST_CODE);
    }

    //是否支持蓝牙
    public void isSupportBlueTooth(View view) {
        showToast(mController.isSupportBlueTooth() ? "该设备支持蓝牙" : "该设备不支持蓝牙");
    }

    //蓝牙是否打开
    public void isBlueToothEnable(View view) {
        showToast(mController.getBlueToothStatus() ? "该设备已打开蓝牙" : "该设备没有打开蓝牙");
    }

    //打开蓝牙
    public void turnOnBlueTooth(View view) {
        mController.turnOnBlueTooth(this, REQUEST_CODE);
    }

    //关闭蓝牙
    public void turnOffBlueTooth(View view) {
        mController.turnOffBlueTooth();
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showToast("打开成功");
        } else {
            showToast("打开失败");
        }
    }


    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    final String SPP_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9F";

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        //更新
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.d(TAB, "=======onPhyUpdate=========");
        }


        //读入
        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.d(TAB, "=======onPhyRead=========");

        }

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
            bluetoothGattService = gatt.getService(UUID.fromString(SPP_UUID));
            String a = "6e400002-b5a3-f393-e0a9-e50e24dcca9f";
            String b = "6e400003-b5a3-f393-e0a9-e50e24dcca9f";
            //设置写入特征UUID,原型是：BluetoothGattCharacteristic writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITEUUID));
            writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(a));
            //设置监听特征UUID,原型是：BluetoothGattCharacteristic notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFYUUID));
            notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(b));
            //开启监听
            boolean b1 = gatt.setCharacteristicNotification(notifyCharacteristic, true);
            if (b1){
                List<BluetoothGattDescriptor> descriptors = notifyCharacteristic.getDescriptors();
                if(descriptors != null && descriptors.size() > 0) {
                    for(BluetoothGattDescriptor descriptor : descriptors) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
            Log.d("uuidconnectsuccess", "uuid连接成功==b1=="+b1);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAB, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("onCharacteristicWrite", "写入成功后的回调");
            //第一步   写入成功了
            byte[] value = characteristic.getValue();
            Log.e("onCharacteristicWrite", bytesHexStrTranslate.bytesToHexFun1(value));

        }

        //接受数据回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            Log.e("ReceiveSuccess", bytesHexStrTranslate.bytesToHexFun1(value));
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAB,"========onDescriptorRead============");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAB,"========onDescriptorWrite============");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAB,"========onMtuChanged============");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAB,"========onReadRemoteRssi============");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAB,"========onReliableWriteCompleted============");
        }

    };

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    //查看连接的
    public void seeBluetooth(View view) {
        mController.getBondedDeviceList();
    }

    private BluetoothGatt bluetoothGatt;

    //第一次绑定
    public void bindingOne(View view) {
        Log.d(TAB, "====================================== bind");
        writeCharacteristic.setValue(instructions.getBindRequestOne());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);

    }
}

