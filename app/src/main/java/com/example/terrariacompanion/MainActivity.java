package com.example.terrariacompanion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.connect_screen); // Gets the layout of the screen from home_page.xml and sets it according to that.

        Window window = getWindow1();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // This makes it so the content on the screen can extend into the status bar (so the status bar doesn't just sit on top of everything)


        Button connect_button = findViewById(R.id.connect_button);
        EditText ip_form = findViewById(R.id.ip_form);

        connect_button.setOnClickListener(view -> {
            String input_text = ip_form.getText().toString().trim();

            if (input_text.contains(":")) {
                String[] parts = input_text.split(":");

                if (parts.length == 2) {
                    String ip = parts[0];

                    try {
                        int port = Integer.parseInt(parts[1]);

                        // Run socket logic in a separate thread
                        new Thread(() -> {
                            SocketManager socketManager = SocketManagerSingleton.getInstance();

                            try {
                                if (socketManager.connect(ip, port)) {

                                    SocketManagerSingleton.setInstance(socketManager);

                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();


                                        Intent home_intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(home_intent);
                                    });

                                } else {
                                    runOnUiThread(() -> Toast.makeText(this, "Failed to connect to server. Try double checking the your IP and Port.", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    }
                    catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid Port Number.", Toast.LENGTH_SHORT).show();

                    }


                } else {
                    Toast.makeText(this, "Invalid format, must use IP:Port", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid format, must use IP:Port", Toast.LENGTH_SHORT).show();
            }
        });

//        TextView textView = findViewById(R.id.textView1); // Replace with your TextView ID
//
//
    }

    @NonNull
    private Window getWindow1() {
        Window window = getWindow(); // Gets the window instance of the activity and gets the properties of it.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // Essentially tells the system that it wants to be in charge of drawing the background for the phone's status bar, so my app can handle it (this lets me make it transparent).
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // Clears the translucency of the status bar, the clear flag function lets me remove any specific setting.
        window.setStatusBarColor(Color.TRANSPARENT); // Sets the colour of the status bar to transparent
        return window;
    }
}
