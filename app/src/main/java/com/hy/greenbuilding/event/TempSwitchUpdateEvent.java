package com.hy.greenbuilding.event;

public class TempSwitchUpdateEvent {
    private final boolean newSwitchState;

    /**
     * @param newSwitchState 开关的最新状态（true为打开，false为关闭）
     */
    public TempSwitchUpdateEvent(boolean newSwitchState) {
        this.newSwitchState = newSwitchState;
    }

    public boolean getNewSwitchState() {
        return newSwitchState;
    }
}
