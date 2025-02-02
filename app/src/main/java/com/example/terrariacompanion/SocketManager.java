package com.example.terrariacompanion;

import java.io.*;
import java.net.Socket;

public class SocketManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

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

    public String receiveMessage() {
        if (socket != null && !socket.isClosed() && input != null) {
            try {
                return input.readLine(); // Waits for a response from the server
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        }
        return null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && output != null && input != null;
    }

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
}