package com.hy.greenbuilding.model;

import java.util.List;

public class WeatherDataInfo1 {
    //{"data":
    // {
    // "yesterday":{"date":"15日星期一","high":"高温 24℃","fx":"东风","low":"低温 19℃","fl":"<![CDATA[2级]]>","type":"多云"},
    // "city":"深圳",
    // "forecast":[
    // {"date":"16日星期二","high":"高温 26℃","fengli":"<![CDATA[2级]]>","low":"低温 22℃","fengxiang":"东北风","type":"多云"},
    // {"date":"17日星期三","high":"高温 27℃","fengli":"<![CDATA[2级]]>","low":"低温 21℃","fengxiang":"东北风","type":"晴"},
    // {"date":"18日星期四","high":"高温 26℃","fengli":"<![CDATA[2级]]>","low":"低温 21℃","fengxiang":"东北风","type":"多云"},
    // {"date":"19日星期五","high":"高温 27℃","fengli":"<![CDATA[2级]]>","low":"低温 21℃","fengxiang":"东风","type":"阴"},
    // {"date":"20日星期六","high":"高温 26℃","fengli":"<![CDATA[3级]]>","low":"低温 20℃","fengxiang":"东风","type":"多云"}],
    // "ganmao":"感冒低发期，天气舒适，请注意多吃蔬菜水果，多喝水哦。","wendu":"23"
    // }
    // ,"status":1000,"desc":"OK"}
    private Data1 data;
    private int status;
    private String desc;

    public Data1 getData() {
        return data;
    }

    public void setData(Data1 data) {
        this.data = data;
    }

    public class Data1{
       // private String yesterday;
        private String city;
        private List<Detail> forecast;
        private String ganmao;
        private String wendu;

        public List<Detail> getForecast() {
            return forecast;
        }

        public void setForecast(List<Detail> forecast) {
            this.forecast = forecast;
        }

        public String getWendu() {
            return wendu;
        }

        public void setWendu(String wendu) {
            this.wendu = wendu;
        }

        public class Detail{
            private String date;
            private String high;
            private String fengli;
            private String low;
            private String fengxiang;
            private String type;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }

}
