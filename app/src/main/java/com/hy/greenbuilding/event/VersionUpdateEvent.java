package com.hy.greenbuilding.event;

public class VersionUpdateEvent {
    private int type;
    private String message;
    public VersionUpdateEvent(int type,String message){
        this.message = message;
        this.type = type;
    }
    public String getMessage(){
        return this.message;
    }
    public int getType(){
        return this.type;
    }
}
