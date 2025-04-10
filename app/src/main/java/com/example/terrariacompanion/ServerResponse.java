package com.example.terrariacompanion;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;

public class ServerResponse {
    private HomeDataManager homeData;
    private NpcDataManager npcData;
    private ItemDataManager itemData;
    private List<ItemData> recipeData;
    private List<Pair<String, Boolean>> checklistData;

    private ServerResponse() {}
    public ServerResponse(HomeDataManager homeData) {
        this.homeData = homeData;
    }

    public ServerResponse(NpcDataManager npcData) {
        this.npcData = npcData;
    }

    public ServerResponse(ItemDataManager itemData) {
        this.itemData = itemData;
    }

    public static ServerResponse fromRecipes(List<ItemData> recipeData) {
        ServerResponse response = new ServerResponse();
        response.recipeData = recipeData;
        return response;
    }

    public static ServerResponse fromChecklist(List<Pair<String, Boolean>> checklistData) {
        ServerResponse response = new ServerResponse();
        response.checklistData = checklistData;
        return response;
    }

    public HomeDataManager getHomeData() { return homeData; }

    public NpcDataManager getNpcData() { return npcData; }

    public ItemDataManager getItemData() { return itemData; }

    public List<ItemData> getRecipeData() { return recipeData; }

    public List<Pair<String, Boolean>> getChecklistData() { return checklistData; }

}
