package com.hy.greenbuilding.event;

public class ResetSystemEvent {
    private boolean success;

    public ResetSystemEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
