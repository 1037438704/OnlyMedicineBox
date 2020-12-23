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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartkitlibe.Configure;
import com.example.smartkitlibe.instructions.Instructions;
import com.example.smartkitlibe.utils.BlueToothController;
import com.example.smartkitlibe.utils.BytesHexStrTranslate;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private Toast mToast;
    private BlueToothController mController;
    private String TAB = "zdl";
    private Instructions instructions;
    private BytesHexStrTranslate bytesHexStrTranslate;
    private TextView text_one_bind, text_two_bind, text_looking_for_the_kit;
    int getSwitch = 0;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;


    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

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
            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
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
            Log.e(TAB, "=====点击的数据======" + bytesHexStrTranslate.bytesToHexFun1(value));

        }

        //接受数据回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            Log.e("ReceiveSuccess", bytesHexStrTranslate.bytesToHexFun1(value));
            Message msg = new Message();
            Bundle data = new Bundle();
            //将获取到的String装载到msg中
            data.putString("value", bytesHexStrTranslate.bytesToHexFun1(value));
            msg.setData(data);
            msg.what = 1;
            //发消息到主线程
            handler.sendMessage(msg);
        }
    };

    //转到主线程进行交互 子线程无法修改UI
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Bundle data = msg.getData();
                String val = data.getString("value");
                switch (getSwitch) {
                    case 0:
                        text_one_bind.setText("第一次返回的信息：   " + val);
                        break;
                    case 1:
                        String electricityQuantity = new BigInteger(val.substring(5, 7), 16).toString();
                        String v1 = new BigInteger(val.substring(7, 9), 16).toString();
                        String v2 = new BigInteger(val.substring(11, 13), 16).toString();
                        text_two_bind.setText("第二次返回的信息: " + "\n电量：" + electricityQuantity
                                + "\n版本号(高位):" + v1
                                + "\n版本号(低位):" + v2);
                        break;
                    case 2:
                        //寻找药盒
                        text_looking_for_the_kit.setText(val);
                        break;
                    case 3:
                        //停止提醒
                        text_looking_for_the_kit.setText(val);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "暂无此功能", Toast.LENGTH_SHORT).show();
                }
                //设置UI
            } else if (msg.what == 0) {
                Toast.makeText(getApplicationContext(), "请求资源不成功", Toast.LENGTH_LONG).show();
            }
        }
    };

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
        text_one_bind = findViewById(R.id.text_one_bind);
        text_two_bind = findViewById(R.id.text_two_bind);
        text_looking_for_the_kit = findViewById(R.id.text_looking_for_the_kit);
    }


    //查找设备
    public void startBluetooth(View view) {
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


    //查看连接的
    public void seeBluetooth(View view) {
        mController.getBondedDeviceList();
    }

    //第一次绑定
    public void bindingOne(View view) {
        if (writeCharacteristic == null) {
            return;
        }
        writeCharacteristic.setValue(instructions.getBindRequestOne());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
        getSwitch = 0;
    }

    //第二次交互
    public void bindingTwo(View view) {
        if (writeCharacteristic == null) {
            return;
        }
        writeCharacteristic.setValue(instructions.getBindRequestThree());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
        getSwitch = 1;
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

    //寻找药盒
    public void lookingForTheKit(View view) {
        if (writeCharacteristic == null) {
            return;
        }
        writeCharacteristic.setValue(instructions.getLookingForTheKit());
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
        getSwitch = 2;
    }
}

