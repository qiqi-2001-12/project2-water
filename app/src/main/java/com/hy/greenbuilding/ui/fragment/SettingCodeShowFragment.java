package com.hy.greenbuilding.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.utils.PackageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.hy.greenbuilding.utils.PackageUtil.getMAC;

public class SettingCodeShowFragment extends BaseDialogFragment {

    @BindView(R.id.iv_qrCode)
    ImageView mQrCode;
    @BindView(R.id.tv_mac)
    TextView tvMac;

    private View rootView;
    private Unbinder unbinder;
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.tv_wifi)
    LinearLayout tvWifi;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_code_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Bitmap bitmap = PackageUtil.createQRCodeBitmap(200, 200, "H", "1", Color.BLACK, Color.WHITE);
        mQrCode.setImageBitmap(bitmap);
        String mac = getMAC();
        if (!TextUtils.isEmpty(mac)) {
            tvMac.setVisibility(View.VISIBLE);
            tvMac.setText("MAC：" + mac);
        } else {
            tvMac.setVisibility(View.GONE);
        }
        return rootView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @OnClick({R.id.tv_wifi})
    public void onWifiClick(View view) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        // 如果是从非 Activity 上下文启动，可能需要 FLAG_ACTIVITY_NEW_TASK 标志
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
