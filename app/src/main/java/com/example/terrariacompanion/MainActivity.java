package com.example.terrariacompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView1);

        ServerConnection serverConnection = new ServerConnection();

        // Replace with the actual IP address of the Terraria server
        String serverIp = "192.168.1.179"; // Example IP address
        int serverPort = 12345;

        serverConnection.connectToServer(serverIp, serverPort, result -> {
            // Update the TextView with the received result
            textView.setText("Player Stats: " + result);
        });


    }
}