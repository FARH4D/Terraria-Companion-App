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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;
import java.util.Locale;

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


        ProgressBar health_bar = findViewById(R.id.health_bar);
        ProgressBar mana_bar = findViewById(R.id.mana_bar);
        TextView health_status = findViewById(R.id.health_status);
        TextView mana_status = findViewById(R.id.mana_status);
        TextView bufftitle = findViewById(R.id.buffTitle);
        LinearLayout player_names_view = findViewById(R.id.player_names_view);


        new Thread(() -> {
            try {
                while (true) {
                    DataManager1 data = socketManager.receiveMessage();
                    if (data != null) {
                        final DataManager1 finalData = data;
                        runOnUiThread(() -> {

                            player_names_view.removeAllViews();
                            health_bar.setProgress(finalData.currentHealth, true);
                            health_bar.setMax(finalData.maxHealth);
                            mana_bar.setProgress(finalData.currentMana, true);
                            mana_bar.setMax(finalData.maxMana);
                            health_status.setText(String.format(Locale.UK, "%d/%d", finalData.currentHealth, finalData.maxHealth));
                            mana_status.setText(String.format(Locale.UK, "%d/%d", finalData.currentMana, finalData.maxMana));
                            bufftitle.setText(TextUtils.join(", ", finalData.playerNames));

                            List<String> player_names = finalData.playerNames;

                            for (String name : player_names) {

                                TextView tv = new TextView(this);

                                tv.setText(name);
                                tv.setTextSize(20);
                                Typeface custom = ResourcesCompat.getFont(this, R.font.andy_bold);
                                tv.setTypeface(custom);

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );

                                params.gravity = Gravity.CENTER_HORIZONTAL;

                                int marginInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, this.getResources().getDisplayMetrics());
                                params.setMargins(0, marginInPixels, 0, marginInPixels);
                                tv.setLayoutParams(params);

                                player_names_view.addView(tv);

                            }


                        });
                    } else {
                        mana_status.setText("not work");
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
