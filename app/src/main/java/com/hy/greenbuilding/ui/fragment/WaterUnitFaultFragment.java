package com.hy.greenbuilding.ui.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.modbus.ModbusRegisterValueStore;
import com.hy.greenbuilding.modbus.WaterUnitFaultDecoder;
import com.hy.greenbuilding.modbus.WaterUnitFaultHistoryStore;
import com.hy.greenbuilding.modbus.WaterUnitRegisterMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Displays the current non-zero fault information registers. */
public class WaterUnitFaultFragment extends Fragment {
    private final SimpleDateFormat faultTimeFormat =
            new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);

    private LinearLayout faultList;
    private TextView emptyView;
    private Button historyButton;
    private boolean showingHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.water_unit_fault, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        faultList = view.findViewById(R.id.water_fault_list);
        emptyView = view.findViewById(R.id.water_fault_empty);
        historyButton = view.findViewById(R.id.water_fault_history);
        Button resetButton = view.findViewById(R.id.water_fault_reset);

        historyButton.setOnClickListener(ignored -> {
            showingHistory = !showingHistory;
            renderMode();
        });
        resetButton.setOnClickListener(ignored -> confirmReset());
        renderMode();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        renderMode();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onModbusRegistersUpdated(ModbusRegisterValueStore.ModbusRegistersUpdatedEvent event) {
        if (overlaps(event.startAddress, event.quantity,
                WaterUnitRegisterMap.STATUS_SYSTEM_AND_FAULTS)) {
            renderMode();
        }
    }

    private void renderMode() {
        if (showingHistory) {
            historyButton.setText("查看当前故障");
            renderHistoryFaults();
        } else {
            historyButton.setText("历史故障");
            renderCurrentFaults();
        }
    }

    private void renderCurrentFaults() {
        Map<Integer, Integer> values = ModbusRegisterValueStore.getInstance().snapshot();
        long now = System.currentTimeMillis();
        List<WaterUnitFaultDecoder.Fault> faults = WaterUnitFaultDecoder.decode(values);
        faultList.removeAllViews();
        faultList.setVisibility(View.VISIBLE);

        for (WaterUnitFaultDecoder.Fault fault : faults) {
            Long firstSeenAt = WaterUnitFaultHistoryStore.getInstance()
                    .getActiveStartedAt(fault.key());
            if (firstSeenAt == null) {
                firstSeenAt = now;
            }
            addFaultRow(fault.name, firstSeenAt);
        }

        emptyView.setVisibility(faults.isEmpty() ? View.VISIBLE : View.GONE);
        if (faults.isEmpty()) {
            emptyView.setText("暂无当前故障");
        }
    }

    private void renderHistoryFaults() {
        List<WaterUnitFaultHistoryStore.Entry> history =
                WaterUnitFaultHistoryStore.getInstance().getHistory();
        faultList.removeAllViews();
        faultList.setVisibility(View.VISIBLE);
        for (WaterUnitFaultHistoryStore.Entry entry : history) {
            addFaultRow(entry.name, entry.occurredAt);
        }
        emptyView.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
        if (history.isEmpty()) {
            emptyView.setText("暂无历史故障记录");
        }
    }

    private void addFaultRow(String faultName, long firstSeenAt) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(24), dp(16), dp(24), dp(16));

        TextView nameView = new TextView(requireContext());
        nameView.setText(faultName);
        nameView.setTextColor(Color.parseColor("#3C3027"));
        nameView.setTextSize(20);
        row.addView(nameView, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView timeView = new TextView(requireContext());
        timeView.setText(faultTimeFormat.format(new Date(firstSeenAt)));
        timeView.setTextColor(Color.parseColor("#6E5540"));
        timeView.setTextSize(18);
        timeView.setGravity(android.view.Gravity.END);
        row.addView(timeView, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        faultList.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static boolean overlaps(int startAddress, int quantity,
                                    WaterUnitRegisterMap.RegisterBlock block) {
        int endAddress = startAddress + quantity - 1;
        int blockEndAddress = block.startAddress + block.quantity - 1;
        return startAddress <= blockEndAddress && endAddress >= block.startAddress;
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }

    private void confirmReset() {
        new AlertDialog.Builder(requireContext())
                .setTitle("故障复位")
                .setMessage("将向外机发送故障复位指令。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认复位", (dialog, which) -> Toast.makeText(requireContext(),
                        "未配置故障复位寄存器，未发送复位指令", Toast.LENGTH_SHORT).show())
                .show();
    }

}
