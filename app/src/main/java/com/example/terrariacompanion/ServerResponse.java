package com.example.terrariacompanion;

import java.util.HashMap;
import java.util.List;

import kotlin.Pair;

public class ServerResponse {
    private DataManager1 homeData;
    private List<Pair<String, Integer>> recipeData;

    public ServerResponse(DataManager1 homeData) {
        this.homeData = homeData;
    }

    public ServerResponse(List<Pair<String, Integer>> recipeData) {
        this.recipeData = recipeData;
    }

    public DataManager1 getHomeData() {
        return homeData;
    }

    public List<Pair<String, Integer>> getRecipeData() {
        return recipeData;
    }

    public boolean isHomeData() {
        return homeData != null;
    }
}
