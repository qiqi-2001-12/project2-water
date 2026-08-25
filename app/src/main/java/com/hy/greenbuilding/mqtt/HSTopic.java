package com.hy.greenbuilding.mqtt;

public class HSTopic {
    private byte[] hsData = new byte[(byte)0x0072];

    public byte[] getHsData() {
        return hsData;
    }

    public void setHsData(byte[] hsData) {
        this.hsData = hsData;
    }
}
