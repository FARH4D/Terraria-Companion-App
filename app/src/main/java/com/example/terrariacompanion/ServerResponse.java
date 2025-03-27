package com.example.terrariacompanion;

import java.util.List;

public class ServerResponse {
    private HomeDataManager homeData;
    private NpcDataManager npcData;
    private ItemDataManager itemData;
    private List<ItemData> recipeData;

    public ServerResponse(HomeDataManager homeData) {
        this.homeData = homeData;
    }

    public ServerResponse(NpcDataManager npcData) {
        this.npcData = npcData;
    }

    public ServerResponse(ItemDataManager itemData) {
        this.itemData = itemData;
    }

    public ServerResponse(List<ItemData> recipeData) { this.recipeData = recipeData;}

    public HomeDataManager getHomeData() { return homeData; }

    public NpcDataManager getNpcData() { return npcData; }

    public ItemDataManager getItemData() { return itemData; }

    public List<ItemData> getRecipeData() { return recipeData; }

}
