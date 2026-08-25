package com.hy.greenbuilding.event;

public class DefrostChangeEvent {

    private boolean isDefrost;
    public DefrostChangeEvent(boolean isDefrost){
        this.isDefrost = isDefrost;
    }
    public boolean getDefrostStatus(){
        return isDefrost;
    }
}
