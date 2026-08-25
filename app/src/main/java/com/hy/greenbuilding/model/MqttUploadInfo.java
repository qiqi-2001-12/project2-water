package com.hy.greenbuilding.model;

import java.util.List;

/**
 * 发布主题
 */
public class MqttUploadInfo {
    private String ip_addr;//设备IP地址
    private String device_addr;//设备地址
    private String app_version;//app版本号
    private String control_version;//主控机版本号
    private String client_id;//mqtt客户端ID "hy_"+device_type+"_"+ device_code
    private String vendor_id;//厂商ID
    private String serial;//序列号  // 第一段表示城市，如深圳：001; 第二段表示外机类型;第三段之后为扩展，无则不填
    private int device_type;//设备类型
    private String device_code;//设备码
    private String timestamp;//当前时间
    private int sensor_type;//传感器类型 1:485  2:zigbee
    private int outside_type;//室外机类型 1:低温增焓 2:光伏 3:除湿 4:多联机  传0则不更新该字段
   // private int fan_modify;//是否更新fan_type_obj字段；1：更新  0：不更新
    private List<FanTypeCount> fan_type_obj;


    public List<FanTypeCount> getFan_type_obj() {
        return fan_type_obj;
    }

    public void setFan_type_obj(List<FanTypeCount> fan_type_obj) {
        this.fan_type_obj = fan_type_obj;
    }

    public int getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(int sensor_type) {
        this.sensor_type = sensor_type;
    }

    public int getOutside_type() {
        return outside_type;
    }

    public void setOutside_type(int outside_type) {
        this.outside_type = outside_type;
    }

//    public int getFan_modify() {
//        return fan_modify;
//    }
//
//    public void setFan_modify(int fan_modify) {
//        this.fan_modify = fan_modify;
//    }

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public String getDevice_addr() {
        return device_addr;
    }

    public void setDevice_addr(String device_addr) {
        this.device_addr = device_addr;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version= app_version;
    }

    public String getControl_version() {
        return control_version;
    }

    public void setControl_version(String control_version) {
        this.control_version = control_version;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
