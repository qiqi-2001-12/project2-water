package com.hwellyi.smarthome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by xia_w on 2017/8/29.
 */

public class TypeFinalClass
{
    //[0,108) [0,23)开场动画和[23,108)天气动画
    //[108,165) 回家 57
    //[165,235) 离家 70
    //[235,333) 睡觉 98
    Bitmap[]tempBitmapBuff = new Bitmap[333];
    int lastAnimationType = 0;
    int lastSaveAnimationType = 0;
    int lastAnimationIndex = 0;
    int lastThreadIndex = 0;
    View mRes;
    RelativeLayout mWeatherLayout;
    //启动一个线程去加载  其它图片
    TypeFinalClass(final View mainview)
    {
        lastAnimationType = 0;
        lastSaveAnimationType = 0;
        lastAnimationIndex = 0;
        lastThreadIndex = 0;
        for (int i = 0; i < 333; i++)
        {
            tempBitmapBuff[i] = null;
        }
        //默认天睛
        mRes = mainview;
        mWeatherLayout = mRes.findViewById(R.id.layoutWeather);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //启动动画
                for (int i = 0; i < 23; i++)
                {
                    tempBitmapBuff[i] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(PublicUse.AnimationStart, i));
                }
                //天气动画
                for (int i = 0; i < 85; i++)
                {
                    tempBitmapBuff[i + 23] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(PublicUse.AnimationSunny, i));
                }
                lastSaveAnimationType = lastAnimationType = PublicUse.AnimationSunny;
            }
        }).start();
    };
    Bitmap onGetBitmap()
    {
        switch (lastAnimationType)
        {
            case PublicUse.AnimationBackHome://回家场景动画
                if(lastAnimationIndex >= 165)
                {
                    (mRes.findViewById(R.id.imageMainBHome)).setBackgroundResource(R.drawable.btm_ghome_off);
                    onChangeAnimationType(lastSaveAnimationType);
                }
                break;
            case PublicUse.AnimationLeaveHome://离家场景动画
                if(lastAnimationIndex >= 235)
                {
                    (mRes.findViewById(R.id.imageMainLHome)).setBackgroundResource(R.drawable.btm_lhome_off);
                    onChangeAnimationType(lastSaveAnimationType);
                }
                break;
            case PublicUse.AnimationSleep://睡觉场景动画
                if(lastAnimationIndex >= 333)
                {
                    (mRes.findViewById(R.id.imageMainSleep)).setBackgroundResource(R.drawable.btm_sleep_off);
                    onChangeAnimationType(lastSaveAnimationType);
                }
                break;
            default://这个是天气
                if(lastAnimationType == PublicUse.AnimationRain)
                {
                    if(lastAnimationIndex > 106)
                    {
                        lastAnimationIndex = 23;
                    }
                }
                else
                {
                    if(lastAnimationIndex > 107)
                    {
                        lastAnimationIndex = 23;
                    }
                }
                if(lastAnimationIndex == 21)
                {
                    mWeatherLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
        if(tempBitmapBuff[lastAnimationIndex] != null)
        {
            lastAnimationIndex++;
            return tempBitmapBuff[lastAnimationIndex - 1];
        }
        else
        {
            return null;
        }
    };

    boolean onChangeAnimationType(final int type)
    {
        lastAnimationIndex = 0;//定位到启动动画
        ((ImageView)mRes.findViewById(R.id.imageView_weather)).setImageBitmap(tempBitmapBuff[0]);
        //((MainFragment)PublicUse.mainActivity.fragmentList.get(0)).handler.sendEmptyMessage(3);
        //先释放以前的内存 再分配新的内存
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                lastThreadIndex++;
                int saveThreadIndex = lastThreadIndex;
                //PublicUse.mJniFunCB.onPrintLogToJni("in=" + String.valueOf(saveThreadIndex) + "! type=" + String.valueOf(lastAnimationType) + "index=" + String.valueOf(lastAnimationIndex));
                //if(lastAnimationType != type)
                {
                    //立即切换
                    int tempType = lastAnimationType;
                    lastAnimationType = type;
                    //先释放内存
                    Bitmap tempBitMap = null;
                    int i = 0;
                    i = 23;
                    while((i < 333) && (saveThreadIndex == lastThreadIndex))//释放所有内存
                    {
                        if((tempBitmapBuff[i] != null) && (!tempBitmapBuff[i].isRecycled()))
                        {
                            tempBitmapBuff[i].recycle();
                        }
                        tempBitmapBuff[i] = null;
                        i++;
                    }
                    //分配内存
                    if((type & 0xF0) == 0x10)//天气
                    {
                        lastAnimationIndex = 21;
                        i = 0;
                        while((i < 85) && (saveThreadIndex == lastThreadIndex))
                        {
                            tempBitmapBuff[i + 23] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(type, i));
                            i++;
                        }
                    }
                    else
                    {
                        if((tempType & 0xF0) == 0x10)
                        {
                            lastSaveAnimationType = tempType;
                        }
                        if(type == PublicUse.AnimationBackHome)
                        {
                            lastAnimationIndex = 108;
                            i = 0;
                            while((i < 57) && (saveThreadIndex == lastThreadIndex))
                            {
                                tempBitmapBuff[i + 108] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(PublicUse.AnimationBackHome, i));
                                i++;
                            }
                        }
                        else if(type == PublicUse.AnimationLeaveHome)
                        {
                            lastAnimationIndex = 165;
                            i = 0;
                            while((i < 70) && (saveThreadIndex == lastThreadIndex))
                            {
                                tempBitmapBuff[i + 165] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(PublicUse.AnimationLeaveHome, i));
                                i++;
                            }
                        }
                        else if(type == PublicUse.AnimationSleep)
                        {
                            lastAnimationIndex = 235;
                            i = 0;
                            while((i < 98) && (saveThreadIndex == lastThreadIndex))
                            {
                                tempBitmapBuff[i + 235] = BitmapFactory.decodeResource(mRes.getResources(), onGetDrawableID(PublicUse.AnimationSleep, i));
                                i++;
                            }
                        }
                    }
                }
                //PublicUse.mJniFunCB.onPrintLogToJni("out=" + String.valueOf(saveThreadIndex) + "! type=" + String.valueOf(lastAnimationType) + "index=" + String.valueOf(lastAnimationIndex));
            }
        }).start();
        return true;
    }

    int onGetDrawableID(int type, int index)
    {
        if(index < 0) index = 0;
        int retID = R.drawable.image_start00;
        switch(type)
        {
            case PublicUse.AnimationStart:
                switch (index)
                {
                    case 0:retID = R.drawable.image_start00;break;
                    case 1:retID = R.drawable.image_start01;break;
                    case 2:retID = R.drawable.image_start02;break;
                    case 3:retID = R.drawable.image_start03;break;
                    case 4:retID = R.drawable.image_start04;break;
                    case 5:retID = R.drawable.image_start05;break;
                    case 6:retID = R.drawable.image_start06;break;
                    case 7:retID = R.drawable.image_start07;break;
                    case 8:retID = R.drawable.image_start08;break;
                    case 9:retID = R.drawable.image_start09;break;
                    case 10:retID = R.drawable.image_start10;break;
                    case 11:retID = R.drawable.image_start11;break;
                    case 12:retID = R.drawable.image_start12;break;
                    case 13:retID = R.drawable.image_start13;break;
                    case 14:retID = R.drawable.image_start14;break;
                    case 15:retID = R.drawable.image_start15;break;
                    case 16:retID = R.drawable.image_start16;break;
                    case 17:retID = R.drawable.image_start17;break;
                    case 18:retID = R.drawable.image_start18;break;
                    case 19:retID = R.drawable.image_start19;break;
                    case 20:retID = R.drawable.image_start20;break;
                    case 21:retID = R.drawable.image_start21;break;
                    case 22:retID = R.drawable.image_start22;break;
                }
                break;
            case PublicUse.AnimationSunny:
                switch(index)
                {
                    case 0:retID = R.drawable.image_sunny00;break;
                    case 1:retID = R.drawable.image_sunny01;break;
                    case 2:retID = R.drawable.image_sunny02;break;
                    case 3:retID = R.drawable.image_sunny03;break;
                    case 4:retID = R.drawable.image_sunny04;break;
                    case 5:retID = R.drawable.image_sunny05;break;
                    case 6:retID = R.drawable.image_sunny06;break;
                    case 7:retID = R.drawable.image_sunny07;break;
                    case 8:retID = R.drawable.image_sunny08;break;
                    case 9:retID = R.drawable.image_sunny09;break;
                    case 10:retID = R.drawable.image_sunny10;break;
                    case 11:retID = R.drawable.image_sunny11;break;
                    case 12:retID = R.drawable.image_sunny12;break;
                    case 13:retID = R.drawable.image_sunny13;break;
                    case 14:retID = R.drawable.image_sunny14;break;
                    case 15:retID = R.drawable.image_sunny15;break;
                    case 16:retID = R.drawable.image_sunny16;break;
                    case 17:retID = R.drawable.image_sunny17;break;
                    case 18:retID = R.drawable.image_sunny18;break;
                    case 19:retID = R.drawable.image_sunny19;break;
                    case 20:retID = R.drawable.image_sunny20;break;
                    case 21:retID = R.drawable.image_sunny21;break;
                    case 22:retID = R.drawable.image_sunny22;break;
                    case 23:retID = R.drawable.image_sunny23;break;
                    case 24:retID = R.drawable.image_sunny24;break;
                    case 25:retID = R.drawable.image_sunny25;break;
                    case 26:retID = R.drawable.image_sunny26;break;
                    case 27:retID = R.drawable.image_sunny27;break;
                    case 28:retID = R.drawable.image_sunny28;break;
                    case 29:retID = R.drawable.image_sunny29;break;
                    case 30:retID = R.drawable.image_sunny30;break;
                    case 31:retID = R.drawable.image_sunny31;break;
                    case 32:retID = R.drawable.image_sunny32;break;
                    case 33:retID = R.drawable.image_sunny33;break;
                    case 34:retID = R.drawable.image_sunny34;break;
                    case 35:retID = R.drawable.image_sunny35;break;
                    case 36:retID = R.drawable.image_sunny36;break;
                    case 37:retID = R.drawable.image_sunny37;break;
                    case 38:retID = R.drawable.image_sunny38;break;
                    case 39:retID = R.drawable.image_sunny39;break;
                    case 40:retID = R.drawable.image_sunny40;break;
                    case 41:retID = R.drawable.image_sunny41;break;
                    case 42:retID = R.drawable.image_sunny42;break;
                    case 43:retID = R.drawable.image_sunny43;break;
                    case 44:retID = R.drawable.image_sunny44;break;
                    case 45:retID = R.drawable.image_sunny45;break;
                    case 46:retID = R.drawable.image_sunny46;break;
                    case 47:retID = R.drawable.image_sunny47;break;
                    case 48:retID = R.drawable.image_sunny48;break;
                    case 49:retID = R.drawable.image_sunny49;break;
                    case 50:retID = R.drawable.image_sunny50;break;
                    case 51:retID = R.drawable.image_sunny51;break;
                    case 52:retID = R.drawable.image_sunny52;break;
                    case 53:retID = R.drawable.image_sunny53;break;
                    case 54:retID = R.drawable.image_sunny54;break;
                    case 55:retID = R.drawable.image_sunny55;break;
                    case 56:retID = R.drawable.image_sunny56;break;
                    case 57:retID = R.drawable.image_sunny57;break;
                    case 58:retID = R.drawable.image_sunny58;break;
                    case 59:retID = R.drawable.image_sunny59;break;
                    case 60:retID = R.drawable.image_sunny60;break;
                    case 61:retID = R.drawable.image_sunny61;break;
                    case 62:retID = R.drawable.image_sunny62;break;
                    case 63:retID = R.drawable.image_sunny63;break;
                    case 64:retID = R.drawable.image_sunny64;break;
                    case 65:retID = R.drawable.image_sunny65;break;
                    case 66:retID = R.drawable.image_sunny66;break;
                    case 67:retID = R.drawable.image_sunny67;break;
                    case 68:retID = R.drawable.image_sunny68;break;
                    case 69:retID = R.drawable.image_sunny69;break;
                    case 70:retID = R.drawable.image_sunny70;break;
                    case 71:retID = R.drawable.image_sunny71;break;
                    case 72:retID = R.drawable.image_sunny72;break;
                    case 73:retID = R.drawable.image_sunny73;break;
                    case 74:retID = R.drawable.image_sunny74;break;
                    case 75:retID = R.drawable.image_sunny75;break;
                    case 76:retID = R.drawable.image_sunny76;break;
                    case 77:retID = R.drawable.image_sunny77;break;
                    case 78:retID = R.drawable.image_sunny78;break;
                    case 79:retID = R.drawable.image_sunny79;break;
                    case 80:retID = R.drawable.image_sunny80;break;
                    case 81:retID = R.drawable.image_sunny81;break;
                    case 82:retID = R.drawable.image_sunny82;break;
                    case 83:retID = R.drawable.image_sunny83;break;
                    case 84:retID = R.drawable.image_sunny84;break;
                }
                break;
            case PublicUse.AnimationRain:
                switch (index)
                {
                    case 0:retID = R.drawable.image_rain00;break;
                    case 1:retID = R.drawable.image_rain01;break;
                    case 2:retID = R.drawable.image_rain02;break;
                    case 3:retID = R.drawable.image_rain03;break;
                    case 4:retID = R.drawable.image_rain04;break;
                    case 5:retID = R.drawable.image_rain05;break;
                    case 6:retID = R.drawable.image_rain06;break;
                    case 7:retID = R.drawable.image_rain07;break;
                    case 8:retID = R.drawable.image_rain08;break;
                    case 9:retID = R.drawable.image_rain09;break;
                    case 10:retID = R.drawable.image_rain10;break;
                    case 11:retID = R.drawable.image_rain11;break;
                    case 12:retID = R.drawable.image_rain12;break;
                    case 13:retID = R.drawable.image_rain13;break;
                    case 14:retID = R.drawable.image_rain14;break;
                    case 15:retID = R.drawable.image_rain15;break;
                    case 16:retID = R.drawable.image_rain16;break;
                    case 17:retID = R.drawable.image_rain17;break;
                    case 18:retID = R.drawable.image_rain18;break;
                    case 19:retID = R.drawable.image_rain19;break;
                    case 20:retID = R.drawable.image_rain20;break;
                    case 21:retID = R.drawable.image_rain21;break;
                    case 22:retID = R.drawable.image_rain22;break;
                    case 23:retID = R.drawable.image_rain23;break;
                    case 24:retID = R.drawable.image_rain24;break;
                    case 25:retID = R.drawable.image_rain25;break;
                    case 26:retID = R.drawable.image_rain26;break;
                    case 27:retID = R.drawable.image_rain27;break;
                    case 28:retID = R.drawable.image_rain28;break;
                    case 29:retID = R.drawable.image_rain29;break;
                    case 30:retID = R.drawable.image_rain30;break;
                    case 31:retID = R.drawable.image_rain31;break;
                    case 32:retID = R.drawable.image_rain32;break;
                    case 33:retID = R.drawable.image_rain33;break;
                    case 34:retID = R.drawable.image_rain34;break;
                    case 35:retID = R.drawable.image_rain35;break;
                    case 36:retID = R.drawable.image_rain36;break;
                    case 37:retID = R.drawable.image_rain37;break;
                    case 38:retID = R.drawable.image_rain38;break;
                    case 39:retID = R.drawable.image_rain39;break;
                    case 40:retID = R.drawable.image_rain40;break;
                    case 41:retID = R.drawable.image_rain41;break;
                    case 42:retID = R.drawable.image_rain42;break;
                    case 43:retID = R.drawable.image_rain43;break;
                    case 44:retID = R.drawable.image_rain44;break;
                    case 45:retID = R.drawable.image_rain45;break;
                    case 46:retID = R.drawable.image_rain46;break;
                    case 47:retID = R.drawable.image_rain47;break;
                    case 48:retID = R.drawable.image_rain48;break;
                    case 49:retID = R.drawable.image_rain49;break;
                    case 50:retID = R.drawable.image_rain50;break;
                    case 51:retID = R.drawable.image_rain51;break;
                    case 52:retID = R.drawable.image_rain52;break;
                    case 53:retID = R.drawable.image_rain53;break;
                    case 54:retID = R.drawable.image_rain54;break;
                    case 55:retID = R.drawable.image_rain55;break;
                    case 56:retID = R.drawable.image_rain56;break;
                    case 57:retID = R.drawable.image_rain57;break;
                    case 58:retID = R.drawable.image_rain58;break;
                    case 59:retID = R.drawable.image_rain59;break;
                    case 60:retID = R.drawable.image_rain60;break;
                    case 61:retID = R.drawable.image_rain61;break;
                    case 62:retID = R.drawable.image_rain62;break;
                    case 63:retID = R.drawable.image_rain63;break;
                    case 64:retID = R.drawable.image_rain64;break;
                    case 65:retID = R.drawable.image_rain65;break;
                    case 66:retID = R.drawable.image_rain66;break;
                    case 67:retID = R.drawable.image_rain67;break;
                    case 68:retID = R.drawable.image_rain68;break;
                    case 69:retID = R.drawable.image_rain69;break;
                    case 70:retID = R.drawable.image_rain70;break;
                    case 71:retID = R.drawable.image_rain71;break;
                    case 72:retID = R.drawable.image_rain72;break;
                    case 73:retID = R.drawable.image_rain73;break;
                    case 74:retID = R.drawable.image_rain74;break;
                    case 75:retID = R.drawable.image_rain75;break;
                    case 76:retID = R.drawable.image_rain76;break;
                    case 77:retID = R.drawable.image_rain77;break;
                    case 78:retID = R.drawable.image_rain78;break;
                    case 79:retID = R.drawable.image_rain79;break;
                    case 80:retID = R.drawable.image_rain80;break;
                    case 81:retID = R.drawable.image_rain81;break;
                    case 82:retID = R.drawable.image_rain82;break;
                    case 83:retID = R.drawable.image_rain83;break;
                    case 84:retID = R.drawable.image_rain84;break;
                }
                break;
            case PublicUse.AnimationCloudy:
                switch (index)
                {
                    case 0:retID = R.drawable.image_cloudy00;break;
                    case 1:retID = R.drawable.image_cloudy01;break;
                    case 2:retID = R.drawable.image_cloudy02;break;
                    case 3:retID = R.drawable.image_cloudy03;break;
                    case 4:retID = R.drawable.image_cloudy04;break;
                    case 5:retID = R.drawable.image_cloudy05;break;
                    case 6:retID = R.drawable.image_cloudy06;break;
                    case 7:retID = R.drawable.image_cloudy07;break;
                    case 8:retID = R.drawable.image_cloudy08;break;
                    case 9:retID = R.drawable.image_cloudy09;break;
                    case 10:retID = R.drawable.image_cloudy10;break;
                    case 11:retID = R.drawable.image_cloudy11;break;
                    case 12:retID = R.drawable.image_cloudy12;break;
                    case 13:retID = R.drawable.image_cloudy13;break;
                    case 14:retID = R.drawable.image_cloudy14;break;
                    case 15:retID = R.drawable.image_cloudy15;break;
                    case 16:retID = R.drawable.image_cloudy16;break;
                    case 17:retID = R.drawable.image_cloudy17;break;
                    case 18:retID = R.drawable.image_cloudy18;break;
                    case 19:retID = R.drawable.image_cloudy19;break;
                    case 20:retID = R.drawable.image_cloudy20;break;
                    case 21:retID = R.drawable.image_cloudy21;break;
                    case 22:retID = R.drawable.image_cloudy22;break;
                    case 23:retID = R.drawable.image_cloudy23;break;
                    case 24:retID = R.drawable.image_cloudy24;break;
                    case 25:retID = R.drawable.image_cloudy25;break;
                    case 26:retID = R.drawable.image_cloudy26;break;
                    case 27:retID = R.drawable.image_cloudy27;break;
                    case 28:retID = R.drawable.image_cloudy28;break;
                    case 29:retID = R.drawable.image_cloudy29;break;
                    case 30:retID = R.drawable.image_cloudy30;break;
                    case 31:retID = R.drawable.image_cloudy31;break;
                    case 32:retID = R.drawable.image_cloudy32;break;
                    case 33:retID = R.drawable.image_cloudy33;break;
                    case 34:retID = R.drawable.image_cloudy34;break;
                    case 35:retID = R.drawable.image_cloudy35;break;
                    case 36:retID = R.drawable.image_cloudy36;break;
                    case 37:retID = R.drawable.image_cloudy37;break;
                    case 38:retID = R.drawable.image_cloudy38;break;
                    case 39:retID = R.drawable.image_cloudy39;break;
                    case 40:retID = R.drawable.image_cloudy40;break;
                    case 41:retID = R.drawable.image_cloudy41;break;
                    case 42:retID = R.drawable.image_cloudy42;break;
                    case 43:retID = R.drawable.image_cloudy43;break;
                    case 44:retID = R.drawable.image_cloudy44;break;
                    case 45:retID = R.drawable.image_cloudy45;break;
                    case 46:retID = R.drawable.image_cloudy46;break;
                    case 47:retID = R.drawable.image_cloudy47;break;
                    case 48:retID = R.drawable.image_cloudy48;break;
                    case 49:retID = R.drawable.image_cloudy49;break;
                    case 50:retID = R.drawable.image_cloudy50;break;
                    case 51:retID = R.drawable.image_cloudy51;break;
                    case 52:retID = R.drawable.image_cloudy52;break;
                    case 53:retID = R.drawable.image_cloudy53;break;
                    case 54:retID = R.drawable.image_cloudy54;break;
                    case 55:retID = R.drawable.image_cloudy55;break;
                    case 56:retID = R.drawable.image_cloudy56;break;
                    case 57:retID = R.drawable.image_cloudy57;break;
                    case 58:retID = R.drawable.image_cloudy58;break;
                    case 59:retID = R.drawable.image_cloudy59;break;
                    case 60:retID = R.drawable.image_cloudy60;break;
                    case 61:retID = R.drawable.image_cloudy61;break;
                    case 62:retID = R.drawable.image_cloudy62;break;
                    case 63:retID = R.drawable.image_cloudy63;break;
                    case 64:retID = R.drawable.image_cloudy64;break;
                    case 65:retID = R.drawable.image_cloudy65;break;
                    case 66:retID = R.drawable.image_cloudy66;break;
                    case 67:retID = R.drawable.image_cloudy67;break;
                    case 68:retID = R.drawable.image_cloudy68;break;
                    case 69:retID = R.drawable.image_cloudy69;break;
                    case 70:retID = R.drawable.image_cloudy70;break;
                    case 71:retID = R.drawable.image_cloudy71;break;
                    case 72:retID = R.drawable.image_cloudy72;break;
                    case 73:retID = R.drawable.image_cloudy73;break;
                    case 74:retID = R.drawable.image_cloudy74;break;
                    case 75:retID = R.drawable.image_cloudy75;break;
                    case 76:retID = R.drawable.image_cloudy76;break;
                    case 77:retID = R.drawable.image_cloudy77;break;
                    case 78:retID = R.drawable.image_cloudy78;break;
                    case 79:retID = R.drawable.image_cloudy79;break;
                    case 80:retID = R.drawable.image_cloudy80;break;
                    case 81:retID = R.drawable.image_cloudy81;break;
                    case 82:retID = R.drawable.image_cloudy82;break;
                    case 83:retID = R.drawable.image_cloudy83;break;
                    case 84:retID = R.drawable.image_cloudy84;break;
                }
                break;
            case PublicUse.AnimationSnow:
                switch (index)
                {
                    case 0:retID = R.drawable.image_snow00;break;
                    case 1:retID = R.drawable.image_snow01;break;
                    case 2:retID = R.drawable.image_snow02;break;
                    case 3:retID = R.drawable.image_snow03;break;
                    case 4:retID = R.drawable.image_snow04;break;
                    case 5:retID = R.drawable.image_snow05;break;
                    case 6:retID = R.drawable.image_snow06;break;
                    case 7:retID = R.drawable.image_snow07;break;
                    case 8:retID = R.drawable.image_snow08;break;
                    case 9:retID = R.drawable.image_snow09;break;
                    case 10:retID = R.drawable.image_snow10;break;
                    case 11:retID = R.drawable.image_snow11;break;
                    case 12:retID = R.drawable.image_snow12;break;
                    case 13:retID = R.drawable.image_snow13;break;
                    case 14:retID = R.drawable.image_snow14;break;
                    case 15:retID = R.drawable.image_snow15;break;
                    case 16:retID = R.drawable.image_snow16;break;
                    case 17:retID = R.drawable.image_snow17;break;
                    case 18:retID = R.drawable.image_snow18;break;
                    case 19:retID = R.drawable.image_snow19;break;
                    case 20:retID = R.drawable.image_snow20;break;
                    case 21:retID = R.drawable.image_snow21;break;
                    case 22:retID = R.drawable.image_snow22;break;
                    case 23:retID = R.drawable.image_snow23;break;
                    case 24:retID = R.drawable.image_snow24;break;
                    case 25:retID = R.drawable.image_snow25;break;
                    case 26:retID = R.drawable.image_snow26;break;
                    case 27:retID = R.drawable.image_snow27;break;
                    case 28:retID = R.drawable.image_snow28;break;
                    case 29:retID = R.drawable.image_snow29;break;
                    case 30:retID = R.drawable.image_snow30;break;
                    case 31:retID = R.drawable.image_snow31;break;
                    case 32:retID = R.drawable.image_snow32;break;
                    case 33:retID = R.drawable.image_snow33;break;
                    case 34:retID = R.drawable.image_snow34;break;
                    case 35:retID = R.drawable.image_snow35;break;
                    case 36:retID = R.drawable.image_snow36;break;
                    case 37:retID = R.drawable.image_snow37;break;
                    case 38:retID = R.drawable.image_snow38;break;
                    case 39:retID = R.drawable.image_snow39;break;
                    case 40:retID = R.drawable.image_snow40;break;
                    case 41:retID = R.drawable.image_snow41;break;
                    case 42:retID = R.drawable.image_snow42;break;
                    case 43:retID = R.drawable.image_snow43;break;
                    case 44:retID = R.drawable.image_snow44;break;
                    case 45:retID = R.drawable.image_snow45;break;
                    case 46:retID = R.drawable.image_snow46;break;
                    case 47:retID = R.drawable.image_snow47;break;
                    case 48:retID = R.drawable.image_snow48;break;
                    case 49:retID = R.drawable.image_snow49;break;
                    case 50:retID = R.drawable.image_snow50;break;
                    case 51:retID = R.drawable.image_snow51;break;
                    case 52:retID = R.drawable.image_snow52;break;
                    case 53:retID = R.drawable.image_snow53;break;
                    case 54:retID = R.drawable.image_snow54;break;
                    case 55:retID = R.drawable.image_snow55;break;
                    case 56:retID = R.drawable.image_snow56;break;
                    case 57:retID = R.drawable.image_snow57;break;
                    case 58:retID = R.drawable.image_snow58;break;
                    case 59:retID = R.drawable.image_snow59;break;
                    case 60:retID = R.drawable.image_snow60;break;
                    case 61:retID = R.drawable.image_snow61;break;
                    case 62:retID = R.drawable.image_snow62;break;
                    case 63:retID = R.drawable.image_snow63;break;
                    case 64:retID = R.drawable.image_snow64;break;
                    case 65:retID = R.drawable.image_snow65;break;
                    case 66:retID = R.drawable.image_snow66;break;
                    case 67:retID = R.drawable.image_snow67;break;
                    case 68:retID = R.drawable.image_snow68;break;
                    case 69:retID = R.drawable.image_snow69;break;
                    case 70:retID = R.drawable.image_snow70;break;
                    case 71:retID = R.drawable.image_snow71;break;
                    case 72:retID = R.drawable.image_snow72;break;
                    case 73:retID = R.drawable.image_snow73;break;
                    case 74:retID = R.drawable.image_snow74;break;
                    case 75:retID = R.drawable.image_snow75;break;
                    case 76:retID = R.drawable.image_snow76;break;
                    case 77:retID = R.drawable.image_snow77;break;
                    case 78:retID = R.drawable.image_snow78;break;
                    case 79:retID = R.drawable.image_snow79;break;
                    case 80:retID = R.drawable.image_snow80;break;
                    case 81:retID = R.drawable.image_snow81;break;
                    case 82:retID = R.drawable.image_snow82;break;
                    case 83:retID = R.drawable.image_snow83;break;
                    case 84:retID = R.drawable.image_snow84;break;
                }
                break;
            case PublicUse.AnimationBackHome:
                switch(index)
                {
                    case 0:retID = R.drawable.image_back00;break;
                    case 1:retID = R.drawable.image_back01;break;
                    case 2:retID = R.drawable.image_back02;break;
                    case 3:retID = R.drawable.image_back03;break;
                    case 4:retID = R.drawable.image_back04;break;
                    case 5:retID = R.drawable.image_back05;break;
                    case 6:retID = R.drawable.image_back06;break;
                    case 7:retID = R.drawable.image_back07;break;
                    case 8:retID = R.drawable.image_back08;break;
                    case 9:retID = R.drawable.image_back09;break;
                    case 10:retID = R.drawable.image_back10;break;
                    case 11:retID = R.drawable.image_back11;break;
                    case 12:retID = R.drawable.image_back12;break;
                    case 13:retID = R.drawable.image_back13;break;
                    case 14:retID = R.drawable.image_back14;break;
                    case 15:retID = R.drawable.image_back15;break;
                    case 16:retID = R.drawable.image_back16;break;
                    case 17:retID = R.drawable.image_back17;break;
                    case 18:retID = R.drawable.image_back18;break;
                    case 19:retID = R.drawable.image_back19;break;
                    case 20:retID = R.drawable.image_back20;break;
                    case 21:retID = R.drawable.image_back21;break;
                    case 22:retID = R.drawable.image_back22;break;
                    case 23:retID = R.drawable.image_back23;break;
                    case 24:retID = R.drawable.image_back24;break;
                    case 25:retID = R.drawable.image_back25;break;
                    case 26:retID = R.drawable.image_back26;break;
                    case 27:retID = R.drawable.image_back27;break;
                    case 28:retID = R.drawable.image_back28;break;
                    case 29:retID = R.drawable.image_back29;break;
                    case 30:retID = R.drawable.image_back30;break;
                    case 31:retID = R.drawable.image_back31;break;
                    case 32:retID = R.drawable.image_back32;break;
                    case 33:retID = R.drawable.image_back33;break;
                    case 34:retID = R.drawable.image_back34;break;
                    case 35:retID = R.drawable.image_back35;break;
                    case 36:retID = R.drawable.image_back36;break;
                    case 37:retID = R.drawable.image_back37;break;
                    case 38:retID = R.drawable.image_back38;break;
                    case 39:retID = R.drawable.image_back39;break;
                    case 40:retID = R.drawable.image_back40;break;
                    case 41:retID = R.drawable.image_back41;break;
                    case 42:retID = R.drawable.image_back42;break;
                    case 43:retID = R.drawable.image_back43;break;
                    case 44:retID = R.drawable.image_back44;break;
                    case 45:retID = R.drawable.image_back45;break;
                    case 46:retID = R.drawable.image_back46;break;
                    case 47:retID = R.drawable.image_back47;break;
                    case 48:retID = R.drawable.image_back48;break;
                    case 49:retID = R.drawable.image_back49;break;
                    case 50:retID = R.drawable.image_back50;break;
                    case 51:retID = R.drawable.image_back51;break;
                    case 52:retID = R.drawable.image_back52;break;
                    case 53:retID = R.drawable.image_back53;break;
                    case 54:retID = R.drawable.image_back54;break;
                    case 55:retID = R.drawable.image_back55;break;
                    case 56:retID = R.drawable.image_back56;break;
                }
                break;
            case PublicUse.AnimationLeaveHome:
                switch(index)
                {
                    case 0:retID = R.drawable.image_leave00;break;
                    case 1:retID = R.drawable.image_leave01;break;
                    case 2:retID = R.drawable.image_leave02;break;
                    case 3:retID = R.drawable.image_leave03;break;
                    case 4:retID = R.drawable.image_leave04;break;
                    case 5:retID = R.drawable.image_leave05;break;
                    case 6:retID = R.drawable.image_leave06;break;
                    case 7:retID = R.drawable.image_leave07;break;
                    case 8:retID = R.drawable.image_leave08;break;
                    case 9:retID = R.drawable.image_leave09;break;
                    case 10:retID = R.drawable.image_leave10;break;
                    case 11:retID = R.drawable.image_leave11;break;
                    case 12:retID = R.drawable.image_leave12;break;
                    case 13:retID = R.drawable.image_leave13;break;
                    case 14:retID = R.drawable.image_leave14;break;
                    case 15:retID = R.drawable.image_leave15;break;
                    case 16:retID = R.drawable.image_leave16;break;
                    case 17:retID = R.drawable.image_leave17;break;
                    case 18:retID = R.drawable.image_leave18;break;
                    case 19:retID = R.drawable.image_leave19;break;
                    case 20:retID = R.drawable.image_leave20;break;
                    case 21:retID = R.drawable.image_leave21;break;
                    case 22:retID = R.drawable.image_leave22;break;
                    case 23:retID = R.drawable.image_leave23;break;
                    case 24:retID = R.drawable.image_leave24;break;
                    case 25:retID = R.drawable.image_leave25;break;
                    case 26:retID = R.drawable.image_leave26;break;
                    case 27:retID = R.drawable.image_leave27;break;
                    case 28:retID = R.drawable.image_leave28;break;
                    case 29:retID = R.drawable.image_leave29;break;
                    case 30:retID = R.drawable.image_leave30;break;
                    case 31:retID = R.drawable.image_leave31;break;
                    case 32:retID = R.drawable.image_leave32;break;
                    case 33:retID = R.drawable.image_leave33;break;
                    case 34:retID = R.drawable.image_leave34;break;
                    case 35:retID = R.drawable.image_leave35;break;
                    case 36:retID = R.drawable.image_leave36;break;
                    case 37:retID = R.drawable.image_leave37;break;
                    case 38:retID = R.drawable.image_leave38;break;
                    case 39:retID = R.drawable.image_leave39;break;
                    case 40:retID = R.drawable.image_leave40;break;
                    case 41:retID = R.drawable.image_leave41;break;
                    case 42:retID = R.drawable.image_leave42;break;
                    case 43:retID = R.drawable.image_leave43;break;
                    case 44:retID = R.drawable.image_leave44;break;
                    case 45:retID = R.drawable.image_leave45;break;
                    case 46:retID = R.drawable.image_leave46;break;
                    case 47:retID = R.drawable.image_leave47;break;
                    case 48:retID = R.drawable.image_leave48;break;
                    case 49:retID = R.drawable.image_leave49;break;
                    case 50:retID = R.drawable.image_leave50;break;
                    case 51:retID = R.drawable.image_leave51;break;
                    case 52:retID = R.drawable.image_leave52;break;
                    case 53:retID = R.drawable.image_leave53;break;
                    case 54:retID = R.drawable.image_leave54;break;
                    case 55:retID = R.drawable.image_leave55;break;
                    case 56:retID = R.drawable.image_leave56;break;
                    case 57:retID = R.drawable.image_leave57;break;
                    case 58:retID = R.drawable.image_leave58;break;
                    case 59:retID = R.drawable.image_leave59;break;
                    case 60:retID = R.drawable.image_leave60;break;
                    case 61:retID = R.drawable.image_leave61;break;
                    case 62:retID = R.drawable.image_leave62;break;
                    case 63:retID = R.drawable.image_leave63;break;
                    case 64:retID = R.drawable.image_leave64;break;
                    case 65:retID = R.drawable.image_leave65;break;
                    case 66:retID = R.drawable.image_leave66;break;
                    case 67:retID = R.drawable.image_leave67;break;
                    case 68:retID = R.drawable.image_leave68;break;
                    case 69:retID = R.drawable.image_leave69;break;
                }
                break;
            case PublicUse.AnimationSleep:
                switch(index)
                {
                    case 0:retID = R.drawable.image_sleep00;break;
                    case 1:retID = R.drawable.image_sleep01;break;
                    case 2:retID = R.drawable.image_sleep02;break;
                    case 3:retID = R.drawable.image_sleep03;break;
                    case 4:retID = R.drawable.image_sleep04;break;
                    case 5:retID = R.drawable.image_sleep05;break;
                    case 6:retID = R.drawable.image_sleep06;break;
                    case 7:retID = R.drawable.image_sleep07;break;
                    case 8:retID = R.drawable.image_sleep08;break;
                    case 9:retID = R.drawable.image_sleep09;break;
                    case 10:retID = R.drawable.image_sleep10;break;
                    case 11:retID = R.drawable.image_sleep11;break;
                    case 12:retID = R.drawable.image_sleep12;break;
                    case 13:retID = R.drawable.image_sleep13;break;
                    case 14:retID = R.drawable.image_sleep14;break;
                    case 15:retID = R.drawable.image_sleep15;break;
                    case 16:retID = R.drawable.image_sleep16;break;
                    case 17:retID = R.drawable.image_sleep17;break;
                    case 18:retID = R.drawable.image_sleep18;break;
                    case 19:retID = R.drawable.image_sleep19;break;
                    case 20:retID = R.drawable.image_sleep20;break;
                    case 21:retID = R.drawable.image_sleep21;break;
                    case 22:retID = R.drawable.image_sleep22;break;
                    case 23:retID = R.drawable.image_sleep23;break;
                    case 24:retID = R.drawable.image_sleep24;break;
                    case 25:retID = R.drawable.image_sleep25;break;
                    case 26:retID = R.drawable.image_sleep26;break;
                    case 27:retID = R.drawable.image_sleep27;break;
                    case 28:retID = R.drawable.image_sleep28;break;
                    case 29:retID = R.drawable.image_sleep29;break;
                    case 30:retID = R.drawable.image_sleep30;break;
                    case 31:retID = R.drawable.image_sleep31;break;
                    case 32:retID = R.drawable.image_sleep32;break;
                    case 33:retID = R.drawable.image_sleep33;break;
                    case 34:retID = R.drawable.image_sleep34;break;
                    case 35:retID = R.drawable.image_sleep35;break;
                    case 36:retID = R.drawable.image_sleep36;break;
                    case 37:retID = R.drawable.image_sleep37;break;
                    case 38:retID = R.drawable.image_sleep38;break;
                    case 39:retID = R.drawable.image_sleep39;break;
                    case 40:retID = R.drawable.image_sleep40;break;
                    case 41:retID = R.drawable.image_sleep41;break;
                    case 42:retID = R.drawable.image_sleep42;break;
                    case 43:retID = R.drawable.image_sleep43;break;
                    case 44:retID = R.drawable.image_sleep44;break;
                    case 45:retID = R.drawable.image_sleep45;break;
                    case 46:retID = R.drawable.image_sleep46;break;
                    case 47:retID = R.drawable.image_sleep47;break;
                    case 48:retID = R.drawable.image_sleep48;break;
                    case 49:retID = R.drawable.image_sleep49;break;
                    case 50:retID = R.drawable.image_sleep50;break;
                    case 51:retID = R.drawable.image_sleep51;break;
                    case 52:retID = R.drawable.image_sleep52;break;
                    case 53:retID = R.drawable.image_sleep53;break;
                    case 54:retID = R.drawable.image_sleep54;break;
                    case 55:retID = R.drawable.image_sleep55;break;
                    case 56:retID = R.drawable.image_sleep56;break;
                    case 57:retID = R.drawable.image_sleep57;break;
                    case 58:retID = R.drawable.image_sleep58;break;
                    case 59:retID = R.drawable.image_sleep59;break;
                    case 60:retID = R.drawable.image_sleep60;break;
                    case 61:retID = R.drawable.image_sleep61;break;
                    case 62:retID = R.drawable.image_sleep62;break;
                    case 63:retID = R.drawable.image_sleep63;break;
                    case 64:retID = R.drawable.image_sleep64;break;
                    case 65:retID = R.drawable.image_sleep65;break;
                    case 66:retID = R.drawable.image_sleep66;break;
                    case 67:retID = R.drawable.image_sleep67;break;
                    case 68:retID = R.drawable.image_sleep68;break;
                    case 69:retID = R.drawable.image_sleep69;break;
                    case 70:retID = R.drawable.image_sleep70;break;
                    case 71:retID = R.drawable.image_sleep71;break;
                    case 72:retID = R.drawable.image_sleep72;break;
                    case 73:retID = R.drawable.image_sleep73;break;
                    case 74:retID = R.drawable.image_sleep74;break;
                    case 75:retID = R.drawable.image_sleep75;break;
                    case 76:retID = R.drawable.image_sleep76;break;
                    case 77:retID = R.drawable.image_sleep77;break;
                    case 78:retID = R.drawable.image_sleep78;break;
                    case 79:retID = R.drawable.image_sleep79;break;
                    case 80:retID = R.drawable.image_sleep80;break;
                    case 81:retID = R.drawable.image_sleep81;break;
                    case 82:retID = R.drawable.image_sleep82;break;
                    case 83:retID = R.drawable.image_sleep83;break;
                    case 84:retID = R.drawable.image_sleep84;break;
                    case 85:retID = R.drawable.image_sleep85;break;
                    case 86:retID = R.drawable.image_sleep86;break;
                    case 87:retID = R.drawable.image_sleep87;break;
                    case 88:retID = R.drawable.image_sleep88;break;
                    case 89:retID = R.drawable.image_sleep89;break;
                    case 90:retID = R.drawable.image_sleep90;break;
                    case 91:retID = R.drawable.image_sleep91;break;
                    case 92:retID = R.drawable.image_sleep92;break;
                    case 93:retID = R.drawable.image_sleep93;break;
                    case 94:retID = R.drawable.image_sleep94;break;
                    case 95:retID = R.drawable.image_sleep95;break;
                    case 96:retID = R.drawable.image_sleep96;break;
                    case 97:retID = R.drawable.image_sleep97;break;
                }
                break;
        }
        return retID;
    }

    void onDestroy()
    {
        Log.i(PublicUse.Tag, "mFinalClass bitmap destroy! ");
        for(int i = 0; i < 333; i++)//释放所有内存
        {
            if((tempBitmapBuff[i] != null) && (!tempBitmapBuff[i].isRecycled()))
            {
                tempBitmapBuff[i].recycle();
            }
            tempBitmapBuff[i] = null;
        }
    }
}
