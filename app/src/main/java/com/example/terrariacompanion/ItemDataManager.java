package com.example.terrariacompanion;

import java.util.List;
import java.util.Map;

public class ItemDataManager {

    public String name;
    public List<List<Map<String, Object>>> recipes;

    public ItemDataManager(String name, List<List<Map<String, Object>>> recipes) {
        this.name = name;
        this.recipes = recipes;
    }
}
