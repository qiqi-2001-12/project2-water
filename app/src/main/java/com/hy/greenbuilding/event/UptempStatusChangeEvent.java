package com.hy.greenbuilding.event;

public class UptempStatusChangeEvent {
    private String data;
    private boolean  isSuccess;
    public UptempStatusChangeEvent(String data,boolean  isSuccess){
        this.data = data;
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess(){
        return this.isSuccess;
    }
}
