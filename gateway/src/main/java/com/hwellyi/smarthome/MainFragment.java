package com.hwellyi.smarthome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements View.OnClickListener
{
    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RelativeLayout mWeatherLayout;
    public final String Tag = "Log_View";
    int WeatherType = PublicUse.AnimationSunny;
    String PM2_5Value = "51";
    String TempValue = "31";
    String MaxValue = "34";
    String MinValue = "28";
    String CondValue = "正在更新";
    boolean mGetWeatherOK = false;
    boolean mIsGetWeatherFlag = false;
    String HumiValue = "59";
    ImageView mImageViewWeather;

    TextView textViewPm25value;
    TextView textViewTempvalue;
    TextView textViewHumivalue;
    TextView textCondValue;
    Button mBtnSet;
    boolean mScreenOn = true;
    TypeFinalClass mFinalClass = null;

    public MainFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View retView = inflater.inflate(R.layout.fragment_main, container, false);
        mWeatherLayout = retView.findViewById(R.id.layoutWeather);
        mWeatherLayout.setVisibility(View.INVISIBLE);
        mImageViewWeather = retView.findViewById(R.id.imageView_weather);
        mImageViewWeather.setBackgroundResource(R.drawable.image_start00);
        mBtnSet = retView.findViewById(R.id.btnMainSet);
        textViewPm25value = retView.findViewById(R.id.textview_pm25value);
        textViewTempvalue = retView.findViewById(R.id.textView_tempvalue);
        textViewHumivalue = retView.findViewById(R.id.textview_humivalue);
        textCondValue = retView.findViewById(R.id.text_condvalue);
        //登记所有按钮，添加按键消息
        retView.findViewById(R.id.btnMainPhone).setOnClickListener(this);
        retView.findViewById(R.id.imageMainBHome).setOnClickListener(this);
        retView.findViewById(R.id.imageMainLHome).setOnClickListener(this);
        retView.findViewById(R.id.imageMainSleep).setOnClickListener(this);
        mBtnSet.setOnClickListener(this);
        mFinalClass = new TypeFinalClass(retView);
        handler.sendEmptyMessageDelayed(2, 3000);
        handler.postDelayed(runnable, 40);
        PublicUse.onPrintLogToJni("MainFragment init!");
        return retView;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case 0x01:
                {
                    //更新首页天气值
                    switch(PM2_5Value.length())
                    {
                        case 2:textViewPm25value.setText(" " + PM2_5Value);break;
                        case 3:textViewPm25value.setText(PM2_5Value);break;
                        default:textViewPm25value.setText("  " + PM2_5Value);
                    }
                    switch(TempValue.length())
                    {
                        case 2:textViewTempvalue.setText(" " + TempValue + "°");break;
                        case 3:textViewTempvalue.setText(TempValue + "°");break;
                        default:textViewTempvalue.setText("  " + TempValue + "°");break;
                    }
                    switch(HumiValue.length())
                    {
                        case 2:textViewHumivalue.setText(" " + HumiValue);break;
                        case 3:textViewHumivalue.setText(HumiValue);break;
                        default:textViewHumivalue.setText("  " + HumiValue);break;
                    }
                    switch (CondValue.length())
                    {
                        case 1:textCondValue.setText(MinValue + "°~" + MaxValue + "°   " + CondValue);break;
                        case 2:textCondValue.setText(MinValue + "°~" + MaxValue + "°  " + CondValue);break;
                        case 3:textCondValue.setText(MinValue + "°~" + MaxValue + "° " + CondValue);break;
                        default:textCondValue.setText(MinValue + "°~" + MaxValue + "°" + CondValue);break;
                    }

                    //确定当前在天气界面
                    if((mFinalClass.lastAnimationType & 0xF0) == 0x10)
                    {
                        //直接更新界面
                        if(mFinalClass.lastAnimationType != WeatherType)
                        {
                            mFinalClass.onChangeAnimationType(WeatherType);
                        }
                    }
                    else
                    {
                        //更新在保存记录里面
                        if(mFinalClass.lastSaveAnimationType != WeatherType)
                        {
                            mFinalClass.lastSaveAnimationType = WeatherType;
                        }
                    }
                    removeMessages(1);
                    sendEmptyMessageDelayed(0x02, 3000000);//50分钟更新一次
                }
                break;
                case 0x02:
                    if(!mIsGetWeatherFlag)
                    {
                        removeMessages(2);
                        new Thread(mGetWeather).start();
                    }
                    break;
            }
        }

    };

    Runnable mGetWeather = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                mIsGetWeatherFlag = true;
                mGetWeatherOK = false;
                {
                    PublicUse.onPrintLogToJni("正在更新天气 ");
                    URL url = new URL("http://api.hwellyi.com/v1/wealoc/weathers" + PublicUse.mJniFunCB.onGetJniToken());
                    //打开连接
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-type", "application/json");
                    urlConnection.setRequestProperty("Authorization","Basic MjUwODY4MTAyMDgwNzU3Nzc6MjAyNTBkOGEyNTdmMjliZWQ0ZTVjODRkYWZjYzg4MTk=");
                    urlConnection.setInstanceFollowRedirects(false);
                    urlConnection.connect();
                    if(HttpURLConnection.HTTP_OK == urlConnection.getResponseCode())
                    {
                        //得到输入流
                        InputStream is =urlConnection.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while(-1 != (len = is.read(buffer))){
                            baos.write(buffer,0,len);
                            baos.flush();
                        }
                        try
                        {
                            CondValue = "正在更新";
                            JSONObject tempWeathersJson = new JSONObject(baos.toString("utf-8"));
                            if(!tempWeathersJson.isNull("data"))
                            {
                                JSONObject tempJson = new JSONObject(tempWeathersJson.getString("data"));
                                if(!tempJson.isNull("air_quality"))
                                {
                                    JSONObject tempSubJson = new JSONObject(tempJson.getString("air_quality"));
                                    if (!tempSubJson.isNull("pm2_5"))
                                    {
                                        PM2_5Value = tempSubJson.getString("pm2_5");
                                    }
                                }
                                if(!tempJson.isNull("weather"))
                                {
                                    JSONObject tempSubJson = new JSONObject(tempJson.getString("weather"));
                                    if (!tempSubJson.isNull("temp"))
                                    {
                                        TempValue = tempSubJson.getString("temp");
                                    }
                                    if (!tempSubJson.isNull("humidity"))
                                    {
                                        HumiValue = tempSubJson.getString("humidity");
                                    }
                                    if(!tempSubJson.isNull("temp_low"))
                                    {
                                        MinValue = tempSubJson.getString("temp_low");
                                    }
                                    if(!tempSubJson.isNull("temp_high"))
                                    {
                                        MaxValue = tempSubJson.getString("temp_high");
                                    }
                                    if (!tempSubJson.isNull("condition"))
                                    {
                                        CondValue = tempSubJson.getString("condition");
                                        if(CondValue.equals("睛"))
                                        {
                                            WeatherType = PublicUse.AnimationSunny;
                                        }
                                        else if(CondValue.equals("阴") || CondValue.equals("多云"))
                                        {
                                            WeatherType = PublicUse.AnimationCloudy;
                                        }
                                        else if(CondValue.indexOf('雨') >= 0)
                                        {
                                            WeatherType = PublicUse.AnimationRain;
                                        }
                                        else if(CondValue.indexOf('雪') >= 0)
                                        {
                                            WeatherType = PublicUse.AnimationSnow;
                                        }
                                        mGetWeatherOK = true;
                                        handler.sendEmptyMessageDelayed(0x01, 2000);
                                    }
                                }
                            }
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }  catch (IOException e) {
                e.printStackTrace();
            }
            if(!mGetWeatherOK)
            {
                if(PublicUse.mJniFunCB.onGetNetWorkStatus())
                {
                    handler.sendEmptyMessageDelayed(0x02, 10000);
                }
                else
                {
                    handler.sendEmptyMessageDelayed(0x02, 3000000);
                }
            }
            mIsGetWeatherFlag = false;
        }
    };

    // 线程类
    long lastTimeMS = System.currentTimeMillis();
    Runnable runnable = new Runnable()
    {
        long minTimeValue = 0;
        long maxTimeValue = 0;
        int tempCount = 0;
        @Override
        public void run()
        {
            {
                PowerManager powerManager = (PowerManager) PublicUse.mainActivity.getSystemService(Context.POWER_SERVICE);
                if(powerManager.isScreenOn())
                {
                    if(!mScreenOn)
                    {
                        mScreenOn = true;
                        PublicUse.onPrintLogToJni("screen on xx!");
                    }
                    long currentTimeMS = System.currentTimeMillis();
                    long tempLong = currentTimeMS - lastTimeMS;
                    lastTimeMS = currentTimeMS;
                    if(tempLong > 40)
                    {
                        tempLong = 80 - tempLong;
                    }
                    else
                    {
                        tempLong = 40;
                    }
                    if(tempLong < 10) tempLong = 10;
                    handler.postDelayed(this, tempLong);
                    Bitmap tempBitmap = mFinalClass.onGetBitmap();
                    if(tempBitmap != null)
                    {
                        mImageViewWeather.setImageBitmap(tempBitmap);
                    }
                    tempCount++;
                    if(tempCount >= 25)
                    {
                        maxTimeValue = currentTimeMS;
                        minTimeValue = maxTimeValue - minTimeValue;
                        minTimeValue = maxTimeValue;
                        tempCount = 0;
                    }
                }
                else
                {
                    if(mScreenOn)
                    {
                        mScreenOn = false;
                        PublicUse.onPrintLogToJni("screen off!");
                    }
                    handler.postDelayed(this, 200);
                }
            }
        }
    };

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.btnMainPhone){//拨打售后服务  暂未开通
            if(PublicUse.isDemoFlag)
            {

                Intent setIntent = new Intent();
                setIntent.setClass(MainFragment.this.getContext(), CallConnectActivity.class);
                startActivity(setIntent);
            }
        }else if(v.getId() ==  R.id.imageMainBHome){//回家场景
            PublicUse.mJniFunCB.onSetSceneStatus(PublicUse.SCENE_HOME);
            mWeatherLayout.setVisibility(View.INVISIBLE);
            mFinalClass.onChangeAnimationType(PublicUse.AnimationBackHome);
        }else if(v.getId() ==  R.id.imageMainLHome){//离家场景
            PublicUse.mJniFunCB.onSetSceneStatus(PublicUse.SCENE_LEAVE);
            mWeatherLayout.setVisibility(View.INVISIBLE);
            mFinalClass.onChangeAnimationType(PublicUse.AnimationLeaveHome);
        }else if(v.getId() ==  R.id.imageMainSleep){//睡觉场景
            PublicUse.mJniFunCB.onSetSceneStatus(PublicUse.SCENE_SLEEP);
            mWeatherLayout.setVisibility(View.INVISIBLE);
            mFinalClass.onChangeAnimationType(PublicUse.AnimationSleep);
        }else if(v.getId() ==  R.id.btnMainSet){//系统设置
            Intent setIntent = new Intent();
            setIntent.setClass(MainFragment.this.getContext(), SettingActivity.class);
            startActivity(setIntent);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // Update argument type and name
    }
}
