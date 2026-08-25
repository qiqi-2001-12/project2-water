package com.hy.greenbuilding.event;

public class RunModeEvent {

    private boolean isSuccess;
    private int mode;
    private int manualMode;

    public RunModeEvent(boolean success, int mode, int manualMode){
        this.isSuccess = success;
        this.mode = mode;
        this.manualMode = manualMode;
    }

    public boolean isModeEvent(){
        return this.isSuccess;
    }
    public int getMode(){
        return this.mode;
    }

    public int getManualMode() {
        return this.manualMode;
    }
}
