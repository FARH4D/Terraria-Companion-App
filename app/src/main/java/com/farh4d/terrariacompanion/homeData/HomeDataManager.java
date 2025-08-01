package com.farh4d.terrariacompanion.homeData;

import org.json.JSONObject;

import java.util.List;

public class HomeDataManager {
    public int currentHealth;
    public int maxHealth;
    public int currentMana;
    public int maxMana;
    public List<String> playerNames;
    public JSONObject cosmetics;
    public String biome;
    public List<Integer> trackedItems;
    public boolean bossChecklist;

    public HomeDataManager(int currentHealth, int maxHealth, int currentMana, int maxMana, List<String> playerNames, JSONObject cosmetics, String biome, List<Integer> trackedItems, boolean bossChecklist) {
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.playerNames = playerNames;
        this.cosmetics = cosmetics;
        this.biome = biome;
        this.trackedItems = trackedItems;
        this.bossChecklist = bossChecklist;
    }
}