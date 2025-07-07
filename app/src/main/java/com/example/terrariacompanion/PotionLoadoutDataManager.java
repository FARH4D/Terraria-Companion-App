package com.example.terrariacompanion;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}