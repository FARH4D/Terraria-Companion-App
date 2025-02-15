package com.example.terrariacompanion;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SocketManager socketManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main, container, false);  // Just inflate the layout here, don't do UI interactions yet
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar health_bar = view.findViewById(R.id.health_bar);
        ProgressBar mana_bar = view.findViewById(R.id.mana_bar);
        TextView health_status = view.findViewById(R.id.health_status);
        TextView mana_status = view.findViewById(R.id.mana_status);
        TextView bufftitle = view.findViewById(R.id.buffTitle);
        LinearLayout player_names_view = view.findViewById(R.id.player_names_view);

        socketManager = SocketManagerSingleton.getInstance();
        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(getActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_recipe).setOnClickListener(v -> {
            new Thread(() -> {
                socketManager.setCurrent_page("RECIPES");
                socketManager.sendMessage("RECIPES");
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new RecipeFragment()).commit();
                }
            }).start();
        });
        ///////////////////////////////////////////////////////////

        new Thread(() -> {
            try {
                while (true) {
                    ServerResponse server_data = socketManager.receiveMessage();
                    if (server_data != null) {
                        DataManager1 data = server_data.getHomeData();
                        if (data != null) {
                            final DataManager1 finalData = data;
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (getActivity() != null) {
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
                                            TextView tv = new TextView(getContext());
                                            tv.setText(name);
                                            tv.setTextSize(20);
                                            Typeface custom = ResourcesCompat.getFont(getContext(), R.font.andy_bold);
                                            tv.setTypeface(custom);

                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            );

                                            params.gravity = Gravity.CENTER_HORIZONTAL;
                                            int marginInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                                            params.setMargins(0, marginInPixels, 0, marginInPixels);
                                            tv.setLayoutParams(params);

                                            player_names_view.addView(tv);
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Disconnected.", Toast.LENGTH_SHORT).show());
                        }
                        break;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Connection error.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
