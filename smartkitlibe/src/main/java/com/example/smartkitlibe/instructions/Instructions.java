package com.example.smartkitlibe.instructions;



/**
 *  十六进制
 *  0  1  2  3  4   5   6   7   8   9   A   B   C   D   E   F
 *  十进制
 *  0   1   2   3   4   5   6   7   8   9   10  11  12  14  15
 *  二进制
 *  0000    0001    0010    0011    0100
 *
 * */

public class Instructions {
    /**
     * 方向     设备号帧头   长度   指令
     * APP→MCU	0x07	  0x01	0x01
     * 指令一  绑定请求
     *   7+1+1 = 9    256 - 9 = 247
     *
     * */
    private byte[] bindRequestOne = new byte[]{0x07, 0x01, 0x01, (byte) 0xf7};
    /**
     * 方向     设备号帧头   长度   指令
     * APP→MCU	0x07	0x01	0x03
     * 指令3  绑定请求
     * 收到此指令说明药盒已经绑定成功了，绑定流程结束。收到此指令，执行指令4，（注意此处需要置位一个标记）
     * 1、设备号帧头：0x07 固定值
     * 2、长度：指令位后面的所有数据，不包含指令位
     * 3、指令：0x03 固定值
     * 4、校验位：检验位之前的所有数据之和，取反加1	7+1+3 = 11  256-11 = 245
     * */
    private byte[] bindRequestThree =  new byte[]{0x07, 0x01, 0x03, (byte) 0xf5};


    public byte[] getBindRequestOne() {
        return bindRequestOne;
    }

    public byte[] getBindRequestThree() {
        return bindRequestThree;
    }
}
