package com.hy.greenbuilding.model;

/**
 * 后台下发版本信息
 */
public class MqttResponseInfo {
    private String app_version;//版本号
    private String app_url;//apk升级url
    private String control_version;//主控机版本号
    private String vendor_id;
    private String control_url;
    private String serial;//序列号
    private int device_type;
    private String device_code;
    private String sign;//MD5
    private String timestamp;

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_url() {
        return app_url;
    }

    public void setApp_url(String app_url) {
        this.app_url = app_url;
    }

    public String getControl_version() {
        return control_version;
    }

    public void setControl_version(String control_version) {
        this.control_version = control_version;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getControl_url() {
        return control_url;
    }

    public void setControl_url(String control_url) {
        this.control_url = control_url;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getDevice_type() {
        return device_type;
    }

    public void setDevice_type(int device_type) {
        this.device_type = device_type;
    }

    public String getDevice_code() {
        return device_code;
    }

    public void setDevice_code(String device_code) {
        this.device_code = device_code;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
