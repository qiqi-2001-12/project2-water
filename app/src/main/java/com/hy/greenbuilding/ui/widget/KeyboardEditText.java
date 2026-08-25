package com.hy.greenbuilding.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardEditText extends androidx.appcompat.widget.AppCompatEditText {

    OnFocusChangeListener mFocusChangeListener;

    public KeyboardEditText(Context context) {
        super(context);
        init();
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        this.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    openKeyboard(v.getContext(), KeyboardEditText.this);
//                }
//            }
//        });
    }

    public static void openKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFocusChangeListener = null;
    }
}
