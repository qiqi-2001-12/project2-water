package com.hy.greenbuilding.ui.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.modbus.ModbusRegisterValueStore;
import com.hy.greenbuilding.modbus.ModbusRequest;
import com.hy.greenbuilding.modbus.ModbusResponse;
import com.hy.greenbuilding.modbus.ModbusRtuManager;
import com.hy.greenbuilding.modbus.WaterUnitRegisterMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/** 水机状态/设置参数的只读列表。 */
public class WaterUnitParameterListFragment extends Fragment {
    private static final String ARG_STATUS_PAGE = "status_page";
    private static final String ARG_SECTION_INDEX = "section_index";
    private LinearLayout parameterContainer;
    private WaterUnitParameterCatalog.Section section;
    private String keyword = "";

    public static WaterUnitParameterListFragment newInstance(boolean statusPage, int sectionIndex) {
        WaterUnitParameterListFragment fragment = new WaterUnitParameterListFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_STATUS_PAGE, statusPage);
        arguments.putInt(ARG_SECTION_INDEX, sectionIndex);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.water_unit_parameter_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean statusPage = getArguments() == null || getArguments().getBoolean(ARG_STATUS_PAGE, true);
        int sectionIndex = getArguments() == null ? 0 : getArguments().getInt(ARG_SECTION_INDEX, 0);
        section = WaterUnitParameterCatalog.getPageSections(statusPage).get(sectionIndex);
        parameterContainer = view.findViewById(R.id.water_parameter_container);
        render();
    }

    void filter(String query) {
        keyword = query == null ? "" : query.trim().toLowerCase();
        if (parameterContainer != null) {
            render();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onModbusRegistersUpdated(ModbusRegisterValueStore.ModbusRegistersUpdatedEvent event) {
        if (parameterContainer != null) {
            render();
        }
    }

    private void render() {
        parameterContainer.removeAllViews();
        addSection(parameterContainer, section);
    }

    private void addSection(LinearLayout container, WaterUnitParameterCatalog.Section section) {
        int matchCount = 0;
        for (String item : section.items) {
            if (matchesKeyword(item)) {
                addParameterRow(container, item, section.statusItem);
                matchCount++;
            }
        }
        if (matchCount == 0) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("未找到匹配参数");
            emptyView.setTextColor(Color.parseColor("#8A786B"));
            emptyView.setTextSize(24);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, dp(48), 0, dp(48));
            container.addView(emptyView, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    /** 地址保留在后台目录中，但不作为管理员 UI 的搜索条件。 */
    private boolean matchesKeyword(String item) {
        if (keyword.isEmpty()) {
            return true;
        }
        String[] columns = item.split("\\|", -1);
        return columns[0].toLowerCase().contains(keyword)
                || columns[2].toLowerCase().contains(keyword)
                || (columns.length > 3 && columns[3].toLowerCase().contains(keyword));
    }

    private void addParameterRow(LinearLayout container, String item, boolean statusItem) {
        String[] columns = item.split("\\|", -1);
        String code = columns[0];
        String name = columns[2];
        String detail = columns.length > 3 ? columns[3] : "";

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(16), dp(20), dp(16));
        row.setBackgroundResource(R.drawable.water_parameter_card);

        TextView codeView = new TextView(requireContext());
        // Modbus 数值地址仅保留在参数目录中供后台读取与绑定，管理员 UI 不展示。
        codeView.setText(code);
        codeView.setTextColor(Color.parseColor("#6E5540"));
        codeView.setTextSize(22);
        codeView.setGravity(Gravity.CENTER);
        codeView.setBackgroundResource(R.drawable.water_parameter_code_bg);
        row.addView(codeView, new LinearLayout.LayoutParams(dp(120), dp(76)));

        LinearLayout textContainer = new LinearLayout(requireContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setPadding(dp(20), 0, dp(16), 0);

        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#29221D"));
        nameView.setTextSize(26);
        nameView.setSingleLine(false);
        textContainer.addView(nameView);

        if (!detail.isEmpty()) {
            TextView detailView = new TextView(requireContext());
            detailView.setText(detail);
            detailView.setTextColor(Color.parseColor("#8A786B"));
            detailView.setTextSize(19);
            detailView.setPadding(0, dp(8), 0, 0);
            textContainer.addView(detailView);
        }

        row.addView(textContainer, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView valueView = new TextView(requireContext());
        WaterUnitRegisterMap.Register register = WaterUnitRegisterMap.resolve(code);
        Integer rawValue = register == null ? null
                : ModbusRegisterValueStore.getInstance().get(register.address);
        valueView.setText(rawValue == null ? (statusItem ? "--" : "待接入")
                : String.valueOf(rawValue));
        valueView.setTextColor(Color.parseColor("#A8907C"));
        valueView.setTextSize(20);
        valueView.setGravity(Gravity.CENTER);
        row.addView(valueView, new LinearLayout.LayoutParams(dp(100),
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (!statusItem && register != null && register.writable) {
            row.setClickable(true);
            row.setOnClickListener(v -> showWriteDialog(register, name, rawValue));
        }

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(16));
        container.addView(row, rowParams);
    }

    /** P 参数采用单寄存器原始值写入；倍率和工程单位在实体联调确认后统一补充。 */
    private void showWriteDialog(WaterUnitRegisterMap.Register register, String name, Integer currentValue) {
        if (!ModbusRtuManager.getInstance().isCommunicationEnabled()) {
            Toast.makeText(requireContext(), "水机实体通讯未启用", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true);
        if (currentValue != null) {
            input.setText(String.valueOf(currentValue));
            input.setSelection(input.length());
        }
        int padding = dp(24);
        input.setPadding(padding, dp(8), padding, dp(8));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(name)
                .setMessage("请输入 0 - 65535 的 Modbus 原始值")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("下一步", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> confirmWrite(dialog, input, register, name)));
        dialog.show();
    }

    private void confirmWrite(AlertDialog inputDialog, EditText input,
                              WaterUnitRegisterMap.Register register, String name) {
        String text = input.getText().toString().trim();
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            input.setError("请输入整数");
            return;
        }
        if (value < 0 || value > 0xFFFF) {
            input.setError("允许范围为 0 - 65535");
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("确认写入")
                .setMessage("确认将“" + name + "”写为原始值 " + value + "？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认写入", (dialog, which) -> {
                    inputDialog.dismiss();
                    writeRegister(register, value);
                })
                .show();
    }

    private void writeRegister(WaterUnitRegisterMap.Register register, int value) {
        ModbusRtuManager.getInstance().submit(ModbusRequest.writeSingle(register.address, value),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        verifyWrittenValue(register);
                    }

                    @Override
                    public void onFailure(String message) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                                "写入失败：" + message, Toast.LENGTH_LONG).show());
                    }
                });
    }

    /** 06H 成功响应后立即用 03H 回读同一寄存器，避免只依据写响应显示成功。 */
    private void verifyWrittenValue(WaterUnitRegisterMap.Register register) {
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                register.readFunction, register.address, 1), new ModbusRtuManager.Callback() {
            @Override
            public void onSuccess(ModbusResponse response) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "写入并回读成功", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String message) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "写入已响应，但回读失败：" + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }
}
