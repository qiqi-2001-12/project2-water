package com.hy.greenbuilding.mqtt;

import android.util.Log;

import androidx.annotation.Keep;

import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.PackageUtil;

/**
 * 云端数据上报
 */
public class MqttUploadManager {
    private static volatile MqttUploadManager mqttUploadManager;

    private HDTopic mHDTopic;
    private HETopic mHETopic;
    private HFTopic mHFTopic;
    private HXTopic mHxTopic;
    private HSTopic mHsTopic;
    private int currentType = 0;

    private byte[] hdData;
    private byte[] heData;
    private byte[] hfData;
    private byte[] hxData;
    private byte[] hsData;

    public MqttUploadManager() {
        mHDTopic = new HDTopic();
        mHETopic = new HETopic();
        mHFTopic = new HFTopic();
        mHxTopic = new HXTopic();
        mHsTopic = new HSTopic();
    }

    @Keep
    public static MqttUploadManager getInstance() {
        if (mqttUploadManager == null) {
            synchronized (MqttUploadManager.class) {
                if (mqttUploadManager == null) {
                    mqttUploadManager = new MqttUploadManager();
                }
            }
        }
        return mqttUploadManager;
    }

    public HDTopic getmHDTopic() {
        return mHDTopic;
    }

    public void setmHDTopic(HDTopic mHDTopic) {
        this.mHDTopic = mHDTopic;
    }

    public HETopic getmHETopic() {
        return mHETopic;
    }

    public void setmHETopic(HETopic mHETopic) {
        this.mHETopic = mHETopic;
    }

    public HFTopic getmHFTopic() {
        return mHFTopic;
    }

    public void setmHFTopic(HFTopic mHFTopic) {
        this.mHFTopic = mHFTopic;
    }

    public HXTopic getmHxTopic() {
        return mHxTopic;
    }

    public void setmHxTopic(HXTopic mHxTopic) {
        this.mHxTopic = mHxTopic;
    }

    public HSTopic getmHsTopic() {
        return mHsTopic;
    }

    public void setmHsTopic(HSTopic mHsTopic) {
        this.mHsTopic = mHsTopic;
    }

    public void uploadData() {
        if (currentType >= 5) {
            currentType = 0;
        }
        currentType++;
        if (currentType == 1) {
            hdData = mHDTopic.getBytes();
            Log.i("info", "send length -HD--" + hdData.length);
            try {
                MyMqttService.publishGreen(hdData, "hy_1_" + PackageUtil.getSerialNumber(), "HD");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (currentType == 2) {
            heData = mHETopic.getBytes();
            Log.i("info", Hex.bytesToHexString(heData) + "----send length -HE--" + heData.length);
            try {
                MyMqttService.publishGreen(heData, "hy_1_" + PackageUtil.getSerialNumber(), "HE");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (currentType == 3) {
            hfData = mHFTopic.getBytes();
            Log.i("info", "send length -HF--" + hfData.length);
            try {
                MyMqttService.publishGreen(hfData, "hy_1_" + PackageUtil.getSerialNumber(), "HF");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (currentType == 4) {
            MyMqttService.sendDataToServer();
            hxData = mHxTopic.getBytes();
            try {
                MyMqttService.publishGreen(hxData, "hy_1_" + PackageUtil.getSerialNumber(), "HX");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (currentType == 5) {
            hsData = mHsTopic.getHsData();
            Log.i("info", "send length -HS--" + hsData.length);
            try {
                MyMqttService.publishGreen(hsData, "hy_1_" + PackageUtil.getSerialNumber(), "HS");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
