package com.example.terrariacompanion;

import java.util.List;

public class DataManager1 {
    public int currentHealth;
    public int maxHealth;
    public int currentMana;
    public int maxMana;
    public List<String> playerNames;

    public DataManager1(int currentHealth, int maxHealth, int currentMana, int maxMana, List<String> playerNames) {
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.playerNames = playerNames;
    }
}