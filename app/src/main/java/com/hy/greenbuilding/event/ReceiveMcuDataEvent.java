package com.hy.greenbuilding.event;

public class ReceiveMcuDataEvent {
    public String hexString;
    public ReceiveMcuDataEvent(String hexString){
        this.hexString = hexString;
    }

    public String getHexString() {
        return hexString;
    }
}
