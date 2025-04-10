package com.example.terrariacompanion;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

public class BossChecklist extends Fragment {

    private SocketManager socketManager;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.boss_checklist, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(requireActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_home).setOnClickListener(v -> {
            new Thread(() -> {
                socketManager.setCurrent_page("HOME");
                socketManager.sendMessage("HOME");
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment()).commit();
                }
            }).start();
        });
        ///////////////////////////////////////////////////////////

        LinearLayout pre_hardmode_container = view.findViewById(R.id.pre_hardmode_container);
        LinearLayout hardmode_container = view.findViewById(R.id.hardmode_container);



        new Thread(() -> {
            try {
                Thread.sleep(1000);
                socketManager.sendMessage("CHECKLIST");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    List<Pair<String, Boolean>> boss_checklist = server_data.getChecklistData();
                    if (boss_checklist != null) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {
                                    boolean wallOfFlesh = false;
                                    for (Pair<String, Boolean> boss : boss_checklist) {
                                        String bossName = boss.first;
                                        boolean defeated = boss.second;

                                        TextView bossView = new TextView(getContext());
                                        bossView.setText(bossName + (defeated ? " ✔" : " ✖"));
                                        bossView.setTextColor(defeated ? Color.GREEN : Color.RED);
                                        bossView.setTextSize(21);
                                        bossView.setPadding(8, 8, 8, 8);
                                        bossView.setGravity(Gravity.CENTER);
                                        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                        bossView.setTypeface(typeface);

                                        if (bossName.equalsIgnoreCase("Wall of Flesh")) {
                                            pre_hardmode_container.addView(bossView);
                                            wallOfFlesh = true;
                                        } else if (!wallOfFlesh) {
                                            pre_hardmode_container.addView(bossView);
                                        } else {
                                            hardmode_container.addView(bossView);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Connection error.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }
}
