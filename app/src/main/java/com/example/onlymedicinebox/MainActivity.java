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
import com.example.smartkitlibe.utils.BluetoothGattController;
import com.example.smartkitlibe.utils.BytesHexStrTranslate;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private static final String ACTION_RECEIVE_MESSAGE = "laya";
    private Toast mToast;
    private BlueToothController mController;
    private String TAB = "zdl";
    private BluetoothGattController bluetoothGattController;
    private TextView text_one_bind, text_two_bind, text_looking_for_the_kit;
    int getSwitch = 0;

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
    }

    private void initView() {
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
            if (BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    Log.d(TAB, "======" + device.getName());
                    if (device.getName().equalsIgnoreCase("PB1-CE1BDF")) {
                        // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                        mController.outSearchBlueTooth();
                        // 状态改变的广播
                        bluetoothGattController = new BluetoothGattController(device, context);
                    }
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
        if (bluetoothGattController == null){

            return;
        }
        bluetoothGattController.getBindRequest();
        getSwitch = 0;
    }

    //第二次交互
    public void bindingTwo(View view) {
        bluetoothGattController.getBindingSuccessFeedback();
        getSwitch = 1;
    }

    //寻找药盒
    public void lookingForTheKit(View view) {
        bluetoothGattController.getLookingForTheKit();
        getSwitch = 2;
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


}

