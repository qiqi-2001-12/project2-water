package com.hy.greenbuilding.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Environment;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.City;
import com.hy.greenbuilding.model.District;
import com.hy.greenbuilding.model.Province;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {
    public static final String EMPTY = "";
    public static String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File
            .separator + "app";
    public static String WEATHER_URL = "https://www.tianqiapi.com/api/?version=v6&appid=32737929&appsecret=ZpCrpQ6e&city=";
    public static String WEATHER_URL1 = "http://wthrcdn.etouch.cn/weather_mini?city=";
    public static SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy年MM月dd");
    public static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("HH");
    public static SimpleDateFormat simpleDateFormat4 = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String INIT_PASSWORD = "admin";
    /**
     * 判断字符串为Null或者Empty
     *
     * @param str 传入的字符串
     * @return 判断结果
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String getAmountString(float amount){
       return String.format("%.2f", amount);
    }

    public static String getPriceString(float amount){
        return String.format("%.1f", amount);
    }

    /**
     * addString:将指定的基础字符串补充指定的字符，以达到指定的长度.
     *
     * @param base
     *            要补充的基础字符串
     * @param add
     *            填充的字符
     * @param len
     *            填充后的总长度,如果填充后的总长度小于基础字符串的长度则不进行处理
     * @param pos
     *            填充的位置，L向左填充 R向右填充
     * @return 填充后的字符串
     * @throws @since
     *             JDK 1.8
     * @author luoyibo
     */
    public static String addString(String base, String add, Integer len, String pos) {
        StringBuffer sBuffer = new StringBuffer();
        String reString = base;
        Integer addLen = len - base.length();
        if (addLen > 0) {
            for (int i = 0; i < addLen; i++) {
                sBuffer.append(add);
            }
            if (pos.toUpperCase().equals("L"))
                reString = sBuffer.toString() + reString;
            else
                reString = reString + sBuffer.toString();
        }
        return reString;
    }


    /**
     * 计算文件的字节数
     *
     * @param file
     * @return
     */
    public static byte[] readFile(File file) {
        if (file.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int len = 0;
                while ((len = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                return outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
        return null;
    }

    public static String convertUnicodeToCh(String str) {
        if(StringUtils.isNullOrEmpty(str)){
            return "";
        }
        try {
            Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
            Matcher matcher = pattern.matcher(str);

            // 迭代，将str中的所有unicode转换为正常字符
            while (matcher.find()) {
                String unicodeFull = matcher.group(1); // 匹配出的每个字的unicode，比如\u67e5
                String unicodeNum = matcher.group(2); // 匹配出每个字的数字，比如\u67e5，会匹配出67e5

                // 将匹配出的数字按照16进制转换为10进制，转换为char类型，就是对应的正常字符了
                char singleChar = (char) Integer.parseInt(unicodeNum, 16);

                // 替换原始字符串中的unicode码
                str = str.replace(unicodeFull, singleChar + "");
            }
            return str;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }

    public static List<Province> parser(Context mContext){
        List<Province>list =null;
        Province province = null;

        List<City>cities = null;
        City city = null;

        List<District>districts = null;
        District district = null;

        // 创建解析器，并制定解析的xml文件
        XmlResourceParser parser = mContext.getResources().getXml(R.xml.city_list);
        try{
            int type = parser.getEventType();
            while(type!=1) {
                String tag = parser.getName();//获得标签名
                switch (type) {
                    case XmlResourceParser.START_DOCUMENT:
                        list = new ArrayList<Province>();
                        break;
                    case XmlResourceParser.START_TAG:
                        if ("p".equals(tag)) {
                            province=new Province();
                            cities = new ArrayList<City>();
                            int n =parser.getAttributeCount();
                            for(int i=0 ;i<n;i++){
                                //获得属性的名和值
                                String name = parser.getAttributeName(i);
                                String value = parser.getAttributeValue(i);
                                if("p_id".equals(name)){
                                    province.setId(value);
                                }
                            }
                        }
                        if ("pn".equals(tag)){//省名字
                            province.setName(parser.nextText());
                        }
                        if ("c".equals(tag)){//城市
                            city = new City();
                            districts = new ArrayList<District>();
                            int n =parser.getAttributeCount();
                            for(int i=0 ;i<n;i++){
                                String name = parser.getAttributeName(i);
                                String value = parser.getAttributeValue(i);
                                if("c_id".equals(name)){
                                    city.setId(value);
                                }
                            }
                        }
                        if ("cn".equals(tag)){
                            city.setName(parser.nextText());
                        }
                        if ("d".equals(tag)){
                            district = new District();
                            int n =parser.getAttributeCount();
                            for(int i=0 ;i<n;i++){
                                String name = parser.getAttributeName(i);
                                String value = parser.getAttributeValue(i);
                                if("d_id".equals(name)){
                                    district.setId(value);
                                }
                            }
                            district.setName(parser.nextText());
                            districts.add(district);
                        }
                        break;
                    case XmlResourceParser.END_TAG:
                        if ("c".equals(tag)){
                            city.setDistricts(districts);
                            cities.add(city);
                        }
                        if("p".equals(tag)){
                            province.setCitys(cities);
                            list.add(province);
                        }
                        break;
                    default:
                        break;
                }
                type = parser.next();
            }
        }catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } */
        catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
    public long dateDiff(String startTime, String endTime) {
        long nd = 1000 * 24 * 60 * 60;
        long diff;
        long day = 0;
        try {
            diff = StringUtils.sd.parse(endTime).getTime()
                    - StringUtils.sd.parse(startTime).getTime();
            day = diff / nd;
            if (day >= 1) {
                return day;
            } else {
                if (day == 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }
}
