package com.farh4d.terrariacompanion.server;

public class SocketManagerSingleton {
    private static SocketManager instance;

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public static void setInstance(SocketManager socketManager) {
        instance = socketManager;
    }
}