package com.hy.greenbuilding.event;

public class FunctionTestEvent {
    private boolean isSuccess;
    private String status;
    private int type;
    public FunctionTestEvent(int type,boolean isSuccess,String status){
        this.isSuccess = isSuccess;
        this.type = type;
        this.status = status;
    }
    public boolean isSuccess(){
        return this.isSuccess;
    }
    public int getType(){
        return this.type;
    }
    public String getStatus(){
        return this.status;
    }
}
