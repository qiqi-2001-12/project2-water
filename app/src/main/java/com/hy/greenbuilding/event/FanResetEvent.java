package com.hy.greenbuilding.event;

public class FanResetEvent {

    private int type;
    private byte[] bytes;
    public FanResetEvent(int type,byte[] bytes){
        this.type = type;
        this.bytes = bytes;
    }
    public int getType(){
        return this.type;
    }
    public byte[] getBytes(){
        return this.bytes;
    }
}
