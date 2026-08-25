package android.serialport;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.hy.greenbuilding.utils.Hex;

import java.io.File;
import me.f1reking.serialportlib.SerialPortHelper;
import me.f1reking.serialportlib.listener.IOpenSerialPortListener;
import me.f1reking.serialportlib.listener.ISerialPortDataListener;
import me.f1reking.serialportlib.listener.Status;

/**
 * 基于 me.f1reking.serialportlib 的串口工具类
 * 功能：串口打开/关闭、数据发送/接收、自动数据拼接、循环发送
 */
public abstract class SerialHelper {
    private byte[] _bLoopData = {48};
    private boolean _isOpen = false;
    private int iBaudRate = 9600;
    private int iDelay = 500;
    private String sPort = "/dev/ttyAS3";

    private SerialPortHelper mF1SerialHelper;
    private SendThread mSendThread;

    public SerialHelper() {
        this("/dev/ttyAS3", 115200);
    }

    public SerialHelper(String paramString) {
        this(paramString, 9600);
    }

    public SerialHelper(String paramString, int paramInt) {
        this.sPort = paramString;
        this.iBaudRate = paramInt;

        if (TextUtils.isEmpty(this.sPort)) {
            this.sPort = "/dev/ttyAS3";
        }
        initF1SerialHelper();
    }

    public SerialHelper(String paramString1, String paramString2) {
        this(paramString1, getBaudRateFromStr(paramString2));
    }


    private void initF1SerialHelper() {
        mF1SerialHelper = new SerialPortHelper.Builder(sPort, iBaudRate)
                .setDataBits(8)
                .setStopBits(1)
                .setParity(0)
                .build();


        mF1SerialHelper.setIOpenSerialPortListener(new IOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {
                _isOpen = true;
                mSendThread = new SendThread();
                mSendThread.setSuspendFlag();
                mSendThread.start();
                Log.d("SerialHelper", "串口打开成功 → 路径：" + device.getPath());
            }

            @Override
            public void onFail(File device, Status status) {
                // 串口打开失败：回调上层（通过抽象方法扩展）
                _isOpen = false;
                Log.e("SerialHelper", "串口打开失败 → 路径：" + device.getPath() + "，原因：" + status.name());
                onSerialOpenFail(status);
            }
        });

