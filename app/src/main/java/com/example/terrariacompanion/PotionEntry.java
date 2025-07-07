package com.example.terrariacompanion;

public class PotionEntry {
    private String name;
    private int itemId;
    private String base64;

    public PotionEntry(String name, int itemId, String base64) {
        this.name = name;
        this.itemId = itemId;
        this.base64 = base64;
    }

    public String getName() { return name; }
    public int getItemId() { return itemId; }
    public String getBase64() { return base64; }
}