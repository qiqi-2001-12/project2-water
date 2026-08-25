package com.hy.greenbuilding.event;

public class TempControlEvent {
    private int type ;
    public TempControlEvent(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
