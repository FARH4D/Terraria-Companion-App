package com.example.terrariacompanion;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnection {

    // Use a single-threaded executor for networking
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void connectToServer(final String ipAddress, final int port, final ConnectionCallback callback) {
        executorService.execute(() -> {
            String result = "";
            try {
                // Connect to the Terraria server
                result = "hiii";
                Socket socket = new Socket(ipAddress, port);

                // Read data from the server
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                result = input.readLine();

                // Close the connection
                input.close();
                socket.close();
            } catch (Exception e) {
                Log.e("ServerConnection", "Error connecting to server", e);
                result = "Error: " + e.getMessage();
            }

            // Send the result back to the main thread
            String finalResult = result;
            mainThreadHandler.post(() -> callback.onResult(finalResult));
        });
    }

    public interface ConnectionCallback {
        void onResult(String result);
    }
}
