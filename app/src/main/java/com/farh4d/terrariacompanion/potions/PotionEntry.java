package com.farh4d.terrariacompanion.potions;

public class PotionEntry {
    private String name;
    private String modName;
    private String internalName;
    private String base64;

    public PotionEntry(String name, String modName, String internalName, String base64) {
        this.name = name;
        this.modName = modName;
        this.internalName = internalName;
        this.base64 = base64;
    }

    public String getName() { return name; }
    public String getMod() { return modName; }
    public String getInternalName() { return internalName; }
    public String getBase64() { return base64; }
}