package com.example.terrariacompanion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SocketManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private SocketManager socketManager;
    private String current_page = "HOME";
    private volatile String status = "working";
    private volatile boolean again = true;
    public boolean connect(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (socket != null && !socket.isClosed() && output != null) {
            output.println(message);
        }
    }

    public ServerResponse receiveMessage() {
        try {
            socketManager = SocketManagerSingleton.getInstance();
            if (socket != null && !socket.isClosed() && input != null) {
                socket.setSoTimeout(2000);
                String jsonData = input.readLine();

                if (jsonData != null) {
                    System.out.println("Received: " + jsonData);
                    String cleanedData = jsonData.replaceAll("\"", "").trim();

                    String currentPage = getCurrent_page();

                    if (cleanedData.trim().equals("No BossChecklist")) {
                        return new ServerResponse(cleanedData.trim());
                    }

                    if ("HOME".equals(currentPage)) {
                        if (jsonData.trim().startsWith("{")) {
                            return new ServerResponse(processHomeData(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("RECIPES".equals(currentPage)) {
                        if (jsonData.trim().startsWith("[")) {
                            return ServerResponse.fromRecipes(processItemsData(jsonData));
                        } else if (jsonData.trim().equals("MAX")){
                            socketManager.setStatus("MAX");
                            return receiveMessage();
                        }
                        else {
                            return receiveMessage();
                        }
                    }
                    else if ("BEASTIARY".equals(currentPage)) {
                        if (jsonData.trim().startsWith("[")) {
                            return ServerResponse.fromRecipes(processItemsData(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("BEASTIARYINFO".equals(currentPage)) {
                        if (jsonData.trim().startsWith("{")) {
                            return new ServerResponse(processNpcPage(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("ITEMINFO".equals(currentPage)) {
                        if (jsonData.trim().startsWith("{")) {
                            return new ServerResponse(processItemPage(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("CHECKLIST".equals(currentPage)) {
                        if (jsonData.trim().startsWith("[")) {
                            return ServerResponse.fromChecklist(processChecklist(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("BOSSINFO".equals(currentPage)) {
                        if (jsonData.trim().startsWith("{")) {
                            return new ServerResponse(processBossData(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
        return null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && output != null && input != null;
    }

    public void setCurrent_page(String current_page){ this.current_page = current_page; }

    public String getCurrent_page(){ return this.current_page; }

    public synchronized void setStatus(String new_status) { status = new_status; }

    public synchronized String getStatus(){ return status; }

    public synchronized void setAgain(boolean new_status) { again = new_status; }

    public synchronized boolean getAgain(){ return again; }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    private HomeDataManager processHomeData(String jsonData) {
        try {
            JSONObject playerData = new JSONObject(jsonData);

            JSONObject health = playerData.getJSONObject("health");
            int currentHealth = health.getInt("current");
            int maxHealth = health.getInt("max");

            JSONObject mana = playerData.getJSONObject("mana");
            int currentMana = mana.getInt("current");
            int maxMana = mana.getInt("max");

            JSONArray player_array = playerData.getJSONArray("player_list");
            List<String> player_names = new ArrayList<>();

            for (int i = 0; i < player_array.length(); i++) {
                player_names.add(player_array.getString(i));
            }

            return new HomeDataManager(currentHealth, maxHealth, currentMana, maxMana, player_names);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private NpcDataManager processNpcPage(String jsonData) {
        try {
            JSONObject npcData = new JSONObject(jsonData);

            String name = npcData.getString("name");
            int hp = npcData.getInt("hp");
            int defense = npcData.getInt("defense");
            int attack = npcData.getInt("attack");
            String knockback = npcData.getString("knockback");

            JSONArray dropArray = npcData.getJSONArray("drop_list");
            List<DropItem> dropList = new ArrayList<>();

            if (dropArray.length() > 0) {
                for (int i = 0; i < dropArray.length(); i++) {
                    JSONObject dropObject = dropArray.getJSONObject(i);

                    Map<String, Object> dropItem = new HashMap<>();
                    int id = dropObject.getInt("id");
                    String dropName = dropObject.getString("name");
                    String image = dropObject.getString("image");
                    double droprate = dropObject.getDouble("droprate");

                    dropList.add(new DropItem(id, dropName, image, droprate));
                }
            }

            return new NpcDataManager(name, hp, defense, attack, knockback, dropList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<ItemData> processItemsData(String jsonData) {
        List<ItemData> itemList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                int id = jsonObject.getInt("id");
                String base64Image = jsonObject.getString("image");

                Bitmap bitmap = decodeBase64ToBitmap(base64Image);

                itemList.add(new ItemData(name, id, bitmap));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return itemList;
    }

    private ItemDataManager processItemPage(String jsonData) {
        try {
            JSONObject itemData = new JSONObject(jsonData);

            String name = itemData.getString("name");
            JSONArray recipeArray = itemData.getJSONArray("recipes");
            List<List<Map<String, Object>>> recipeList = new ArrayList<>();

            if (recipeArray.length() > 0) {
                for (int i = 0; i < recipeArray.length(); i++) {
                    JSONArray recipeObject = recipeArray.getJSONArray(i);

                    List<Map<String, Object>> recipe = new ArrayList<>();

                    for (int j = 0; j < recipeObject.length(); j++) {
                        JSONObject entryObject = recipeObject.getJSONObject(j);

                        Map<String, Object> data = new HashMap<>();
                        if (entryObject.has("id") && entryObject.has("name") && entryObject.has("image")) {
                            data.put("id", entryObject.getInt("id"));
                            data.put("name", entryObject.getString("name"));
                            data.put("image", entryObject.getString("image"));

                            if (entryObject.has("quantity")) {
                                data.put("quantity", entryObject.getInt("quantity"));
                            }
                            recipe.add(data);
                        }
                    }
                    recipeList.add(recipe);
                }
            }

            return new ItemDataManager(name, recipeList);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Pair<String, Boolean>> processChecklist(String jsonData) {
        List<Pair<String, Boolean>> bossList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                boolean downed = obj.getBoolean("downed");
                bossList.add(new Pair<>(name, downed));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bossList;
    }

    private BossDataManager processBossData(String jsonData) {
        try {
            JSONObject bossData = new JSONObject(jsonData);
            String name = bossData.getString("bossName");
            String base64Image = bossData.getString("bossImage");
            String spawnInfo = bossData.getString("spawnInfo");
            boolean defeated = bossData.getBoolean("status");

            JSONArray spawnItemsArray = bossData.getJSONArray("spawnItems");
            List<ItemData> spawnItems = new ArrayList<>();
            for (int i = 0; i < spawnItemsArray.length(); i++) {
                JSONObject itemObj = spawnItemsArray.getJSONObject(i);
                String itemName = itemObj.getString("name");
                int itemId = itemObj.getInt("id");

                String base64Image2 = itemObj.getString("image");
                Bitmap bitmap2 = decodeBase64ToBitmap(base64Image2);

                spawnItems.add(new ItemData(itemName, itemId, bitmap2));
            }

            JSONArray dropsArray = bossData.getJSONArray("drops");
            List<DropItem> drops = new ArrayList<>();
            for (int i = 0; i < dropsArray.length(); i++) {
                JSONObject dropObj = dropsArray.getJSONObject(i);
                String dropName = dropObj.getString("name");
                int dropId = dropObj.getInt("id");
                float dropRate = (float) dropObj.getDouble("dropRate");

                String base64Image3 = dropObj.getString("image");

                drops.add(new DropItem(dropId, dropName, base64Image3, dropRate));
            }

            return new BossDataManager(name, base64Image, spawnInfo, spawnItems, drops, defeated);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = android.util.Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void flushSocket() {
        try {
            if (input != null) {
                while (input.ready()) {
                    String discarded = input.readLine();
                    System.out.println("Flushed: " + discarded);
                }
            }
        } catch (Exception e) {
            System.out.println("Error while flushing socket: " + e.getMessage());
        }
    }
}