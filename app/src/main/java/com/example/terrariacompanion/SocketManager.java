package com.example.terrariacompanion;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        } else {
            System.err.println("Socket is not connected or is closed!");
        }
    }

    public ServerResponse receiveMessage() {
        if (socket != null && !socket.isClosed() && input != null) {
            try {
                String jsonData = input.readLine();
                if (jsonData != null) {
                    System.out.println("Received: " + jsonData);
                    if ("HOME".equals(getCurrent_page())) {
                        return new ServerResponse(processServerData(jsonData));
                    } else if ("RECIPES".equals(getCurrent_page())) {
                        return new ServerResponse(processItemsData(jsonData));
                    }

                } else {
                    System.err.println("json data is null");
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
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

    private DataManager1 processServerData(String jsonData) {
        try {
            JSONObject playerData = new JSONObject(jsonData);

            JSONObject health = playerData.getJSONObject("health");
            int currentHealth = health.getInt("current");
            int maxHealth = health.getInt("max");

            JSONObject mana = playerData.getJSONObject("mana");
            int currentMana = mana.getInt("current");
            int maxMana = mana.getInt("max");

            // Extract other values
            JSONArray player_array = playerData.getJSONArray("player_list");
            List<String> player_names = new ArrayList<>();

//            JSONArray boss_names_array = playerData.getJSONArray("boss_names");
//            List<String> boss_names = new ArrayList<>();

            for (int i = 0; i < player_array.length(); i++) {
                player_names.add(player_array.getString(i));
            }

            return new DataManager1(currentHealth, maxHealth, currentMana, maxMana, player_names);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private HashMap<String, List<Integer>> processItemsData(String jsonData) {
        HashMap<String, List<Integer>> itemMap = new HashMap<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                int id = jsonObject.getInt("id");

                itemMap.computeIfAbsent(name, k -> new ArrayList<>()).add(id);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return itemMap;
    }




}