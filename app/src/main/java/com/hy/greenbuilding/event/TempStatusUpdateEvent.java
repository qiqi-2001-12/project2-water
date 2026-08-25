package com.hy.greenbuilding.event;

public class TempStatusUpdateEvent {
    public boolean isOpen;
    public int tempMode;

    public TempStatusUpdateEvent (boolean isOpen,int tempMode){
        this.isOpen = isOpen;
        this.tempMode = tempMode;
    }

    public int getTempMode() {
        return tempMode;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
