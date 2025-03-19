package com.example.terrariacompanion;

import java.util.List;

public class DataManager2 {

    public String name;
    public int health;
    public int defense;
    public int attack;
    public String knockback;
    public List<DropItem> drop_list;

    public DataManager2(String name, int health, int defense, int attack, String knockback, List<DropItem> drop_list) {
        this.name = name;
        this.health = health;
        this.defense = defense;
        this.attack = attack;
        this.knockback = knockback;
        this.drop_list = drop_list;
    }

}
