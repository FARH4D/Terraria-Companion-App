package com.example.terrariacompanion;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView1); // Replace with your TextView ID

        // Run socket logic in a separate thread
        new Thread(() -> {
            SocketManager socketManager = new SocketManager();

            try {
                if (socketManager.connect("192.168.1.179", 12345)) {
                    runOnUiThread(() -> textView.setText("Connected to Terraria server!"));

                    // Example: Send a greeting
                    socketManager.sendMessage("Hello Terraria!");

                    // Example: Continuously read updates
                    while (true) {
                        String serverMessage = socketManager.receiveMessage();
                        if (serverMessage != null) {
                            runOnUiThread(() -> textView.setText("Received: " + serverMessage));
                        } else {
                            runOnUiThread(() -> textView.setText("Disconnected or no more messages."));
                            break;
                        }
                    }

                    socketManager.disconnect();
                } else {
                    runOnUiThread(() -> textView.setText("Failed to connect to server."));
                }
            } catch (Exception e) {
                runOnUiThread(() -> textView.setText("Error: " + e.toString()));
            }
        }).start();
    }
}
