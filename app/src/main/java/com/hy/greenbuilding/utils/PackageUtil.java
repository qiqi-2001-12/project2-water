package com.hy.greenbuilding.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

public class PackageUtil {
    private static String serialNumber ;
    private static String mac;
    /**
     * 获取序列号
     * @return
     */
    public static String getSerialNumber() {
        if(!TextUtils.isEmpty(serialNumber)){
            return serialNumber;
        }
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        serialNumber = serial;
        return serial;
    }

    /**
     * 获取MAC地址
     * @return
     */
    public static String getMAC(){
        if(!TextUtils.isEmpty(mac)){
            return mac;
        }
        String ethernetMacAddress= "";
        // 优先尝试 wlan0（模拟器和大多数设备），其次 eth0
        String[] ifaces = {"eth0", "wlan0"};
        for (String iface : ifaces) {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("/sys/class/net/" + iface + "/address")))) {
                String line = input.readLine();
                if (!StringUtils.isNullOrEmpty(line)) {
                    ethernetMacAddress = line.replace(":", "");
                    break;
                }
            } catch (IOException ex) {
                // not found, try next
            }
        }
        // 模拟器兜底：随机生成一个 MAC
        mac = ethernetMacAddress;
        return  ethernetMacAddress;
    }
    /**
     * 检查网络是否可用
     *
     * @param paramContext
     * @return
     */
    public static boolean checkEnable(Context paramContext) {
//        boolean i = false;

//        NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext
//                .getSystemService("connectivity")).getActiveNetworkInfo();
//        if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable()))
//            return true;
        return false;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 获取IP出错!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
        // return null;
    }

    //GPRS连接下的ip
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("a", ex.toString());
        }
        return null;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */

    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
            return "找不到版本号";
        }
    }




    private static String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File
            .separator + "app/";
    /**
     * 安装APK
     * @param filePath
     * @return
     */
    public static boolean installUseRoot(String filePath) {
        File file = new File(destFileDir,filePath);
        if (TextUtils.isEmpty(file.getPath()))
            throw new IllegalArgumentException("Please check apk file path!");
        boolean result = false;
        Process process = null;
        OutputStream outputStream = null;
        BufferedReader errorStream = null;
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = process.getOutputStream();
            String command = "pm install -r " + file.getPath() + "\n";
            outputStream.write(command.getBytes());
            outputStream.flush();
            outputStream.write("exit\n".getBytes());
            outputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder msg = new StringBuilder();
            String line;
            while ((line = errorStream.readLine()) != null) {
                msg.append(line);
            }
            if (!msg.toString().contains("Failure")) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                outputStream = null;
                errorStream = null;
                process.destroy();
            }
        }
        return result;

    }

    public static void install(Context context,String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    context
                    , "com.hy.greenbuilding"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    public static boolean installAPK(Context context, String apkPath){

        File file=new File(apkPath);
        String apkName=apkPath.substring(apkPath.lastIndexOf(File.separator)+1,apkPath.lastIndexOf(".apk"));
        PackageManager packageManager = context.getPackageManager();
        PackageInstaller packageInstaller = packageManager.getPackageInstaller();
        PackageInstaller.SessionParams params=new PackageInstaller
                .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        PackageInstaller.Session session=null;
        OutputStream outputStream=null;
        FileInputStream inputStream=null;
        try {
            //创建Session
            int sessionId = packageInstaller.createSession(params);
            //开启Session
            session=packageInstaller.openSession(sessionId);
            //获取输出流，用于将apk写入session
            outputStream = session.openWrite(apkName, 0, -1);
            inputStream=new FileInputStream(file);
            byte[] buffer=new byte[4096];
            int n;
            //读取apk文件写入session
            while ((n=inputStream.read(buffer))>0){
                outputStream.write(buffer,0,n);
            }

            //写完需要关闭流，否则会抛异常“files still open”
            inputStream.close();
            inputStream=null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;

            //配置安装完成后发起的intent，通常是打开activity（这里我做了修改，修改为广播，intent并未设置目标参数，后面有需求在这里修改补充）
            Intent intent=new Intent();
            PendingIntent pendingIntent= PendingIntent.getBroadcast(context,0,intent,0);
            IntentSender intentSender = pendingIntent.getIntentSender();
            //提交启动安装
            session.commit(intentSender);
            return true;

        }catch (Exception e){
            e.printStackTrace();
            if(session!=null){
                session.abandon();
            }
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            if(inputStream!=null){
                try {
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }


        return false;


    }

    public static boolean installAPK2(Context context,String apkPath){
        try {

            PackageManager packageManager = context.getPackageManager();
            Class<?> pmClz = packageManager.getClass();
            Class<?> aClass = Class.forName("android.app.PackageInstallObserver");
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object installObserver = constructor.newInstance();
            Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
            method.setAccessible(true);
            method.invoke(packageManager, Uri.fromFile(new File(apkPath)), installObserver, 2, null);

            return true;

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean installApk3(String apkPath){

        //String [ ] args = { "pm" , "install" , "-i" , "com.example", apkPath } ;//7.0用这个，参考的博客说要加 --user，但是我发现使用了反而不成功。
        String [ ] args = { "pm" , "install" , "-r" , apkPath } ;
        ProcessBuilder processBuilder = new ProcessBuilder (args) ;

        Process process = null ;
        BufferedReader successResult = null ;
        BufferedReader errorResult = null ;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();


        try {

            process = processBuilder.start();
            successResult = new BufferedReader ( new InputStreamReader(process.getInputStream ()));
            errorResult = new BufferedReader ( new InputStreamReader(process.getErrorStream ()));
            String s ;

            while ( ( s = successResult . readLine () ) != null ) {
                successMsg.append (s) ;
            }

            while ( ( s = errorResult . readLine () ) != null ) {
                errorMsg.append (s) ;
            }

            return  process.waitFor() == 0 || successMsg.toString().contains("Success");

        }catch (IOException e){

            e.printStackTrace();

        }catch (InterruptedException e){

            e.printStackTrace();

        }finally {

            try {
                if ( successResult != null ) {
                    successResult.close() ;
                }
                if ( errorResult != null ) {
                    errorResult.close() ;
                }
            } catch ( IOException e ) {
                e . printStackTrace() ;
            }
            if ( process != null ) {
                process. destroy() ;
            }
        }

        return  false;

    }

    /**
     * 生成简单二维码
     *
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap( int width, int height,String error_correction_level, String margin, int color_black, int color_white) {
        String content = getMAC();
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
