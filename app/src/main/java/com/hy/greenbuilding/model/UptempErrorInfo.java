package com.hy.greenbuilding.model;

import java.util.ArrayList;
import java.util.List;

public class UptempErrorInfo {
    private String name;
    private List<UpTempSystemStatusInfo> value = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UpTempSystemStatusInfo> getValue() {
        return value;
    }

    public void setValue(List<UpTempSystemStatusInfo> value) {
        this.value = value;
    }
}
