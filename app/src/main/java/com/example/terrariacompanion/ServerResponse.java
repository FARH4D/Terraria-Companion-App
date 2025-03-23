package com.example.terrariacompanion;

import java.util.HashMap;
import java.util.List;

import kotlin.Pair;

public class ServerResponse {
    private DataManager1 homeData;
    private DataManager2 npcData;
    private DataManager3 itemData;
    private List<ItemData> recipeData;

    public ServerResponse(DataManager1 homeData) {
        this.homeData = homeData;
    }

    public ServerResponse(DataManager2 npcData) {
        this.npcData = npcData;
    }

    public ServerResponse(DataManager3 itemData) {
        this.itemData = itemData;
    }

    public ServerResponse(List<ItemData> recipeData) { this.recipeData = recipeData;}

    public DataManager1 getHomeData() { return homeData; }

    public DataManager2 getNpcData() { return npcData; }

    public DataManager3 getItemData() { return itemData; }

    public List<ItemData> getRecipeData() { return recipeData; }

}
