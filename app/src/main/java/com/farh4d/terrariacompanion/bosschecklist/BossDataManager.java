package com.farh4d.terrariacompanion.bosschecklist;

import com.farh4d.terrariacompanion.beastiary.DropItem;
import com.farh4d.terrariacompanion.itemlist.ItemData;

import java.util.List;

public class BossDataManager {
    public String name;
    public String base64Image;
    public String spawnInfo;
    public List<ItemData> spawnItems;
    public List<DropItem> drop_list;
    public boolean defeated;

    public BossDataManager(String name, String base64Image, String spawnInfo, List<ItemData> spawnItems, List<DropItem> drop_list, boolean defeated) {
        this.name = name;
        this.base64Image = base64Image;
        this.spawnInfo = spawnInfo;
        this.spawnItems = spawnItems;
        this.drop_list = drop_list;
        this.defeated = defeated;
    }
}