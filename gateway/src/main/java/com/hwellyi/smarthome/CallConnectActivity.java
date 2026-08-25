package com.hwellyi.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
public class CallConnectActivity extends Activity implements View.OnClickListener
{
    ImageView callImagel1;
    ImageView callImagel2;
    ImageView callImagel3;
    ImageView callImager1;
    ImageView callImager2;
    ImageView callImager3;
    TextView  callTextTitle;
    TextView  callTextPrompt;
    int tempCallCount = 0;
    private Ringtone mSoundHandle;
    boolean isExitFlag = false;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutservicecall);
        findViewById(R.id.callImageStop).setOnClickListener(this);
        handler.sendEmptyMessageDelayed(1, 500);
        callImagel1 = findViewById(R.id.callimagel1);
        callImagel2 = findViewById(R.id.callimagel2);
        callImagel3 = findViewById(R.id.callimagel3);
        callImager1 = findViewById(R.id.callimager1);
        callImager2 = findViewById(R.id.callimager2);
        callImager3 = findViewById(R.id.callimager3);
        callTextPrompt = findViewById(R.id.calltextprompt);
        callTextTitle = findViewById(R.id.calltexttitle);
        mSoundHandle = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        mSoundHandle.play();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (!isExitFlag && (msg.what == 0x01))
            {
                //1s一次
                if(tempCallCount <= 30)
                {
                    switch (tempCallCount % 3)
                    {
                        case 0:
                            callImagel1.setVisibility(View.VISIBLE);
                            callImager1.setVisibility(View.VISIBLE);
                            callImagel2.setVisibility(View.INVISIBLE);
                            callImager2.setVisibility(View.INVISIBLE);
                            callImagel3.setVisibility(View.INVISIBLE);
                            callImager3.setVisibility(View.INVISIBLE);
                            break;
                        case 1:
                            callImagel1.setVisibility(View.INVISIBLE);
                            callImager1.setVisibility(View.INVISIBLE);
                            callImagel2.setVisibility(View.VISIBLE);
                            callImager2.setVisibility(View.VISIBLE);
                            callImagel3.setVisibility(View.INVISIBLE);
                            callImager3.setVisibility(View.INVISIBLE);
                            break;
                        case 2:
                            callImagel1.setVisibility(View.INVISIBLE);
                            callImager1.setVisibility(View.INVISIBLE);
                            callImagel2.setVisibility(View.INVISIBLE);
                            callImager2.setVisibility(View.INVISIBLE);
                            callImagel3.setVisibility(View.VISIBLE);
                            callImager3.setVisibility(View.VISIBLE);
                            break;
                        default:break;
                    }
                }
                else
                {
                    if(mSoundHandle.isPlaying())
                    {
                        mSoundHandle.stop();
                    }
                    callImagel1.setVisibility(View.INVISIBLE);
                    callImager1.setVisibility(View.INVISIBLE);
                    callImagel2.setVisibility(View.INVISIBLE);
                    callImager2.setVisibility(View.INVISIBLE);
                    callImagel3.setVisibility(View.INVISIBLE);
                    callImager3.setVisibility(View.INVISIBLE);
                    if(tempCallCount >= 36)
                    {
                        //呼叫失败
                        isExitFlag = true;
                        callTextPrompt.setVisibility(View.VISIBLE);
                        if(mSoundHandle.isPlaying())
                        {
                            mSoundHandle.stop();
                        }
                        finish();
                        return;
                    }
                    else
                    {
                        if((tempCallCount % 2) == 0)
                        {
                            callTextPrompt.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            callTextPrompt.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                tempCallCount++;
                handler.sendEmptyMessageDelayed(1, 500);
            }
        }
    };

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        //Log.i(PublicUse.Tag, "CallConnectActivity key=" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            //返回UI
            isExitFlag = true;
            if(handler != null)
            {
                handler.removeMessages(1);
            }
            if(mSoundHandle.isPlaying())
            {
                mSoundHandle.stop();
            }
            finish();
            return true;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.callImageStop){
            isExitFlag = true;
            if(handler != null)
            {
                handler.removeMessages(1);
            }
            finish();
            if(mSoundHandle.isPlaying())
            {
                mSoundHandle.stop();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        Log.i(PublicUse.Tag, "CallConnectActivity out! ");
        isExitFlag = true;
        if(handler != null)
        {
            handler.removeMessages(1);
        }
        if(mSoundHandle.isPlaying())
        {
            mSoundHandle.stop();
        }
        super.onDestroy();
    }

}
