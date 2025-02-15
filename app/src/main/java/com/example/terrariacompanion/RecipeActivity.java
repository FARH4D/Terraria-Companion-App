package com.example.terrariacompanion;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {

    private SocketManager socketManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recipe_list); // Gets the layout of the screen from home_page.xml and sets it according to that.
        Window window = getWindow1();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // This makes it so the content on the screen can extend into the status bar (so the status bar doesn't just sit on top of everything)

        SocketManager socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(this, "No active connection!", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity if no connection
            return;
        }

        new Thread(() -> {
            try {
                while (true) {
                    ServerResponse server_data = socketManager.receiveMessage();

                    System.out.println("test");
                    if (server_data != null) {
                        System.out.println("test2");
                        HashMap<String, List<Integer>> stringListHashMap = server_data.getRecipeData();
                        if (stringListHashMap != null) {
                            runOnUiThread(() -> {
                                for (Map.Entry<String, List<Integer>> entry : stringListHashMap.entrySet()) {
                                    System.out.println("Name: " + entry.getKey() + " - IDs: " + entry.getValue());
                                }
                            });
                        } else {
                            // Handle the case where the HashMap is null (could indicate server issue)
                            runOnUiThread(() -> Toast.makeText(this, "No data received from server.", Toast.LENGTH_SHORT).show());
                            break; // Break the loop if no data is received
                        }
                    } else {
                        // Handle null server data (likely connection issue or no data)
                        runOnUiThread(() -> Toast.makeText(this, "Disconnected or no response.", Toast.LENGTH_SHORT).show());
                        break; // End the loop if no response
                    }

                    // Sleep for a short time to avoid tight loop blocking (e.g., 1 second)
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Connection error.", Toast.LENGTH_SHORT).show());
                e.printStackTrace(); // Log the stack trace for better debugging
            }
        }).start();





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