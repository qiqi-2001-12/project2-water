package com.hy.greenbuilding.event;

public class OTAStatusEvent {
    private byte[] otaData;
    public OTAStatusEvent(byte[] otaData){
        this.otaData = otaData;
    }
    public byte[] getOtaData(){
        return this.otaData;
    }
}
