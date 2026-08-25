package com.hy.greenbuilding.utils;

import java.util.Locale;


public class DigitalUtil {

    /**
     * 将16位的short转换成byte数组
     *
     * @param s
     *            short
     * @return byte[] 长度为2
     * */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * long类型转换为byte[]
     *
     * @param value 需要转换的数据
     * @return ByteArray
     */
    public static byte[] longToByteArray(long value) {
        long temp = value;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }
    // 整数到字节数组转换
    public static byte[] int2bytesBy32(int n) {
        byte[] ab = new byte[4];
        ab[0] = (byte) ((0xff000000 & n) >> 24);
        ab[1] = (byte) ((0xff0000 & n) >> 16);
        ab[2] = (byte) ((0xff00 & n) >> 8);
        ab[3] = (byte) (0xff & n);
        return ab;
    }

    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    // 从字节数组到十六进制字符串转换
    public static String Bytes2HexString(byte[] b) {
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[i] & 0x0f];
        }
        return new String(buff);
    }

    // 从字节数组到十六进制字符串转换
    public static String MyBytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase(Locale.getDefault());
            // ret += hex;
        }
        return ret;
    }
    // 从十进制字符串到字节数组转换
    public static byte[] DexString2Bytes(String decstr) {
        String stTemp;
        byte[] b = new byte[decstr.length() / 2];
        int j = 0;

        for (int i = 0; i < decstr.length(); i += 2) {
            stTemp = decstr.substring(i, i + 2);
            b[j++] = (byte) Integer.parseInt(stTemp);
        }
        return b;
    }

    /**
     * 字符串反转,前提每两个字符转如:2669CCDD==>DDCC6926,1234-->3412
     *
     * @param orig
     * @return
     */
    public static String StrReverseByte(String orig) {
        String strr = "";
        int iLen = orig.length();
        for (int i = 0; i < iLen; i += 2) {
            strr = orig.substring(i, i + 2) + strr;
        }
        return strr;
    }
    // 从十六进制字符串到字节数组转换
    public static byte[] HexString2Bytes(String hexstr, int len) {
        if(hexstr.length()==0)
            return new byte[1];

        byte[] b = new byte[len];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            if (j < hexstr.length()) {
                char c0 = hexstr.charAt(j++);
                char c1 = hexstr.charAt(j++);
                b[i] = (byte) ((parse(c0) << 4) | parse(c1));
            }
        }

        return b;
    }
    // 从十六进制字符串到字节数组转换
    public static byte[] HexString2Bytes(String hexstr) {
        if(hexstr.length()==0)
            return new byte[1];
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    public static int getUnsignedByte(byte data) { // 将data字节型数据转换为0~255 (0xFF
        // 即BYTE)。
        return data & 0x0FF;
    }

    /**
     * 十六进制字符串转二进制字符串
     * @param hexString
     * @return
     */
    public static String hexStrToBinaryStr(String hexString) {

        if (hexString == null || hexString.equals("")) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        // 将每一个十六进制字符分别转换成一个四位的二进制字符
        for (int i = 0; i < hexString.length(); i++) {
            String indexStr = hexString.substring(i, i + 1);
            String binaryStr = Integer.toBinaryString(Integer.parseInt(indexStr, 16));
            while (binaryStr.length() < 4) {
                binaryStr = "0" + binaryStr;
            }
            sb.append(binaryStr);
        }
        return sb.toString();
    }
    public static int GetSocketCrc(byte[] data, int length) {
        int crc = 0;
        int i, j, m;

        for (j = 0; j < length; j++) {
            m = getUnsignedByte(data[j]);
            crc ^= m;
            for (i = 0; i < 8; i++) {
                if ((crc & 0x01) != 0) {
                    crc >>= 1;
                    crc ^= 0x8c;
                } else {
                    crc >>= 1;
                }
            }
        }

        return crc;
    }

    public static String Get16Len(int len)
    {
        return Integer.toHexString(len).toLowerCase();
    }

    public static Long Get10Value(String value)
    {
        return Long.parseLong(value, 16);
    }

    public static Integer hexString2DecInt(String value)
    {
        return Integer.parseInt(value, 16);
    }

    public static String GetTempValue(String value, int length)
    {
        int len=value.length();
        String strFlag="";
        if(len<length)
        {
            for(int i=0;i<length-len;i++)
            {
                strFlag=strFlag+"0";
            }
        }
        return strFlag+value;
    }

    public static String decInt2HexString(int value){
        return GetTempValue(Get16Len(value),4);
    }

    /**
     * 在str1中从start位置开始查找str2到end位置结束, 返回str2在str1的起始位置, -1表示查找失败
     */
    public static int strstr(byte[] str1, byte[] str2, int start, int end)
    {
        int index1 = start;
        int index2 = 0;
        if(str2!=null)
        {
            while(index1<str1.length && index1<end)
            {
                int dsite = 0;
                while(str1[index1+dsite]==str2[index2+dsite]) {
                    if(index2+dsite+1>=str2.length)
                        return index1;
                    dsite++;
                    if(index1+dsite>=str1.length || index2+dsite>=str2.length)
                        break;
                }
                index1++;
            }
            return -1;
        }
        else
            return index1;
    }

}