        mF1SerialHelper.setISerialPortDataListener(new ISerialPortDataListener() {
            @Override
            public void onDataReceived(byte[] bytes) {
                splicingRead(bytes);
            }

            @Override
            public void onDataSend(byte[] bytes) {
                onSerialDataSend(bytes);
            }
        });
    }


    // ---------------------- 核心方法：打开串口（适配新库API） ----------------------
    public void open() {
        // 新库打开串口无需手动抛异常，结果通过IOpenSerialPortListener返回
        if (mF1SerialHelper != null && !_isOpen) {
            mF1SerialHelper.open();
        } else {
            Log.w("SerialHelper", "串口已打开或初始化失败，无需重复打开");
        }
    }


    // ---------------------- 核心方法：关闭串口（适配新库API） ----------------------
    public void close() {
        if (mF1SerialHelper == null) return;

        // 1. 中断循环发送线程
        if (mSendThread != null) {
            mSendThread.interrupt();
            mSendThread = null;
        }

        // 2. 新库关闭串口（自动释放流和资源）
        mF1SerialHelper.close();
        _isOpen = false;
        Log.d("SerialHelper", "串口关闭成功");
    }


    // ---------------------- 数据发送方法（适配新库sendBytes/sendTxt） ----------------------
    /**
     * 发送字节数组（适配新库sendBytes）
     */
    public void send(byte[] paramArrayOfByte) {
        if (!_isOpen || mF1SerialHelper == null || paramArrayOfByte == null) {
            Log.w("SerialHelper", "发送失败：串口未打开或数据为空");
            return;
        }
        // 新库sendBytes返回布尔值，可判断发送结果
        boolean sendSuccess = mF1SerialHelper.sendBytes(paramArrayOfByte);
        if (!sendSuccess) {
            Log.e("send ota data", "发送失败：新库发送接口返回错误");
        }else {
            Log.e("send ota data", "发送成功：新库发送接口返回成功" + Hex.bytesToHexString(paramArrayOfByte));
        }
    }

    /**
     * 发送字符串（适配新库sendTxt，默认UTF-8编码）
     */
    public void sendTxt(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            Log.w("SerialHelper", "发送失败：字符串为空");
            return;
        }
        // 直接调用新库sendTxt，无需手动转字节数组
        if (_isOpen && mF1SerialHelper != null) {
            mF1SerialHelper.sendTxt(paramString);
        } else {
            Log.w("SerialHelper", "发送失败：串口未打开");
        }
    }

    /**
     * 发送16进制字符串（新库自带sendHex，补充原逻辑缺失功能）
     */
    public void sendHex(String paramHex) {
        if (TextUtils.isEmpty(paramHex) || !_isOpen || mF1SerialHelper == null) {
            Log.w("SerialHelper", "发送失败：16进制字符串为空或串口未打开");
            return;
        }
        mF1SerialHelper.sendHex(paramHex);
    }


    private void splicingRead(byte[] receivedBytes) {
        if (receivedBytes == null || receivedBytes.length == 0) return;
        Log.d("SerialHelper", "本次接收：" + receivedBytes.length + "字节，累计缓存：" + Hex.bytesToHexString(receivedBytes) );
        onDataReceived(receivedBytes);
    }


    // ---------------------- 循环发送线程（保留原逻辑，适配新库发送接口） ----------------------
    private class SendThread extends Thread {
        public boolean suspendFlag = true; // 暂停标记（true：暂停，false：运行）

        @Override
        public void run() {
            super.run();
            // 线程未中断且串口已打开时循环
            while (!isInterrupted() && _isOpen) {
                synchronized (this) {
                    // 暂停时阻塞等待
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            e.printStackTrace();
                        }
                    }
                }

                // 调用新库发送接口（循环发送默认数据）
                send(_bLoopData);
                try {
                    Thread.sleep(iDelay); // 按延时循环
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            Log.d("SerialHelper", "循环发送线程已停止");
        }

        // 恢复循环发送
        public void setResume() {
            synchronized (this) {
                this.suspendFlag = false;
                notify(); // 唤醒线程
            }
        }

        // 暂停循环发送
        public void setSuspendFlag() {
            synchronized (this) {
                this.suspendFlag = true;
            }
        }
    }

    /**
     * 将源数组追加到目标数组
     *
     * @param byte_1 Sou1原数组1
     * @param byte_2 Sou2原数组2
     * @param size   长度
     * @return bytestr 返回一个新的数组，包括了原数组1和原数组2
     */
    public static byte[] arrayAppend(byte[] byte_1, byte[] byte_2, int size) {
        // java 合并两个byte数组
        if (byte_1 == null && byte_2 == null) {
            return null;
        } else if (byte_1 == null) {
            byte[] byte_3 = new byte[size];
            System.arraycopy(byte_2, 0, byte_3, 0, size);
            return byte_3;
            //return byte_2;
        } else if (byte_2 == null) {
            byte[] byte_3 = new byte[byte_1.length];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            return byte_3;
            //return byte_1;
        } else {
            byte[] byte_3 = new byte[byte_1.length + size];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            System.arraycopy(byte_2, 0, byte_3, byte_1.length, size);
            return byte_3;
        }
    }

    /**
     * 字符串波特率转新库BAUDRATE枚举（兼容原构造方法）
     */
    private static int getBaudRateFromStr(String baudStr) {
        try {
            int baud = Integer.parseInt(baudStr);
            // 匹配新库支持的波特率枚举
            switch (baud) {
                case 9600: return 9600;
                case 19200: return 19200;
                case 38400: return 38400;
                case 57600: return 57600;
                case 115200: return 115200;
                case 230400: return 230400;
                default: return 115200; // 默认115200
            }
        } catch (NumberFormatException e) {
            return 115200; // 转换失败默认115200
        }
    }


    // ---------------------- 对外暴露方法（适配新库API） ----------------------
    /** 启动循环发送 */
    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }

    /** 停止循环发送 */
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    /** 枚举所有可用串口（适配新库getAllDeicesPath） */
    public String[] getAllSerialPorts() {
        if (mF1SerialHelper != null) {
            return mF1SerialHelper.getAllDeicesPath();
        }
        return new String[0]; // 初始化失败返回空数组
    }


    // ---------------------- Getter/Setter（适配新库配置修改逻辑） ----------------------
    public int getBaudRate() {
        return iBaudRate;
    }

    /** 修改波特率（仅串口关闭时生效） */
    public boolean setBaudRate(int paramInt) {
        if (_isOpen) {
            Log.w("SerialHelper", "波特率修改失败：串口已打开");
            return false;
        }
        this.iBaudRate = paramInt;
        // 重新初始化新库实例（配置生效）
        initF1SerialHelper();
        return true;
    }

    public String getPort() {
        return sPort;
    }

    /** 修改串口路径（仅串口关闭时生效） */
    public boolean setPort(String paramString) {
        if (_isOpen) {
            Log.w("SerialHelper", "串口路径修改失败：串口已打开");
            return false;
        }
        this.sPort = paramString;
        // 重新初始化新库实例（配置生效）
        initF1SerialHelper();
        return true;
    }

    public boolean isOpen() {
        return _isOpen;
    }

    public byte[] getbLoopData() {
        return _bLoopData;
    }

    public void setbLoopData(byte[] paramArrayOfByte) {
        this._bLoopData = paramArrayOfByte;
    }

    public int getiDelay() {
        return iDelay;
    }

    public void setiDelay(int paramInt) {
        this.iDelay = paramInt;
    }


    protected abstract void onDataReceived(byte[] receivedData);

    /** 新增：串口打开失败回调（新库要求处理失败状态） */
    protected void onSerialOpenFail(Status status) {
        // 空实现，上层可按需重写（如提示用户“无权限”“设备不存在”等）
    }

    /** 新增：数据发送回调（可选，上层可监听发送状态） */
    protected void onSerialDataSend(byte[] sentData) {
        // 空实现，上层可按需重写（如日志打印、UI更新等）
    }
}