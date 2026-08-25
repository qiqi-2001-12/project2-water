package com.hy.greenbuilding.model;

import java.util.List;

/**
 * 空气质量数据
 */
public class AirQualityInfo {
    public List<Detail> devlist;

    public List<Detail> getDevlist() {
        return devlist;
    }

    public void setDevlist(List<Detail> devlist) {
        this.devlist = devlist;
    }

}
