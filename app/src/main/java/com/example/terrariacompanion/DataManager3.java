package com.example.terrariacompanion;

import java.util.List;
import java.util.Map;

public class DataManager3 {

    public String name;
    public List<List<Map<String, Object>>> recipes;

    public DataManager3(String name, List<List<Map<String, Object>>> recipes) {
        this.name = name;
        this.recipes = recipes;
    }
}
