package com.example.terrariacompanion;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); // Gets the layout of the screen from home_page.xml and sets it according to that.
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
                    String message = socketManager.receiveMessage();
                    if (message != null) {
                        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Disconnected.", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Connection error.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.disconnect();
        }
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
