package com.example.terrariacompanion;

import java.util.List;
import java.util.Map;

public class ItemDataManager {

    public String name;
    public int id;
    public List<List<Map<String, Object>>> recipes;

    public ItemDataManager(String name, int id, List<List<Map<String, Object>>> recipes) {
        this.name = name;
        this.id = id;
        this.recipes = recipes;
    }
}
