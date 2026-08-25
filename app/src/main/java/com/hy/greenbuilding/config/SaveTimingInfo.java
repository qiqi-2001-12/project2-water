package com.hy.greenbuilding.config;

import android.util.Log;

/**
 * 保存定时数据
 */
public class SaveTimingInfo {
    private String currentTime;
    private int openDay;//开启时长
    private String beforeTime1;
    private String afterTime1;
    private String beforeTime2;
    private String afterTime2;
    private String beforeTime3;
    private String afterTime3;
    private long startTimeStamp; // 记录到期的绝对时间戳（毫秒）
    private long endTimeStamp; // 最后一次更新的时间

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public int getOpenDay() {
        return openDay;
    }

    public void setOpenDay(int openDay) {
        Log.e("TAG", "setOpenDay: "+openDay);
        this.openDay = openDay;
    }

    public String getBeforeTime1() {
        return beforeTime1;
    }

    public void setBeforeTime1(String beforeTime1) {
        this.beforeTime1 = beforeTime1;
    }

    public String getAfterTime1() {
        return afterTime1;
    }

    public void setAfterTime1(String afterTime1) {
        this.afterTime1 = afterTime1;
    }

    public String getBeforeTime2() {
        return beforeTime2;
    }

    public void setBeforeTime2(String beforeTime2) {
        this.beforeTime2 = beforeTime2;
    }

    public String getAfterTime2() {
        return afterTime2;
    }

    public void setAfterTime2(String afterTime2) {
        this.afterTime2 = afterTime2;
    }

    public String getBeforeTime3() {
        return beforeTime3;
    }

    public void setBeforeTime3(String beforeTime3) {
        this.beforeTime3 = beforeTime3;
    }

    public String getAfterTime3() {
        return afterTime3;
    }

    public void setAfterTime3(String afterTime3) {
        this.afterTime3 = afterTime3;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }
}
