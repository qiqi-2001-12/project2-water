package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveAddress;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.model.City;
import com.hy.greenbuilding.model.Province;
import com.hy.greenbuilding.ui.activity.BaseActivity;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingLocationFrament extends BaseDialogFragment implements BaseActivity.KeyboardVisibilityListener  {
    @BindView(R.id.et_address)
    EditText mAddressView;
    @BindView(R.id.sp_province)
    Spinner mProvinceSpinner;
    @BindView(R.id.sp_city)
    Spinner mCitySpinner;
    @BindView(R.id.li_back)
    ImageView mReturnView;

    private View rootView;
    private Unbinder unbinder;
    private Province province = null;
    private List<Province> provinceList = new ArrayList<>();
    ArrayAdapter<Province> arrayAdapter1;
    ArrayAdapter<City> arrayAdapter2;
    private int mProvinceId = 0;
    private int mCityId = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_address_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initAddress();
        return rootView;
    }

    private void initAddress() {
        SaveAddress address = MySpUtil.getAddress(this.getActivity());
        if (address != null) {
            mProvinceId = address.getProvinceId();
            mCityId = address.getCityId();
            mAddressView.setText(address.getAddressDetail());
        }
        provinceList = StringUtils.parser(this.getActivity());
        arrayAdapter1 = new ArrayAdapter<Province>(this.getActivity(), R.layout.city_spinner, provinceList) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.city_spinner_item, parent, false);
                }
                TextView spinnerText = (TextView) convertView.findViewById(R.id.spinner_textView);
                spinnerText.setText(getItem(position).getName());
                return convertView;
            }
        };
        arrayAdapter2 = new ArrayAdapter<City>(this.getActivity(), R.layout.city_spinner, provinceList.get(mProvinceId).getCitys()) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.city_spinner_item, parent, false);
                }
                TextView spinnerText = convertView.findViewById(R.id.spinner_textView);
                spinnerText.setText(getItem(position).getName());
                return convertView;
            }
        };
        mProvinceSpinner.setAdapter(arrayAdapter1);
        mProvinceSpinner.setSelection(mProvinceId, true);
        mCitySpinner.setAdapter(arrayAdapter2);
        mCitySpinner.setSelection(mCityId, true);
        mProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                province = provinceList.get(position);
                Log.i("SpinnerDebug", "方法：onItemSelected - 选中位置: " + position + ", 选中的省份: " + provinceList.get(position).getName());
                arrayAdapter2 = new ArrayAdapter<City>(getActivity(), R.layout.city_spinner, provinceList.get(position).getCitys()) {
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getActivity().getLayoutInflater().inflate(R.layout.city_spinner_item, parent, false);
                        }
                        TextView spinnerText = convertView.findViewById(R.id.spinner_textView);
                        spinnerText.setText(getItem(position).getName());
                        return convertView;
                    }
                };
                mCitySpinner.setAdapter(arrayAdapter2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 在 City Spinner 选中项改变时，更新并保存选中的 City/区 ID
                mCityId = position;
                onAddressClick();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onAddressClick() {
        String address = mAddressView.getText().toString().trim();
        mAddressView.clearFocus();
        if (StringUtils.isNullOrEmpty(address)) {
            ToastUtil.showToast(getActivity(), "请输入详细地址!");
        } else {
            SaveAddress saveAddress = MySpUtil.getAddress(getActivity());
            saveAddress.setProvinceName(mProvinceSpinner.getSelectedItem().toString());
            saveAddress.setProvinceId(mProvinceSpinner.getSelectedItemPosition());
            saveAddress.setCityId(mCitySpinner.getSelectedItemPosition());
            saveAddress.setCityName(mCitySpinner.getSelectedItem().toString());
            saveAddress.setAddressDetail(address);
            MySpUtil.setParam(getActivity(), MySpUtil.ADDRESS_NAME, new Gson().toJson(saveAddress));
            ToastUtil.showToast(getActivity(), getString(R.string.set_success));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 注册键盘监听器
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).setKeyboardVisibilityListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 清理监听器
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setKeyboardVisibilityListener(null);
        }
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        // 【键盘状态变化回调】
        if (isVisible) {
            Log.d("Keyboard", "软键盘已显示");
        } else {
            Log.d("Keyboard", "软键盘已隐藏");
            onAddressClick();
        }
    }
}
