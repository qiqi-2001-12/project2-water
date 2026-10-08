package com.hy.greenbuilding.modbus;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Persists every detected water-unit fault occurrence across app restarts. */
public final class WaterUnitFaultHistoryStore {
    private static final String PREFERENCES_NAME = "water_unit_fault_history";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_ACTIVE_KEYS = "active_keys";
    private static final WaterUnitFaultHistoryStore INSTANCE =
            new WaterUnitFaultHistoryStore();

    private final Gson gson = new Gson();
    private final List<Entry> history = new ArrayList<>();
    private final Set<String> activeKeys = new HashSet<>();
    private SharedPreferences preferences;

    private WaterUnitFaultHistoryStore() {
    }

    public static WaterUnitFaultHistoryStore getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(Context context) {
        if (preferences != null) {
            return;
        }
        preferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);
        String historyJson = preferences.getString(KEY_HISTORY, "");
        if (historyJson != null && !historyJson.isEmpty()) {
            try {
                Entry[] savedEntries = gson.fromJson(historyJson, Entry[].class);
                if (savedEntries != null) {
                    Collections.addAll(history, savedEntries);
                }
            } catch (RuntimeException ignored) {
                history.clear();
            }
        }
        Set<String> savedActiveKeys = preferences.getStringSet(KEY_ACTIVE_KEYS, null);
        if (savedActiveKeys != null) {
            activeKeys.addAll(savedActiveKeys);
        }
    }

    /** Adds one history row only when a fault changes from inactive to active. */
    public synchronized void update(Map<Integer, Integer> registerValues) {
        if (preferences == null) {
            return;
        }
        List<WaterUnitFaultDecoder.Fault> currentFaults =
                WaterUnitFaultDecoder.decode(registerValues);
        Set<String> nextActiveKeys = new HashSet<>();
        long detectedAt = System.currentTimeMillis();
        boolean historyChanged = false;
        for (WaterUnitFaultDecoder.Fault fault : currentFaults) {
            String key = fault.key();
            nextActiveKeys.add(key);
            if (!activeKeys.contains(key)) {
                history.add(new Entry(key, fault.name, detectedAt));
                historyChanged = true;
            }
        }

        boolean activeStateChanged = !activeKeys.equals(nextActiveKeys);
        if (!historyChanged && !activeStateChanged) {
            return;
        }
        activeKeys.clear();
        activeKeys.addAll(nextActiveKeys);
        SharedPreferences.Editor editor = preferences.edit()
                .putStringSet(KEY_ACTIVE_KEYS, new HashSet<>(activeKeys));
        if (historyChanged) {
            editor.putString(KEY_HISTORY, gson.toJson(history));
        }
        editor.apply();
    }

    /** Returns newest occurrences first. */
    public synchronized List<Entry> getHistory() {
        List<Entry> result = new ArrayList<>(history);
        Collections.reverse(result);
        return result;
    }

    public synchronized Long getActiveStartedAt(String key) {
        if (!activeKeys.contains(key)) {
            return null;
        }
        for (int index = history.size() - 1; index >= 0; index--) {
            Entry entry = history.get(index);
            if (key.equals(entry.key)) {
                return entry.occurredAt;
            }
        }
        return null;
    }

    public static final class Entry {
        public final String key;
        public final String name;
        public final long occurredAt;

        Entry(String key, String name, long occurredAt) {
            this.key = key;
            this.name = name;
            this.occurredAt = occurredAt;
        }
    }
}
