package com.farh4d.terrariacompanion.potions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.bosschecklist.BossChecklist;
import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreatePotionFragment extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private Set<PotionEntry> selectedPotions = new HashSet<>();
    private int trackedItemInt;
    private Vibrator vibrator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_potion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar
        SoundManager.init(getContext());

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(requireActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        GridLayout loadoutGrid = view.findViewById(R.id.loadout_grid);

        view.findViewById(R.id.nav_home).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
                SoundManager.playClick();
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
                SoundManager.playClick();
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
                SoundManager.playClick();
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
                SoundManager.playClick();
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
        ////////////////////////////////////////////////////////////////////////////////////////

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
                SoundManager.playClick();
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.flushSocket();
                socketManager.setCurrent_page("NULL");
                socketManager.sendMessage("NULL");
                socketManager.flushSocket();
                if (isAdded()) {
                    PotionFragment potionFragment = new PotionFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, potionFragment).commit();
                }
            }).start();
        });

        ///////////////////////////////////////////////////////////

        PotionLoadoutDataManager loadoutMap = new PotionLoadoutDataManager();
        Button saveButton = view.findViewById(R.id.save_button);
        EditText potionNameEntry = view.findViewById(R.id.potionNameEntry);

        saveButton.setOnClickListener(v -> {
            SoundManager.playClick();
            String loadoutName = potionNameEntry.getText().toString().trim();

            if (loadoutName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a loadout name.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<PotionEntry> loadoutList = new ArrayList<>(selectedPotions);
            loadoutMap.loadFromJson(requireContext());
            loadoutMap.put(loadoutName, loadoutList);
            loadoutMap.saveToJson(requireContext());

            new Thread(() -> {
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.flushSocket();
                socketManager.setCurrent_page("NULL");
                socketManager.sendMessage("NULL");
                socketManager.flushSocket();
                if (isAdded()) {
                    PotionFragment potionFragment = new PotionFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, potionFragment).commit();
                }
            }).start();

        });

        new Thread(() -> {
            try {
                socketManager.sendMessage("CREATEPOTION");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    List<PotionEntry> potion_list = server_data.getPotionList();
                    if (potion_list != null && !potion_list.isEmpty()) {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                for (PotionEntry potion : potion_list) {
                                    FrameLayout potionFrame = new FrameLayout(requireContext());
                                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                                    params.width = 250;
                                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                    params.setGravity(Gravity.CENTER_HORIZONTAL);
                                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED);
                                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
                                    params.setMargins(10, 10, 10, 10);
                                    potionFrame.setLayoutParams(params);
                                    potionFrame.setBackgroundResource(R.drawable.item_frame);

                                    ImageView potionImage = new ImageView(requireContext());
                                    int imageSize = (int) (200 * 0.5);
                                    FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                                            imageSize, imageSize
                                    );
                                    imageParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                                    imageParams.topMargin = 16;
                                    potionImage.setLayoutParams(imageParams);

                                    String base64 = potion.getBase64();

                                    if (base64 != null && !base64.isEmpty()) {
                                        try {
                                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                            potionImage.setImageBitmap(bitmap);
                                        } catch (Exception e) {
                                            potionImage.setImageResource(R.drawable.no_item);
                                        }
                                    } else {
                                        potionImage.setImageResource(R.drawable.no_item);
                                    }

                                    TextView nameText = new TextView(requireContext());
                                    FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            FrameLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    textParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                                    textParams.bottomMargin = 16;
                                    nameText.setMaxWidth(230);
                                    nameText.setLayoutParams(textParams);
                                    nameText.setEllipsize(null);
                                    nameText.setSingleLine(false);
                                    nameText.setMaxLines(3);
                                    nameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    nameText.setText(potion.getName());
                                    nameText.setTextColor(Color.WHITE);
                                    nameText.setTextSize(14);
                                    nameText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

                                    potionFrame.setOnClickListener(v -> {
                                        boolean isRemoving = selectedPotions.contains(potion);

                                        if (vibrator != null && vibrator.hasVibrator()) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                VibrationEffect effect = VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE);
                                                vibrator.vibrate(effect);
                                            } else {
                                                vibrator.vibrate(40);
                                            }
                                        }

                                        if (isRemoving) {
                                            selectedPotions.remove(potion);
                                            potionFrame.setBackgroundResource(R.drawable.item_frame);
                                        } else {
                                            selectedPotions.add(potion);
                                            potionFrame.setBackgroundResource(R.drawable.item_frame_selected);
                                        }
                                    });

                                    potionFrame.addView(potionImage);
                                    potionFrame.addView(nameText);

                                    loadoutGrid.addView(potionFrame);
                                }
                            });
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
