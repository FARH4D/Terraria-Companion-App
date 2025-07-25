package com.farh4d.terrariacompanion.bosschecklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

import java.util.List;

public class BossChecklist extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private boolean changePage = false;
    private boolean previousDefeated = false;
    private boolean progressionMode = true;
    private LinearLayout pre_hardmode_container;
    private LinearLayout hardmode_container;
    private ServerResponse server_data;
    private int trackedItemInt;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.boss_checklist, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(requireActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_home).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
                SharedPreferences prefs = requireActivity().getSharedPreferences("TrackedItemPrefs", Context.MODE_PRIVATE);
                trackedItemInt = prefs.getInt("tracked_item_id", 1);

                socketManager.setCurrent_page("HOME");
                socketManager.sendMessage("HOME:" + trackedItemInt + ":null");
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment()).commit();
                }
            }).start();
        });

        view.findViewById(R.id.nav_recipe).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
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
                    args.putString("category", "all");
                    args.putString("search", "");
                    itemFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, itemFragment).commit();
                }
            }).start();
        });

        view.findViewById(R.id.nav_beastiary).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
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
                    args.putString("search", "");
                    beastiaryFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, beastiaryFragment).commit();
                }
            }).start();
        });

        view.findViewById(R.id.nav_checklist).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (SessionData.hasBossChecklist()) {
                    socketManager.setCurrent_page("CHECKLIST");
                    socketManager.flushSocket();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BossChecklist()).commit();
                } else {
                    return;
                }
            }).start();
        });
        ///////////////////////////////////////////////////////////

        pre_hardmode_container = view.findViewById(R.id.pre_hardmode_container);
        hardmode_container = view.findViewById(R.id.hardmode_container);
        ImageView progression_button = view.findViewById(R.id.progression_button);

        progression_button.setOnClickListener(v -> {
            if (progressionMode) {
                progressionMode = false;
                progression_button.setImageResource(R.drawable.grey_button);
                requireActivity().runOnUiThread(() -> {
                    this.setupNoProgression();
                });
            }
            else {
                progressionMode = true;
                progression_button.setImageResource(R.drawable.red_button);
                requireActivity().runOnUiThread(() -> {
                    this.setupProgression();
                });
            }
        });

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                socketManager.sendMessage("CHECKLIST:1:null");
                server_data = socketManager.receiveMessage();

                requireActivity().runOnUiThread(() -> {
                    this.setupProgression();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Connection error.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();

    }

    private void setupNoProgression() {
        if (server_data != null) {
            pre_hardmode_container.removeAllViews();
            hardmode_container.removeAllViews();
            List<Pair<String, Boolean>> boss_checklist = server_data.getChecklistData();
            if (boss_checklist != null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (getActivity() != null) {
                            boolean wallOfFlesh = false;
                            int i = 0;

                            for (Pair<String, Boolean> boss : boss_checklist) {
                                String bossName = boss.first;
                                boolean defeated = boss.second;

                                TextView bossView = new TextView(getContext());
                                bossView.setText(bossName + (defeated ? " ✔" : " ✖"));
                                bossView.setTextColor(defeated ? Color.GREEN : Color.RED);
                                bossView.setTextSize(23);
                                bossView.setPadding(8, 8, 8, 8);
                                bossView.setGravity(Gravity.CENTER);
                                Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                bossView.setTypeface(typeface);

                                bossView.setTag(i);

                                if (bossName.equalsIgnoreCase("Wall of Flesh")) {
                                    pre_hardmode_container.addView(bossView);
                                    wallOfFlesh = true;
                                } else if (!wallOfFlesh) {
                                    pre_hardmode_container.addView(bossView);
                                } else {
                                    hardmode_container.addView(bossView);
                                }

                                i++;

                                bossView.setOnClickListener(v -> {
                                    new Thread(() -> {
                                        socketManager.setCurrent_page("BOSSINFO");
                                        if (isAdded()) {
                                            BossInfo bossInfoFragment = new BossInfo();
                                            Bundle args = new Bundle();
                                            args.putInt("bossNum", (int) bossView.getTag());
                                            bossInfoFragment.setArguments(args);
                                            requireActivity().getSupportFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container, bossInfoFragment).commit();
                                        }
                                    }).start();
                                });
                            }
                        }
                    });
                }
                changePage = true;
            }
        }
    }

    private void setupProgression() {
        if (server_data != null) {
            previousDefeated = false;

            pre_hardmode_container.removeAllViews();
            hardmode_container.removeAllViews();

            List<Pair<String, Boolean>> boss_checklist = server_data.getChecklistData();

            if (boss_checklist != null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (getActivity() != null) {
                            boolean wallOfFlesh = false;
                            int i = 0;
                            final boolean[] revealedNext = {false}; // wrapped in array for lambda

                            for (Pair<String, Boolean> boss : boss_checklist) {
                                String bossName = boss.first;
                                boolean defeated = boss.second;

                                TextView bossView = new TextView(getContext());
                                bossView.setTextSize(23);
                                bossView.setPadding(8, 8, 8, 8);
                                bossView.setGravity(Gravity.CENTER);
                                Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                bossView.setTypeface(typeface);

                                bossView.setTag(i);

                                if (defeated) {
                                    bossView.setText(bossName + " ✔");
                                    bossView.setTextColor(Color.GREEN);
                                    previousDefeated = true;
                                } else if (previousDefeated && !revealedNext[0]) {
                                    bossView.setText(bossName + " ✖");
                                    bossView.setTextColor(Color.RED);
                                    revealedNext[0] = true;
                                } else {
                                    bossView.setText("???");
                                    bossView.setTextSize(25);
                                    bossView.setTextColor(Color.RED);
                                }

                                if (bossName.equalsIgnoreCase("Wall of Flesh")) {
                                    pre_hardmode_container.addView(bossView);
                                    wallOfFlesh = true;
                                } else if (!wallOfFlesh) {
                                    pre_hardmode_container.addView(bossView);
                                } else {
                                    hardmode_container.addView(bossView);
                                }

                                i++;

                                bossView.setOnClickListener(v -> {
                                    new Thread(() -> {
                                        socketManager.setCurrent_page("BOSSINFO");
                                        if (isAdded()) {
                                            BossInfo bossInfoFragment = new BossInfo();
                                            Bundle args = new Bundle();
                                            args.putInt("bossNum", (int) bossView.getTag());
                                            bossInfoFragment.setArguments(args);
                                            requireActivity().getSupportFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container, bossInfoFragment).commit(); 
                                        }
                                    }).start();
                                });

                            }
                        }
                    });
                }
                changePage = true;
            }
        }
    }
}
