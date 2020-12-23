package com.example.onlymedicinebox.aty;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlymedicinebox.R;
import com.example.onlymedicinebox.adapter.BluetoothAdp;
import com.example.smartkitlibe.utils.BlueToothController;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueToothActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private Toast mToast;
    private BlueToothController mController;
    private List<BluetoothDevice> list = new ArrayList<>();
    //列表适配器
    BluetoothAdp bluetoothAdp;
    RecyclerView recyclerView;
    private TextView text_one_bind;
    int getSwitch =0;

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

    public boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private void initEvent() {
        bluetoothAdp.setOnItemClickListener(
                (adapter, view, position) -> {
                    List<BluetoothDevice> data = (List<BluetoothDevice>) adapter.getData();
                    if (data != null) {
                        BluetoothDevice bluetoothDevice = data.get(position);
                        if (bluetoothDevice != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                Log.d("zdl", "=====createBond======" + bluetoothDevice.createBond());
//                                bluetoothDevice.createBond();
                                Method method = null;
                                try {
                                    method = BluetoothDevice.class.getMethod("createBond");
                                    Log.e("zld", "开始配对");
                                    method.invoke(bluetoothDevice);
                                } catch (Exception e) {
                                    Log.e("zld", "=======" + e.toString());
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        );
    }

    private void initView() {
        mController = new BlueToothController();
        text_one_bind = findViewById(R.id.text_one_bind);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //初始化适配器
        bluetoothAdp = new BluetoothAdp(R.layout.item_bluetooth_list, list);
        recyclerView.setAdapter(bluetoothAdp);

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
//                list.clear();
//                bluetoothAdp.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    Log.d("zdl", "======" + device.getName());
                    if (device.getName().equalsIgnoreCase("PB1-CE1BDF")) {
                        // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                        mController.outSearchBlueTooth();
                        // 获取蓝牙设备的连接状态
                        int connectState = device.getBondState();
                        switch (connectState) {
                            // 未配对
                            case BluetoothDevice.BOND_NONE:
                                Log.d("zdl", "======正在配对=======");
                                // 配对
                                try {
                                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                    createBondMethod.invoke(device);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            // 已配对
                            case BluetoothDevice.BOND_BONDED:
                                Log.d("zdl", "======已配对=======");
                                try {
                                    // 连接
                                    connect(device);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                Log.d("zdl", "======未配对=======");
                                break;
                        }
                    }
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 状态改变的广播
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    Log.d("zdl", "======" + device.getName());
                    if (device.getName().equalsIgnoreCase("PB1-CE1BDF")) {
                        int connectState = device.getBondState();
                        switch (connectState) {
                            case BluetoothDevice.BOND_NONE:
                                Log.d("zdl", "===BOND_NONE===" + device.getName());
                                showToast("未绑定" + device.getName());
                                break;
                            case BluetoothDevice.BOND_BONDING:
                                showToast("正在绑定" + device.getName());
                                try {
                                    // 连接
                                    connect(device);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case BluetoothDevice.BOND_BONDED:
                                try {
                                    // 连接
                                    connect(device);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }


//                if (device.getName() != null) {
//                    if (device.getName().length() >= 3) {
//                        if ("PB1".equals(device.getName().substring(0, 3))) {
//                            list.add(device);
//                            bluetoothAdp.notifyDataSetChanged();
//                        }
//                    }
//                }
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
//        mController.turnOffBlueTooth();
//        Nordic-----主服务ID：0000180A-0000-1000-8000-00805F9B34FB 和服务UUID ：6E400001-B5A3-F393-E0A9-E50E24DCCA9F
        UUID[] uuids = new UUID[]{
                UUID.fromString("0000FF00-0000-1000-8000-00805F9B34FB"),
                UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB"),
        };
        mController.findUUIDDevice(uuids, mLeScanCallback);
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

    private void connect(BluetoothDevice device) throws IOException {
//        Nordic-----主服务ID：0000180A-0000-1000-8000-00805F9B34FB 和服务UUID ：6E400001-B5A3-F393-E0A9-E50E24DCCA9F
        // 固定的UUID
        Log.d("zdl","=====进来了======");
        final String SPP_UUID = "0000180A-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
    }


    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("zdl", "====LeScanCallback======" + device.getName());
        }
    };
    //查看连接的
    public void seeBluetooth(View view) {

    }

    private BluetoothGatt bluetoothGatt;
    //第一次绑定
    public void bindingOne(View view) {

    }
}