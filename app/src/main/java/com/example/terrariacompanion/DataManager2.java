package com.example.terrariacompanion;

import java.util.List;

public class DataManager2 {

    public String name;
    public int health;
    public int defense;
    public List<DropItem> drop_list;

    public DataManager2(String name, int health, int defense, List<DropItem> drop_list) {
        this.name = name;
        this.health = health;
        this.defense = defense;
        this.drop_list = drop_list;
    }

}
