package com.example.onlymedicinebox.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.onlymedicinebox.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BluetoothAdp extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {

    public BluetoothAdp(int layoutResId, @Nullable List<BluetoothDevice> data) {
        super(layoutResId, data);
    }

    public BluetoothAdp(int item_bluetooth_list) {
        super(item_bluetooth_list);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, BluetoothDevice bluetoothDevice) {
        String uuid = "没有UUID";
        if (bluetoothDevice.getUuids() != null) {
            if (bluetoothDevice.getUuids()[0].toString() != null) {
                uuid = bluetoothDevice.getUuids()[0].toString();
            }
        }

//        PB1-CE1BDF

        baseViewHolder.setText(R.id.item_text_bluetooth,
                "名字：== " + bluetoothDevice.getName()
                        + "\n" + "getAddress：== " + bluetoothDevice.getAddress()
                        + "\n" + "getBondState：== " + bluetoothDevice.getBondState()
                        + "\n" + "getAlias：== " + bluetoothDevice.getAlias()
                        + "\n" + "getType：== " + bluetoothDevice.getType()
                        + "\n" + "getBluetoothClass：== " + bluetoothDevice.getBluetoothClass().toString()
                        + "\n" + "uuid：== " + uuid

        );
    }
}

