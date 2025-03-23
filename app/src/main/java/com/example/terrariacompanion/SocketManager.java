package com.example.terrariacompanion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocketManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private String current_page = "HOME";
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
            if (socket != null && !socket.isClosed() && input != null) {
                socket.setSoTimeout(3000);
                String jsonData = input.readLine();

                if (jsonData != null) {
                    System.out.println("Received: " + jsonData);

                    String currentPage = getCurrent_page();

                    if ("HOME".equals(currentPage)) {
                        if (jsonData.trim().startsWith("{")) {
                            return new ServerResponse(processHomeData(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("RECIPES".equals(currentPage)) {
                        if (jsonData.trim().startsWith("[")) {
                            return new ServerResponse(processItemsData(jsonData));
                        } else {
                            return receiveMessage();
                        }
                    }
                    else if ("BEASTIARY".equals(currentPage)) {
                        if (jsonData.trim().startsWith("[")) {
                            return new ServerResponse(processItemsData(jsonData));
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

    public void setCurrent_page(String current_page){ this.current_page = current_page;}

    public String getCurrent_page(){ return this.current_page; }

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

    private DataManager1 processHomeData(String jsonData) {
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

            return new DataManager1(currentHealth, maxHealth, currentMana, maxMana, player_names);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private DataManager2 processNpcPage(String jsonData) {
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

            return new DataManager2(name, hp, defense, attack, knockback, dropList);

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

    private DataManager3 processItemPage(String jsonData) {
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

            return new DataManager3(name, recipeList);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = android.util.Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}