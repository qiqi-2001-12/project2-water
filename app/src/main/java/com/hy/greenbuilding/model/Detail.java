package com.hy.greenbuilding.model;

public class Detail {
    public int id;
    public int subtype;
    public String room;
    public String name;
    public int temp;
    public int humi;
    public int illum;
    public int PM25;
    public int CO2;
    public int Airlevel;
    private int formaldehyde;
    private int tvoc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHumi() {
        return humi;
    }

    public void setHumi(int humi) {
        this.humi = humi;
    }

    public int getIllum() {
        return illum;
    }

    public void setIllum(int illum) {
        this.illum = illum;
    }

    public int getPM25() {
        return PM25;
    }

    public void setPM25(int PM25) {
        this.PM25 = PM25;
    }

    public int getCO2() {
        return CO2;
    }

    public void setCO2(int CO2) {
        this.CO2 = CO2;
    }

    public int getAirlevel() {
        return Airlevel;
    }

    public void setAirlevel(int airlevel) {
        Airlevel = airlevel;
    }

    public int getFormaldehyde() {
        return formaldehyde;
    }

    public void setFormaldehyde(int formaldehyde) {
        this.formaldehyde = formaldehyde;
    }

    public int getTvoc() {
        return tvoc;
    }

    public void setTvoc(int tvoc) {
        this.tvoc = tvoc;
    }
}
