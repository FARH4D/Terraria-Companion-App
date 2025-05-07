package com.example.terrariacompanion;

import android.graphics.Bitmap;
import java.util.List;

public class BossDataManager {
    public String name;
    public String base64Image;
    public String spawnInfo;
    public List<ItemData> spawnItems;
    public List<DropItem> drops;
    public boolean defeated;

    public BossDataManager(String name, String base64Image, String spawnInfo, List<ItemData> spawnItems, List<DropItem> drops, boolean defeated) {
        this.name = name;
        this.base64Image = base64Image;
        this.spawnInfo = spawnInfo;
        this.spawnItems = spawnItems;
        this.drops = drops;
        this.defeated = defeated;
    }
}