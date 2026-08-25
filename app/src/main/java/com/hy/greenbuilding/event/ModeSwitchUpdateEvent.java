package com.hy.greenbuilding.event;

public class ModeSwitchUpdateEvent {
    private final boolean isMode;

    /**
     * @param isMode 开关的最新状态（true为自动，false为手动）
     */
    public ModeSwitchUpdateEvent(boolean isMode) {
        this.isMode = isMode;
    }

    public boolean isMode() {
        return isMode;
    }
}
