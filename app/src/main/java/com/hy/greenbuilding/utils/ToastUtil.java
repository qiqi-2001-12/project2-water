package com.hy.greenbuilding.utils;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {
    private static Toast mToast;
    public static void showToast(Context context,String text){
        if(mToast == null){
            mToast=Toast.makeText(context,text,Toast.LENGTH_SHORT);
        }else{
            mToast.setText(text);
        }
        mToast.show();
        LinearLayout linearLayout = (LinearLayout) mToast.getView();
        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
        messageTextView.setTextSize(26);
    }
}
