package com.hy.greenbuilding.event;

public class OTAErrorEvent {
    private int type;
    public  OTAErrorEvent(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
