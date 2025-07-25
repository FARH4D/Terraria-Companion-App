package com.farh4d.terrariacompanion.server;

import android.util.Pair;

import com.farh4d.terrariacompanion.homeData.HomeDataManager;
import com.farh4d.terrariacompanion.beastiary.NpcDataManager;
import com.farh4d.terrariacompanion.bosschecklist.BossDataManager;
import com.farh4d.terrariacompanion.itemlist.ItemData;
import com.farh4d.terrariacompanion.itemlist.ItemDataManager;
import com.farh4d.terrariacompanion.potions.PotionEntry;

import java.util.List;

public class ServerResponse {
    private HomeDataManager homeData;
    private NpcDataManager npcData;
    private ItemDataManager itemData;
    private List<ItemData> recipeData;
    private List<Pair<String, Boolean>> checklistData;
    private List<PotionEntry> potions;
    private BossDataManager bossData;
    private String checklistError = "";

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

    public static ServerResponse fromPotions(List<PotionEntry> potionList) {
        ServerResponse response = new ServerResponse();
        response.potions = potionList;
        return response;
    }

    public ServerResponse(BossDataManager bossData) {
        this.bossData = bossData;
    }

    public ServerResponse(String errorMessage) {
        this.checklistError = errorMessage;}

    public HomeDataManager getHomeData() { return homeData; }

    public NpcDataManager getNpcData() { return npcData; }

    public ItemDataManager getItemData() { return itemData; }

    public List<ItemData> getRecipeData() { return recipeData; }

    public List<PotionEntry> getPotionList() { return potions; }

    public List<Pair<String, Boolean>> getChecklistData() { return checklistData; }

    public BossDataManager getBossData() { return bossData; }

    public String getChecklistError() { return checklistError; }

}
