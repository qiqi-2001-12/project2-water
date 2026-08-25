package com.hy.greenbuilding.utils;

import android.util.Log;

import java.nio.ByteBuffer;


public class ByteUtils {

    public enum Endian {
        Big,
        Little
    }

    /**
     * 将字节数组转换为HEX字符串
     *
     * @param inArray 需要转换的字节数组
     * @return HEX字符串
     */
    public static String byteArrayToHexString(byte[] inArray) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");

        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inArray.length; ++j) {
            in = inArray[j] & 0xFF;
            i = (in >> 4) & 0x0F;
            out += hex[i];
            i = in & 0x0F;
            out += hex[i];
        }
        return out;
    }

    /**
     * 将字节数组转换为HEX字符串
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数
     * @return HEX字符串
     */
    public static String byteArrayToHexString(byte[] inArray, int offset, int length) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (inArray.length < offset + length)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于转换个数。");

        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        for (j = offset; j < offset + length; ++j) {
            in = inArray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * 将字节数组转换为BCD字符串
     *
     * @param inArray 需要转换的字节数组
     * @return BCD字符串
     */
    public static String byteArrayToBcdString(byte[] inArray) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");

        StringBuilder sb = new StringBuilder();
        for (byte item : inArray
        ) {
            int h = ((item & 0xff) >> 4) + 48;
            sb.append((char) h);
            int l = (item & 0x0f) + 48;
            sb.append((char) l);
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换为BCD字符串
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数
     * @return HEX字符串
     */
    public static String byteArrayToBcdString(byte[] inArray, int offset, int length) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (inArray.length < offset + length)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于转换个数。");

        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            int h = ((inArray[i] & 0xff) >> 4) + 48;
            sb.append((char) h);
            int l = (inArray[i] & 0x0f) + 48;
            sb.append((char) l);
        }
        return sb.toString();
    }


    /**
     * 从指定位置将指定个数的byte转换为int类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数（不能大于4）
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static int byteArrayToInt(byte[] inArray, int offset, int length) {
        //默认大端模式
        return byteArrayToInt(inArray, offset, length, Endian.Big);
    }


    /**
     * 从指定位置将指定个数的byte转换为int类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数（不能大于4）
     * @param mode    大小端模式
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static int byteArrayToInt(byte[] inArray, int offset, int length, Endian mode) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (length <= 0)
            throw new IllegalArgumentException("转换的byte个数不能小于1个。");
        if (length > 4)
            throw new IllegalArgumentException("转换的byte个数不能大于4个字节。");
        if (inArray.length < offset + length)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于转换个数。");

        int value = 0;
        int oft = 0;
        if (mode == Endian.Little) {
            for (int i = offset; i < offset + length; i++) {
                value = value | (inArray[i] & 0xFF) << oft;
                oft = oft + 8;
            }
        } else {
            //数组下标从0开始，所以偏移位加数据长度必须减1开始往前循环
            for (int i = offset + length - 1; i >= offset; i--) {
                value = value | (inArray[i] & 0xFF) << oft;
                oft = oft + 8;
            }
        }
        return value;
    }
    /**
     * byte数组到int的转换(小端)
     * @param bytes
     * @return
     */
    public static int bytes2IntLittle(byte[] bytes )
    {
        int int1=bytes[0]&0xff;
        int int2=(bytes[1]&0xff)<<8;
        int int3=(bytes[2]&0xff)<<16;
        int int4=(bytes[3]&0xff)<<24;

        return int1|int2|int3|int4;
    }
    /**
     * byte数组到int的转换(大端)
     * @param bytes
     * @return
     */
    public static int bytes2IntBig(byte[] bytes )
    {
        int int1=bytes[3]&0xff;
        int int2=(bytes[2]&0xff)<<8;
        int int3=(bytes[1]&0xff)<<16;
        int int4=(bytes[0]&0xff)<<24;

        return int1|int2|int3|int4;
    }

    /**
     * 将8个byte转换为long类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @return 长整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray) {
        //默认小端模式
        return byteArrayToLong(inArray, Endian.Little);
    }

    /**
     * 从指定位置将8个byte转换为long类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray, int offset) {
        //默认小端模式
        return byteArrayToLong(inArray, offset, Endian.Little);
    }

    /**
     * 从指定位置将指定个数的byte转换为long类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数（不能大于4）
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray, int offset, int length) {
        //默认小端模式
        return byteArrayToLong(inArray, offset, length, Endian.Little);
    }

    /**
     * 将8个byte转换为long类型的数据
     *
     * @param inArray 需要转换的字节数组（必须为8个字节）
     * @param mode    大小端模式
     * @return 长整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray, Endian mode) {
        if (inArray.length != 8)
            throw new IllegalArgumentException("传入数据的长度不等于8个字节。");

        ByteBuffer buffer = ByteBuffer.allocate(8);

        if (mode == Endian.Big) {
            buffer.put(inArray, 0, inArray.length);
            buffer.flip();
        } else {
            for (int i = inArray.length - 1; i > 0; i--)
                buffer.put(inArray[i]);
            buffer.flip();
        }

        return buffer.getLong();
    }

    /**
     * byte[] 转short
     *
     * @param inArray
     * @return
     */
    public static short byteArrayToShort(byte[] inArray) {
        int ch1 = inArray[0] & 0xff;
        int ch2 = inArray[1] & 0xff;
        short result = (short) ((ch1 << 8) | (ch2 << 0));
        return result;
    }
    public static int byte2UnsignedShort(byte[] inArray) {
//        byte[] a = new byte[](byte)0x15, (byte)0xb3};
//        int port = a[0]&0xff;
//        port = port << 8;
//        port += a[1]&0xff;
        int port = (byte)inArray[0]&0xff;
        port = port << 8;
        port += (byte)inArray[1]&0xff;
        return (short)port;
    }
    public static short byte2Short(byte[] inArray) {
        int low = inArray[1] ;
        int high = inArray[0] ;
        return (short) ((low & 0xFF) | (high << 8));
    }
    /**
     * 将16位的short转换成byte数组
     *
     * @param s short
     * @return byte[] 长度为2
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * int转两个字节数组
     *
     * @param value
     * @return
     */
    public static byte[] int16ToByteArray(int value) {
        byte[] valueBytes = new byte[2];
        for (int i = 0; i < 2; i++) {
            valueBytes[i] = (byte) (value >>> 8 * (2 - i - 1));
        }
        return valueBytes;
    }

    /**
     * byte[]大小端模式转换
     * @param a
     * @return
     */
    public static byte[] changeBytes(byte[] a) {
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[b.length - i - 1];
        }
        return b;
    }

    /**
     * string转两个字节数组
     *
     * @param value
     * @return
     */
    public static byte[] stringToByteArray(String value) {
        int intValue;
        byte[] valueBytes = new byte[2];
        if (StringUtils.isNullOrEmpty(value)) {
            intValue = 0;
        } else {
            intValue = Integer.parseInt(value);
        }
        try {
            for (int i = 0; i < 2; i++) {
                valueBytes[i] = (byte)(intValue >>> 8 * (2 - i - 1));
            }
        }catch (Exception e){

        }
        return valueBytes;
    }

    public static byte int16ToByte(int value) {

        return Byte.parseByte(Integer.toString(value));
    }

    /**
     * 长度为2的byte[] 转int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt16(byte[] bytes) {
        int result = 0;
        int len = bytes.length;
        if (len == 2) {
            int ch1 = bytes[0] & 0xff;
            int ch2 = bytes[1] & 0xff;
            result = (short) ((ch1 << 8) | (ch2 << 0));
        } else if (len == 1) {
            int ch1 = bytes[0] & 0xff;
            result = (short) (ch1);
        }
        return result;
    }

//    public static int byteArrayToInt16(byte[] bytes) {
//        int result = 0;
//        if (bytes.length == 2) {
//            // 去掉 (short) 强转，直接用 int 拼接
//            // 0xff 确保了每个 byte 被当作无符号处理
//            int ch1 = bytes[0] & 0xff;
//            int ch2 = bytes[1] & 0xff;
//            result = ((ch1 << 8) | (ch2 << 0));
//        } else if (bytes.length == 1) {
//            result = bytes[0] & 0xff;
//        }
//        return result;
//    }

    /**
     * 合并两个byte[]
     *
     * @param bytes1
     * @param bytes2
     * @return
     */
    public static byte[] splicingBytes(byte[] bytes1, byte[] bytes2) {
        byte[] byte3 = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, byte3, 0, bytes1.length);
        System.arraycopy(bytes2, 0, byte3, bytes1.length, bytes2.length);
        return byte3;
    }

    /**
     * 长度为2的byte转bit数组
     *
     * @param
     * @return
     */
    public static byte[] getBitArray(byte[] bytes) {
        if(bytes == null){
            bytes = new byte[2];
        }
        byte[] array = new byte[16];
        for (int i = 15; i >= 0; i--) {
            if (i > 7) {
                array[i] = (byte) (bytes[1] & 1);
                bytes[1] = (byte) (bytes[1] >> 1);
            } else {
                array[i] = (byte) (bytes[0] & 1);
                bytes[0] = (byte) (bytes[0] >> 1);
            }
        }
        return array;
    }

    public static byte[] getBitArray1(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            bytes = new byte[2]; // 默認為0
        }
        byte[] array = new byte[16];
        for (int i = 0; i < 8; i++) {
            array[i] = (byte) ((bytes[1] >> (7 - i)) & 1);
        }
        for (int i = 8; i < 16; i++) {
            array[i] = (byte) ((bytes[0] >> (15 - i)) & 1);
        }
        return array;
    }

    /**
     * byte转bit数组
     *
     * @param
     * @return
     */
    public static byte[] getBitArray(byte byte1) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (byte1 & 1);
            byte1 = (byte) (byte1 >> 1);
        }
        return array;
    }

    /**
     * int转byte[]，高位在前低位在后
     *
     * @param value
     * @return
     */
    public static byte[] varIntToByteArray(long value) {
        Long l = new Long(value);
        byte[] valueBytes = null;
        if (l == l.byteValue()) {
            valueBytes = toBytes(value, 1);
        } else if (l == l.shortValue()) {
            valueBytes = toBytes(value, 2);
        } else if (l == l.intValue()) {
            valueBytes = toBytes(value, 4);
        } else if (l == l.longValue()) {
            valueBytes = toBytes(value, 8);
        }
        return valueBytes;
    }

    private static byte[] toBytes(long value, int len) {
        byte[] valueBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            valueBytes[i] = (byte) (value >>> 8 * (len - i - 1));
        }
        return valueBytes;
    }

    public static long byteArrayToLong1(byte[] bytes) {
        long result = 0;
        int len = bytes.length;
        if (len == 1) {
            byte ch = (byte) (bytes[0] & 0xff);
            result = ch;
        } else if (len == 2) {
            int ch1 = bytes[0] & 0xff;
            int ch2 = bytes[1] & 0xff;
            result = (short) ((ch1 << 8) | (ch2 << 0));
        } else if (len == 4) {
            int ch1 = bytes[0] & 0xff;
            int ch2 = bytes[1] & 0xff;
            int ch3 = bytes[2] & 0xff;
            int ch4 = bytes[3] & 0xff;
            result = (int) ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
        } else if (len == 8) {
            long ch1 = bytes[0] & 0xff;
            long ch2 = bytes[1] & 0xff;
            long ch3 = bytes[2] & 0xff;
            long ch4 = bytes[3] & 0xff;
            long ch5 = bytes[4] & 0xff;
            long ch6 = bytes[5] & 0xff;
            long ch7 = bytes[6] & 0xff;
            long ch8 = bytes[7] & 0xff;
            result = (ch1 << 56) | (ch2 << 48) | (ch3 << 40) | (ch4 << 32) | (ch5 << 24) | (ch6 << 16) | (ch7 << 8) | (ch8 << 0);
        }
        return result;
    }

    /**
     * 从指定位置将8个byte转换为long类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param mode    大小端模式
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray, int offset, Endian mode) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (inArray.length < offset + 8)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于8个字节。");

        ByteBuffer buffer = ByteBuffer.allocate(8);

        if (mode == Endian.Big) {
            buffer.put(inArray, 0, 8);
            buffer.flip();
        } else {
            //数组下标从0开始，所以第8个字节下标是7，从7开始往前循环
            for (int i = offset + 7; i >= offset; i--)
                buffer.put(inArray[i]);
            buffer.flip();
        }

        return buffer.getLong();
    }

    /**
     * 从指定位置将指定个数的byte转换为int类型的数据
     *
     * @param inArray 需要转换的字节数组
     * @param offset  偏移位置
     * @param length  转换个数（不能大于4）
     * @param mode    大小端模式
     * @return 整型数据
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static long byteArrayToLong(byte[] inArray, int offset, int length, Endian mode) {
        if (inArray == null || inArray.length == 0)
            throw new IllegalArgumentException("不能传入空数据。");
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (length <= 0)
            throw new IllegalArgumentException("转换的byte个数不能小于1个。");
        if (length > 8)
            throw new IllegalArgumentException("转换的byte个数不能大于8个字节。");
        if (inArray.length < offset + length)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于转换个数。");

        ByteBuffer buffer = ByteBuffer.allocate(8);

        if (mode == Endian.Big) {
            for (int i = offset + length - 8; i < offset + length; i++) {
                if (i < offset) buffer.put((byte) 0x00);
                else buffer.put(inArray[i]);
            }
            buffer.flip();
        } else {
            //数组下标从0开始，所以从偏移位加7开始往前循环
            for (int i = offset + 7; i >= offset; i--) {
                if (i > offset + length - 1) buffer.put((byte) 0x00);
                else buffer.put(inArray[i]);
            }
            buffer.flip();
        }

        return buffer.getLong();
    }

    /**
     * 计算循环校验和（CRC）
     *
     * @param inArray 需要计算校验和的数据
     * @param offset  偏移位置
     * @param length  计算的数据长度
     * @return 计算结果
     * @throws IllegalArgumentException 传入的数据不合法
     */
    public static byte calculationSumCrc(byte[] inArray, int offset, int length) {
        if (offset < 0)
            throw new IllegalArgumentException("偏移位置必须大于等于0。");
        if (length <= 0)
            throw new IllegalArgumentException("转换的byte个数不能小于1个。");
        if (inArray.length < offset + length)
            throw new IllegalArgumentException("传入数据从偏移位之后的长度小于计算数据的长度。");

        int sum = 0;

        for (int i = offset; i < offset + length; i++) {
            sum = sum + inArray[i];
        }

        return (byte) (sum);
    }

    /**
     * 十六进制字符串转二进制字符串
     *
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
}
