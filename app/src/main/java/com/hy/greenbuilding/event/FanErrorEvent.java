package com.hy.greenbuilding.event;

public class FanErrorEvent {
    private int errorType;
    private boolean isSuccess;
    public FanErrorEvent(int type,boolean status){
        this.errorType = type;
        this.isSuccess = status;
    }
    public int getType(){
        return this.errorType;
    }
    public boolean getStatus(){
        return this.isSuccess;
    }
}
