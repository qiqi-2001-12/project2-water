package com.hy.greenbuilding.event;


public class SetStatusEvent {
    private int type;
    private boolean isSuccess;
    public SetStatusEvent(int type,boolean status){
        this.type = type;
        this.isSuccess = status;
    }
    public int getType(){
        return this.type;
    }
    public boolean getStatus(){
        return this.isSuccess;
    }
}
