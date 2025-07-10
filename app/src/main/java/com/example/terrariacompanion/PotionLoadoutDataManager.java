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
            File file = new File(context.getFilesDir(), "potion_loadouts.json");
            if (file.exists()) {
                FileInputStream fis = context.openFileInput("potion_loadouts.json");
                InputStreamReader reader = new InputStreamReader(fis);
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, List<PotionEntry>>>() {}.getType();
                Map<String, List<PotionEntry>> existingData = gson.fromJson(reader, type);
                reader.close();

                if (existingData != null) {
                    for (Map.Entry<String, List<PotionEntry>> entry : existingData.entrySet()) {
                        if (!loadouts.containsKey(entry.getKey())) {
                            loadouts.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
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