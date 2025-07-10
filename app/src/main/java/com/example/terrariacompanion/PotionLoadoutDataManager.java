package com.example.terrariacompanion;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PotionLoadoutDataManager {

    private Map<String, List<PotionEntry>> loadouts = new HashMap<>();

    public void put(String key, List<PotionEntry> value) {
        loadouts.put(key, value);
    }

    public Map<String, List<PotionEntry>> getAll() {
        return loadouts;
    }

    public void saveToJson(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("potion_loadouts.json", Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            Gson gson = new Gson();
            gson.toJson(loadouts, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromJson(Context context) {
        try {
            FileInputStream fis = context.openFileInput("potion_loadouts.json");
            InputStreamReader reader = new InputStreamReader(fis);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, List<PotionEntry>>>() {}.getType();
            Map<String, List<PotionEntry>> rawMap = gson.fromJson(reader, type);
            reader.close();
            if (rawMap != null) {
                loadouts.clear();
                loadouts.putAll(rawMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteLoadout(String name) {
        if (loadouts.containsKey(name)) {
            loadouts.remove(name);
        }
    }

    public List<PotionEntry> getAllPotionsFlatList() {
        Set<PotionEntry> flatSet = new HashSet<>();
        for (List<PotionEntry> potions : loadouts.values()) {
            flatSet.addAll(potions);
        }
        return new ArrayList<>(flatSet);
    }

}