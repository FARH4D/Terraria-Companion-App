package com.example.terrariacompanion;

import java.util.HashMap;
import java.util.List;

public class ServerResponse {
    private DataManager1 homeData;
    private HashMap<String, List<Integer>> recipeData;

    public ServerResponse(DataManager1 homeData) {
        this.homeData = homeData;
    }

    public ServerResponse(HashMap<String, List<Integer>> stringListHashMap) {
        this.recipeData = recipeData;
    }

    public DataManager1 getHomeData() {
        return homeData;
    }

    public HashMap<String, List<Integer>> getRecipeData() {
        return recipeData;
    }

    public boolean isHomeData() {
        return homeData != null;
    }
}
