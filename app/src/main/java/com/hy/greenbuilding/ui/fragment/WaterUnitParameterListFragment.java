package com.hy.greenbuilding.ui.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.hy.greenbuilding.modbus.WaterUnitSystemStatusDecoder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        WaterUnitRegisterMap.Register register = WaterUnitRegisterMap.resolve(code);
        Integer rawValue = register == null ? null
                : ModbusRegisterValueStore.getInstance().get(register.address);
        List<WriteOption> writeOptions = parseWriteOptions(name, detail);
        NumericRange numericRange = parseNumericRange(detail);
        boolean hasOptionControl = !statusItem && register != null && register.writable
                && !writeOptions.isEmpty();
        boolean canWrite = !statusItem && register != null && register.writable
                && ModbusRtuManager.getInstance().isCommunicationEnabled();
        TextView valueView = hasOptionControl ? new Button(requireContext())
                : new TextView(requireContext());
        valueView.setText(formatValue(rawValue, statusItem, code, name, writeOptions, numericRange));
        valueView.setTextColor(Color.parseColor("#A8907C"));
        valueView.setTextSize(28);
        valueView.setGravity(Gravity.CENTER);
        row.addView(valueView, new LinearLayout.LayoutParams(dp(130),
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (canWrite) {
            row.setClickable(true);
            if (hasOptionControl) {
                View.OnClickListener listener = v -> showOptionWriteDialog(register, name, writeOptions);
                row.setOnClickListener(listener);
                valueView.setOnClickListener(listener);
            } else {
                row.setOnClickListener(v -> showNumericWriteDialog(register, name, rawValue, numericRange));
            }
        } else if (!statusItem && register != null && register.writable) {
            row.setEnabled(false);
            valueView.setEnabled(false);
        }

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(16));
        container.addView(row, rowParams);
    }

    private String formatValue(Integer rawValue, boolean statusItem, String code, String name,
                               List<WriteOption> writeOptions, NumericRange numericRange) {
        if (rawValue == null) {
            return statusItem || writeOptions.isEmpty() ? "--" : "请选择";
        }
        if ("A01".equals(code)) {
            return WaterUnitSystemStatusDecoder.decode(rawValue);
        }
        if (statusItem && isBinaryOutput(code)) {
            if (rawValue == 0) {
                return "关";
            }
            if (rawValue == 1) {
                return "开";
            }
        }
        if (statusItem && name.contains("开关")) {
            return rawValue == 0 ? "关闭" : "开启";
        }
        for (WriteOption option : writeOptions) {
            if (option.value == rawValue) {
                return option.label;
            }
        }
        return formatEngineeringValue(toDisplayValue(rawValue, numericRange, code, name), code, name);
    }

    private boolean isBinaryOutput(String code) {
        if (code == null || !code.startsWith("Y")) {
            return false;
        }
        try {
            int number = Integer.parseInt(code.substring(1));
            return number >= 0 && number <= 12;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /** Explicit catalog options use labels; range values remain numeric input. */
    private List<WriteOption> parseWriteOptions(String name, String detail) {
        List<WriteOption> options = new ArrayList<>();
        if (!detail.contains("/")) {
            return options;
        }
        String[] optionTexts = detail.split("\\s*/\\s*");
        for (String optionText : optionTexts) {
            int separator = optionText.indexOf('-');
            if (separator <= 0 || separator == optionText.length() - 1) {
                return new ArrayList<>();
            }
            int value;
            try {
                value = Integer.parseInt(optionText.substring(0, separator).trim());
            } catch (NumberFormatException exception) {
                return new ArrayList<>();
            }
            String label = optionText.substring(separator + 1).trim();
            if (name.contains("是否") && (value == 0 || value == 1)) {
                label = value == 0 ? "否" : "是";
            }
            options.add(new WriteOption(value, label));
        }
        return options;
    }

    private NumericRange parseNumericRange(String detail) {
        final String prefix = "范围：";
        int prefixIndex = detail.indexOf(prefix);
        if (prefixIndex < 0) {
            return null;
        }
        String rangeText = detail.substring(prefixIndex + prefix.length());
        int separator = rangeText.indexOf('～');
        if (separator <= 0 || separator == rangeText.length() - 1) {
            return null;
        }
        try {
            int minimum = Integer.parseInt(rangeText.substring(0, separator).trim());
            int maximumEnd = separator + 1;
            while (maximumEnd < rangeText.length()
                    && Character.isDigit(rangeText.charAt(maximumEnd))) {
                maximumEnd++;
            }
            if (maximumEnd == separator + 1) {
                return null;
            }
            int maximum = Integer.parseInt(rangeText.substring(separator + 1, maximumEnd));
            return new NumericRange(minimum, maximum, rangeText);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void showOptionWriteDialog(WaterUnitRegisterMap.Register register, String name,
                                       List<WriteOption> options) {
        if (!isCommunicationEnabled()) {
            return;
        }
        String[] labels = new String[options.size()];
        for (int index = 0; index < options.size(); index++) {
            labels[index] = options.get(index).label;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(name)
                .setItems(labels, (dialog, which) -> {
                    WriteOption option = options.get(which);
                    writeRegister(register, option.value);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** Numeric engineering values retain direct input. */
    private void showNumericWriteDialog(WaterUnitRegisterMap.Register register, String name,
                                        Integer currentValue, NumericRange numericRange) {
        if (!isCommunicationEnabled()) {
            return;
        }

        EditText input = new EditText(requireContext());
        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
        if (usesTenths(register.code, name)) {
            inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }
        input.setInputType(inputType);
        input.setSingleLine(true);
        if (currentValue != null) {
            input.setText(formatEngineeringValue(toDisplayValue(
                    currentValue, numericRange, register.code, name), register.code, name));
            input.setSelection(input.length());
        }
        int padding = dp(24);
        input.setPadding(padding, dp(8), padding, dp(8));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(name)
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("设置", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> writeNumericValue(dialog, input, register, name, numericRange)));
        dialog.show();
    }

    private boolean isCommunicationEnabled() {
        return ModbusRtuManager.getInstance().isCommunicationEnabled();
    }

    private void writeNumericValue(AlertDialog inputDialog, EditText input,
                                   WaterUnitRegisterMap.Register register, String name,
                                   NumericRange numericRange) {
        String text = input.getText().toString().trim();
        boolean useTenths = usesTenths(register.code, name);
        BigDecimal engineeringValue;
        int rawValue;
        try {
            engineeringValue = new BigDecimal(text);
            rawValue = useTenths ? engineeringValue.movePointRight(1).intValueExact()
                    : engineeringValue.intValueExact();
        } catch (NumberFormatException exception) {
            input.setError(useTenths ? "请输入最多一位小数" : "请输入整数");
            return;
        } catch (ArithmeticException exception) {
            input.setError(useTenths ? "请输入最多一位小数" : "请输入整数");
            return;
        }
        if (numericRange != null
                && (engineeringValue.compareTo(BigDecimal.valueOf(numericRange.minimum)) < 0
                || engineeringValue.compareTo(BigDecimal.valueOf(numericRange.maximum)) > 0)) {
            input.setError("允许范围：" + numericRange.text);
            return;
        }
        if (numericRange == null && (rawValue < 0 || rawValue > 0xFFFF)) {
            input.setError("数值超出寄存器可写范围");
            return;
        }
        inputDialog.dismiss();
        writeRegister(register, numericRange != null && numericRange.minimum < 0
                ? rawValue & 0xFFFF : rawValue);
    }

    private int toDisplayValue(int rawValue, NumericRange numericRange, String code, String name) {
        boolean signedValue = (numericRange != null && numericRange.minimum < 0)
                || usesTenths(code, name);
        return signedValue && rawValue > 0x7FFF
                ? rawValue - 0x10000 : rawValue;
    }

    private String formatEngineeringValue(int value, String code, String name) {
        if (!usesTenths(code, name)) {
            return String.valueOf(value);
        }
        if (isPressureValue(code, name)) {
            return String.format(Locale.US, "%.1f", value / 10.0d);
        }
        if (value % 10 == 0) {
            return String.valueOf(value / 10);
        }
        return String.format(Locale.US, "%.1f", value / 10.0d);
    }

    /** Temperature, superheat, and pressure values use a 0.1-unit register scale. */
    private boolean usesTenths(String code, String name) {
        return name.contains("温") || name.contains("过热度") || isPressureValue(code, name);
    }

    private boolean isPressureValue(String code, String name) {
        if ("P152".equals(code) || "P153".equals(code)
                || "P154".equals(code) || "P155".equals(code)) {
            return true;
        }
        return name.contains("压力") && !name.contains("传感器");
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
                        // The setting remains unchanged until a later successful read refreshes it.
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
                // Do not interrupt local configuration when the outdoor unit is absent.
            }
        });
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }

    private static final class WriteOption {
        final int value;
        final String label;

        WriteOption(int value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    private static final class NumericRange {
        final int minimum;
        final int maximum;
        final String text;

        NumericRange(int minimum, int maximum, String text) {
            this.minimum = minimum;
            this.maximum = maximum;
            this.text = text;
        }
    }
}
