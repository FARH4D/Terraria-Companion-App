package com.farh4d.terrariacompanion.beastiary;

import java.util.List;

public class NpcDataManager {

    public String name;
    public int health;
    public int defense;
    public int attack;
    public String knockback;
    public List<DropItem> drop_list;

    public NpcDataManager(String name, int health, int defense, int attack, String knockback, List<DropItem> drop_list) {
        this.name = name;
        this.health = health;
        this.defense = defense;
        this.attack = attack;
        this.knockback = knockback;
        this.drop_list = drop_list;
    }

}
