package com.farh4d.terrariacompanion.potions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.bosschecklist.BossChecklist;
import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.client.ToastUtility;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PotionFragment extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private boolean isDeleteMode = false;
    private boolean isEditMode = false;
    private long lastClickTime = 0;
    private static final long CLICK_COOLDOWN_MS = 1000;
    private int trackedItemInt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.potion_loadout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar
        SoundManager.init(getContext());

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            ToastUtility.showToast(requireActivity(), "No active connection!", Toast.LENGTH_SHORT);
            return;
        }

        GridLayout loadoutGrid = view.findViewById(R.id.loadout_grid);

        // NAVBAR CODE ////////////////////////////////////////////
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
        ///////////////////////////////////////////////////////////

        try {
            FileInputStream fis = requireContext().openFileInput("potion_loadouts.json");
            InputStreamReader reader = new InputStreamReader(fis);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, List<PotionEntry>>>() {
            }.getType();
            Map<String, List<PotionEntry>> rawMap = gson.fromJson(reader, type);
            reader.close();

            if (rawMap != null) {
                PotionLoadoutDataManager loadoutMap = new PotionLoadoutDataManager();
                for (Map.Entry<String, List<PotionEntry>> entry : rawMap.entrySet()) {
                    loadoutMap.put(entry.getKey(), entry.getValue());
                }

                for (Map.Entry<String, List<PotionEntry>> entry : loadoutMap.getAll().entrySet()) {
                    String loadoutName = entry.getKey();
                    List<PotionEntry> potions = entry.getValue();

                    FrameLayout loadoutFrame = new FrameLayout(requireContext());
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 250;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.setGravity(Gravity.CENTER_HORIZONTAL);
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED);
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
                    params.setMargins(10, 10, 10, 10);
                    loadoutFrame.setLayoutParams(params);
                    loadoutFrame.setBackgroundResource(R.drawable.item_frame);

                    ImageView potionImage = new ImageView(requireContext());
                    int imageSize = (int) (200 * 0.5);
                    FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                            imageSize, imageSize
                    );
                    imageParams.gravity = Gravity.CENTER;
                    imageParams.leftMargin = 20;
                    potionImage.setLayoutParams(imageParams);

                    if (!potions.isEmpty()) {
                        String base64Image = potions.get(0).getBase64();
                        byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        potionImage.setImageBitmap(bitmap);
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
                    nameText.setLayoutParams(textParams);
                    nameText.setMaxWidth(230);
                    nameText.setEllipsize(null);
                    nameText.setSingleLine(false);
                    nameText.setMaxLines(3);
                    nameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    nameText.setText(loadoutName);
                    nameText.setTextColor(Color.WHITE);
                    nameText.setTextSize(18);
                    nameText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

                    loadoutFrame.addView(potionImage);
                    loadoutFrame.addView(nameText);

                    loadoutFrame.setOnClickListener(v -> {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) return;
                        lastClickTime = currentTime;

                        if (isDeleteMode) {
                            new AlertDialog.Builder(requireContext()).setTitle("Delete Loadout").setMessage("Are you sure you want to delete \"" + loadoutName + "\"?")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        loadoutMap.loadFromJson(requireContext());
                                        loadoutMap.deleteLoadout(loadoutName);
                                        loadoutMap.saveToJson(requireContext());

                                        loadoutGrid.removeView(loadoutFrame);

                                        ToastUtility.showToast(requireContext(), "Deleted " +loadoutName, Toast.LENGTH_SHORT);
                                        requireActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
                                    }).setNegativeButton("Cancel", null).show();
                        } else if (isEditMode) {
                            EditPotionFragment editFragment = new EditPotionFragment(loadoutName);
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editFragment).addToBackStack(null).commit();
                        } else {
                            SoundManager.playDrink();
                            Map<String, List<PotionEntryData>> strippedLoadout = loadoutMap.getLoadoutsWithoutBase64();
                            List<PotionEntryData> selectedStrippedLoadout = strippedLoadout.get(loadoutName);

                            String json = gson.toJson(selectedStrippedLoadout);
                            String encoded = Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

                            new Thread(() -> {
                                socketManager.sendMessage("USELOADOUT_BASE64:" + encoded);
                            }).start();
                        }
                    });
                    loadoutGrid.addView(loadoutFrame);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.new_button).setOnClickListener(v -> {
            new Thread(() -> {
                SoundManager.playClick();
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.setCurrent_page("CREATEPOTION");
                socketManager.flushSocket();
                if (isAdded()) {
                    CreatePotionFragment createPotionFragment = new CreatePotionFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, createPotionFragment).commit();
                }
            }).start();
        });

        Button deleteButton = view.findViewById(R.id.delete_button);
        Button editButton = view.findViewById(R.id.edit_button);
        deleteButton.setOnClickListener(v -> {
            isDeleteMode = !isDeleteMode;
            SoundManager.playClick();

            if (isDeleteMode) {
                isEditMode = false;
                editButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#185502")));
                deleteButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B00020")));
            } else {
                deleteButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#185502")));
            }

            ToastUtility.showToast(requireContext(), isDeleteMode ? "Tap a loadout to delete it" : "Delete mode off", Toast.LENGTH_SHORT);
        });

        editButton.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            SoundManager.playClick();

            if (isEditMode) {
                isDeleteMode = false;
                deleteButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#185502")));
                editButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B00020")));
            } else {
                editButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#185502")));
            }

            ToastUtility.showToast(requireContext(), isEditMode ? "Tap a loadout to edit it" : "Edit mode off", Toast.LENGTH_SHORT);
        });
    }
}
