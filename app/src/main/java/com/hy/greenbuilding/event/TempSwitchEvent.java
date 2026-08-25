package com.hy.greenbuilding.event;

// TempSwitchEvent.java (在新文件中创建)
public class TempSwitchEvent {
    private final boolean isChecked; // 开关的最新状态

    public TempSwitchEvent(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
