package com.example.terrariacompanion;

import android.graphics.Typeface;
import android.os.Bundle;
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
    private boolean isReceivingData = false;
    private ProgressBar health_bar;
    private ProgressBar mana_bar;
    private TextView health_status;
    private TextView mana_status;
    private LinearLayout player_names_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main, container, false);  // Just inflate the layout here, don't do UI interactions yet
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        health_bar = view.findViewById(R.id.health_bar);
        mana_bar = view.findViewById(R.id.mana_bar);
        health_status = view.findViewById(R.id.health_status);
        mana_status = view.findViewById(R.id.mana_status);
        player_names_view = view.findViewById(R.id.player_names_view);

        socketManager = SocketManagerSingleton.getInstance();
        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(getActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_recipe).setOnClickListener(v -> {
            new Thread(() -> {
                isReceivingData = false;
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.setCurrent_page("RECIPES");
                socketManager.flushSocket();
                if (isAdded()) {
                    ItemFragment itemFragment = new ItemFragment();
                    Bundle args = new Bundle();
                    args.putInt("currentNum", 30);
                    itemFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, itemFragment).commit();
                }
            }).start();
        });

        view.findViewById(R.id.nav_beastiary).setOnClickListener(v -> {
            new Thread(() -> {
                isReceivingData = false;
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.setCurrent_page("BEASTIARY");
                socketManager.flushSocket();
                if (isAdded()) {
                    BeastiaryFragment beastiaryFragment = new BeastiaryFragment();
                    Bundle args = new Bundle();
                    args.putInt("currentNum", 30);
                    beastiaryFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, beastiaryFragment).commit();
                }
            }).start();
        });

        view.findViewById(R.id.nav_checklist).setOnClickListener(v -> {
            new Thread(() -> {
                isReceivingData = false;
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.flushSocket();
                socketManager.sendMessage("CHECKLIST");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data.getChecklistError().equals("No BossChecklist")){
                    socketManager.sendMessage("HOME");
                    isReceivingData = true;
                } else {
                    socketManager.setCurrent_page("CHECKLIST");
                    if (isAdded()) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new BossChecklist()).commit();
                    }
                }
            }).start();
        });
        ///////////////////////////////////////////////////////////

        isReceivingData = true;
        getData();

    }

    public void getData() {
        new Thread(() -> {
            try {
                while (isReceivingData && "HOME".equals(socketManager.getCurrent_page())) {
                    ServerResponse server_data = socketManager.receiveMessage();
                    if (server_data != null && isAdded()) {
                        HomeDataManager data = server_data.getHomeData();
                        if (data != null) {
                            requireActivity().runOnUiThread(() -> {
                                if (isAdded() && getActivity() != null) {
                                    health_bar.setProgress(data.currentHealth, true);
                                    health_bar.setMax(data.maxHealth);
                                    mana_bar.setProgress(data.currentMana, true);
                                    mana_bar.setMax(data.maxMana);
                                    health_status.setText(String.format(Locale.UK, "%d/%d", data.currentHealth, data.maxHealth));
                                    mana_status.setText(String.format(Locale.UK, "%d/%d", data.currentMana, data.maxMana));

                                    player_names_view.removeAllViews();
                                    List<String> player_names = data.playerNames;
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
                    } else {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getActivity(), "Disconnected.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Connection Error.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

}
