package com.hwellyi.smarthome;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.hwellyi.smarthome.PublicUse.onPrintLogToJni;

/**
 * Created by xia_w on 2017/9/28.
 */

public class NetWork
{
    private DatagramSocket mUDPConnector;

    public NetWork()
    {
        try
        {
            mUDPConnector = new DatagramSocket();
            PublicUse.JniPort = mUDPConnector.getLocalPort();
            new mUDPClientReciveThread().start();
        } catch (SocketException e)
        {
            e.printStackTrace();
        }
    }

    public int onGetPort()
    {
        return mUDPConnector.getLocalPort();
    }

    private class mUDPClientReciveThread extends Thread
    {
        @Override
        public void run()
        {
            byte[] reciveBuff = new byte[1024];
            DatagramPacket recivePacket;
            while (true)
            {
                try
                {
                    for(int j = 0; j < 1024; j++)
                    {
                        reciveBuff[j] = 0;
                    }
                    recivePacket = new DatagramPacket(reciveBuff, reciveBuff.length);
                    mUDPConnector.receive(recivePacket);
                    //收到 数据  解析一下
                    JSONObject tempJson = new JSONObject(new String(reciveBuff));
                    int notifyID = 0;
                    long lcmd1 = 0;
                    long lcmd2 = 0;
                    long lvalue = 0;
                    String jniCMDStr = "";
                    PublicUse.onPrintLogToJni("Java UDP R:" + tempJson.toString());
                    if(!tempJson.isNull("notifyid"))
                    {
                        notifyID = tempJson.getInt("notifyid");
                        if(!tempJson.isNull("lcmd1"))
                        {
                            lcmd1 = tempJson.getLong("lcmd1");
                            if(!tempJson.isNull("lcmd2"))
                            {
                                lcmd2 = tempJson.getLong("lcmd2");
                                if(!tempJson.isNull("lvalue"))
                                {
                                    lvalue = tempJson.getLong("lvalue");
                                    if(!tempJson.isNull("strvalue"))
                                    {
                                        jniCMDStr = tempJson.getString("strvalue");
                                        PublicUse.mainActivity.onJniNotifyCB(notifyID, lcmd1, lcmd2, lvalue, jniCMDStr);
                                    }
                                }
                            }
                        }
                    }
                    } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
