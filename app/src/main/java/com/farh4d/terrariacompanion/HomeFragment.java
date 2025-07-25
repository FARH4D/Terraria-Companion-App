package com.farh4d.terrariacompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Base64;

import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.bosschecklist.BossChecklist;
import com.farh4d.terrariacompanion.homeData.HomeDataManager;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.potions.PotionEntry;
import com.farh4d.terrariacompanion.potions.PotionEntryData;
import com.farh4d.terrariacompanion.potions.PotionFragment;
import com.farh4d.terrariacompanion.potions.PotionLoadoutDataManager;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private SocketManager socketManager;
    private boolean isReceivingData = false;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private ProgressBar health_bar;
    private ProgressBar mana_bar;
    private TextView health_status;
    private TextView mana_status;
    private LinearLayout player_names_view;
    private FrameLayout playerFrame;
    private ImageView background;
    private LinearLayout ingredientContainer;
    private SharedPreferences prefs;
    private String itemName;
    private int ingredientCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);  // Just inflate the layout here, don't do UI interactions yet
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar

        health_bar = view.findViewById(R.id.health_bar);
        mana_bar = view.findViewById(R.id.mana_bar);
        health_status = view.findViewById(R.id.health_status);
        mana_status = view.findViewById(R.id.mana_status);
        player_names_view = view.findViewById(R.id.player_names_view);
        playerFrame = view.findViewById(R.id.player_frame);
        background = view.findViewById(R.id.main_background);
        ingredientContainer = view.findViewById(R.id.ingredient_container);

        prefs = requireActivity().getSharedPreferences("TrackedItemPrefs", Context.MODE_PRIVATE);
        itemName = prefs.getString("tracked_item_name", null);
        ingredientCount = prefs.getInt("ingredient_count", 0);

        LinearLayout potionContainer = view.findViewById(R.id.potion_loadout_container);
        final String[] selectedLoadout = {null};
        final boolean[] isOnCooldown = {false};

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] resetRunnable = new Runnable[1];

        socketManager = SocketManagerSingleton.getInstance();
        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(getActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        ////POTION LOADOUT USAGE FRAME//////////////////////////////////////////////////////
        try {
            FileInputStream fis = requireContext().openFileInput("potion_loadouts.json");
            InputStreamReader reader = new InputStreamReader(fis);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, List<PotionEntry>>>() {
            }.getType();
            Map<String, List<PotionEntry>> rawMap = gson.fromJson(reader, type);
            reader.close();

            if (rawMap != null && !rawMap.isEmpty()) {
                PotionLoadoutDataManager loadoutMap = new PotionLoadoutDataManager();
                for (Map.Entry<String, List<PotionEntry>> entry : rawMap.entrySet()) {
                    loadoutMap.put(entry.getKey(), entry.getValue());
                }

                for (Map.Entry<String, List<PotionEntry>> entry : rawMap.entrySet()) {
                    String loadoutName = entry.getKey();
                    final String nameForUse = loadoutName;
                    List<PotionEntry> potions = entry.getValue();

                    FrameLayout loadoutFrame = new FrameLayout(requireContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.setMargins(10, 10, 10, 10);
                    loadoutFrame.setLayoutParams(params);
                    loadoutFrame.setBackgroundResource(R.drawable.item_frame);
                    loadoutFrame.setTag(loadoutName);

                    ImageView potionImage = new ImageView(requireContext());
                    int imageSize = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 48, requireContext().getResources().getDisplayMetrics());
                    FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(imageSize, imageSize);
                    imageParams.gravity = Gravity.CENTER;
                    potionImage.setLayoutParams(imageParams);

                    if (!potions.isEmpty()) {
                        String base64 = potions.get(0).getBase64();
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
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
                    textParams.bottomMargin = 8;
                    nameText.setLayoutParams(textParams);
                    nameText.setMaxWidth(150);
                    nameText.setEllipsize(null);
                    nameText.setSingleLine(false);
                    nameText.setMaxLines(2);
                    nameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    nameText.setText(loadoutName);
                    nameText.setTextColor(Color.WHITE);
                    nameText.setTextSize(14);
                    nameText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

                    loadoutFrame.addView(potionImage);
                    loadoutFrame.addView(nameText);

                    loadoutFrame.setOnClickListener(v -> {

                        if (isOnCooldown[0]) return;

                        if (selectedLoadout[0] != null && !nameForUse.equals(selectedLoadout[0])) {
                            for (int i = 0; i < potionContainer.getChildCount(); i++) {
                                View child = potionContainer.getChildAt(i);
                                if (child instanceof FrameLayout) {
                                    child.setBackgroundResource(R.drawable.item_frame);
                                }
                            }
                        }

                        if (!nameForUse.equals(selectedLoadout[0])) {
                            selectedLoadout[0] = nameForUse;
                            v.setBackgroundResource(R.drawable.item_frame_selected);

                            if (resetRunnable[0] != null) {
                                handler.removeCallbacks(resetRunnable[0]);
                            }

                            resetRunnable[0] = () -> {
                                v.setBackgroundResource(R.drawable.item_frame);
                                selectedLoadout[0] = null;
                            };
                            handler.postDelayed(resetRunnable[0], 2000);
                        } else {
                            isOnCooldown[0] = true;
                            Map<String, List<PotionEntryData>> strippedLoadout = loadoutMap.getLoadoutsWithoutBase64();
                            List<PotionEntryData> selectedStrippedLoadout = strippedLoadout.get(loadoutName);

                            String json = gson.toJson(selectedStrippedLoadout);
                            String encoded = Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

                            new Thread(() -> {
                                socketManager.sendMessage("USELOADOUT_BASE64:" + encoded);

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                socketManager.sendMessage("HOME");
                            }).start();

                            if (resetRunnable[0] != null) {
                                handler.removeCallbacks(resetRunnable[0]);
                            }

                            resetRunnable[0] = () -> {
                                try {
                                    if (isAdded()) {
                                        v.setBackgroundResource(R.drawable.item_frame);
                                        selectedLoadout[0] = null;
                                        isOnCooldown[0] = false;
                                    }
                                } catch (Exception e) {
                                    Log.e("PotionReset", "Error resetting frame: " + e.getMessage());
                                }
                            };
                            handler.postDelayed(resetRunnable[0], 2000);
                        }
                    });
                    potionContainer.addView(loadoutFrame);
                }
            }
            else {
                TextView emptyText = new TextView(requireContext());
                LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                emptyText.setLayoutParams(emptyParams);
                emptyText.setGravity(Gravity.CENTER);
                emptyText.setText("No Potion Loadouts");
                emptyText.setTextColor(Color.WHITE);
                emptyText.setTextSize(26);
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

                potionContainer.addView(emptyText);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        ////////////////////////////////////////////////////////////

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_recipe).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

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
                isReceivingData = false;
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
                    isReceivingData = true;
                    getData();
                }
            }).start();
        });

        view.findViewById(R.id.loadout_frame).setOnClickListener(v -> {
            if (!canNavigate || buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            new Thread(() -> {
                isReceivingData = false;
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

                                    boolean hasBossChecklist = data.bossChecklist;
                                    SessionData.setHasBossChecklist(hasBossChecklist);

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

                                    // ITEM TRACKING ////////////////////////////////////////////
                                    ingredientContainer.removeAllViews();

                                    if (itemName != null && ingredientCount > 0 && data.trackedItems != null) {
                                        TextView itemTitle = new TextView(requireContext());
                                        itemTitle.setText(itemName);
                                        itemTitle.setTextSize(23f);
                                        itemTitle.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));
                                        itemTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                                        itemTitle.setGravity(Gravity.CENTER);

                                        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        titleParams.setMargins(0, 0, 0, 16);
                                        itemTitle.setLayoutParams(titleParams);
                                        ingredientContainer.addView(itemTitle);

                                        for (int i = 0; i < ingredientCount; i++) {
                                            String ingredientName = prefs.getString("ingredient_" + i + "_name", "Unknown");
                                            int quantity = prefs.getInt("ingredient_" + i + "_qty", 0);
                                            int userHas = (i < data.trackedItems.size()) ? data.trackedItems.get(i) : 0;

                                            String text = "• " + ingredientName + ": ";

                                            SpannableString spannable = new SpannableString(text + userHas + "/" + quantity);
                                            int start = text.length();
                                            int end = spannable.length();

                                            if (userHas >= quantity) {
                                                spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.greenText)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            }

                                            TextView ingredientText = new TextView(requireContext());
                                            ingredientText.setText(spannable);
                                            ingredientText.setTextSize(20f);
                                            ingredientText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));
                                            ingredientText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                                            ingredientText.setPadding(10, 8, 10, 8);

                                            ingredientContainer.addView(ingredientText);
                                        }
                                    }
                                    /////////////////////////////////////////////////////////////////////////////////////
                                    setBiomeBackground(background, data.biome);

                                    if (playerFrame.getChildCount() > 0) {
                                        playerFrame.removeAllViews();
                                    }

                                    //////////////PLAYER SKIN TONE////////////////////////////////////////////////////////
                                    ImageView avatarView = new ImageView(getContext());
                                    avatarView.setImageResource(R.drawable.avatar_male);

                                    String skinColorHex = data.cosmetics.optString("SkinColor", "#FFFFFF");
                                    int tintColor = Color.parseColor(skinColorHex);

                                    float r = Color.red(tintColor) / 255f;
                                    float g = Color.green(tintColor) / 255f;
                                    float b = Color.blue(tintColor) / 255f;

                                    ColorMatrix matrix = new ColorMatrix(new float[]{ // Using this matrix so only the white/grey part of the avatar is tinted, instead of colouring the outline too
                                            r, 0, 0, 0, 0,
                                            0, g, 0, 0, 0,
                                            0, 0, b, 0, 0,
                                            0, 0, 0, 1, 0
                                    });

                                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                                    avatarView.setColorFilter(filter);

                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            Gravity.CENTER
                                    );
                                    avatarView.setLayoutParams(params);
                                    playerFrame.addView(avatarView);
                                /////////////////////////////////////////////////////////////////////////////////////////////////////
                                ///////////////PLAYER ARMOUR RENDERING //////////////////////////////////////////////////////////////
                                    String[] layerKeys = new String[] {
                                            "Eyes1", "Eyes2", "Hair", "BodyArmourTorso", "HeadArmour", "BodyArmourRightArm", "BodyArmourLeftArm", "BodyArmourLeftShoulder", "LegArmour"
                                    };

                                    Map<String, Integer> targetHeightsDp = getStringIntegerMap();

                                    Map<String, Integer> topOffsetsDp = new HashMap<>();
                                    topOffsetsDp.put("HeadArmour", -60);

                                    Map<String, Integer> leftOffsetsDp = new HashMap<>();
                                    leftOffsetsDp.put("HeadArmour", -10);

                                    Map<String, LayerConfig> layerConfigs = new HashMap<>();

                                    layerConfigs.put("Eyes1", new LayerConfig(600, -53, -22));
                                    layerConfigs.put("Eyes2", new LayerConfig(600, -53, -22));
                                    layerConfigs.put("LegArmour", new LayerConfig(800, -60, -18));
                                    layerConfigs.put("BodyArmourTorso", new LayerConfig(1000, -60, -20));
                                    layerConfigs.put("BodyArmourLeftArm", new LayerConfig(300, -15, 50));
                                    layerConfigs.put("BodyArmourLeftShoulder", new LayerConfig(300, -15, 50));
                                    layerConfigs.put("BodyArmourRightArm", new LayerConfig(300, -20, 65));
                                    layerConfigs.put("Hair", new LayerConfig(300, -40, 60));

                                    for (String key : layerKeys) {
                                        String base64 = data.cosmetics.optString(key, null);
                                        if (base64 == null) continue;

                                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                        Bitmap originalBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                        if (originalBitmap == null) continue;

                                        LayerConfig config = layerConfigs.getOrDefault(key,
                                                new LayerConfig(targetHeightsDp.getOrDefault(key, 30), topOffsetsDp.getOrDefault(key, 0), leftOffsetsDp.getOrDefault(key, 0)));

                                        int targetHeightPx = dpToPx(config.targetHeightDp);
                                        float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();
                                        int targetWidthPx = (int) (targetHeightPx * aspectRatio);
                                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, false);

                                        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                                FrameLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        params2.gravity = Gravity.TOP | Gravity.START;
                                        params2.topMargin = dpToPx(config.topMarginDp);
                                        params2.leftMargin = dpToPx(config.leftMarginDp);

                                        ImageView layer = new ImageView(getContext());
                                        layer.setImageBitmap(scaledBitmap);

                                        if (key.equals("Eyes2")) {
                                            String hairColorHex = data.cosmetics.optString("EyeColor", "#FFFFFF");
                                            int hairTint = Color.parseColor(hairColorHex);
                                            layer.setColorFilter(new PorterDuffColorFilter(hairTint, PorterDuff.Mode.SRC_ATOP));
                                        }

                                        if (key.equals("Hair")) {
                                            String hairColorHex = data.cosmetics.optString("HairColor", "#FFFFFF");
                                            int tintColorHair = Color.parseColor(hairColorHex);

                                            float rHair = Color.red(tintColorHair) / 255f;
                                            float gHair = Color.green(tintColorHair) / 255f;
                                            float bHair = Color.blue(tintColorHair) / 255f;

                                            ColorMatrix hairMatrix = new ColorMatrix(new float[]{
                                                    rHair, 0, 0, 0, 0,
                                                    0, gHair, 0, 0, 0,
                                                    0, 0, bHair, 0, 0,
                                                    0, 0, 0, 1, 0
                                            });

                                            ColorMatrixColorFilter hairFilter = new ColorMatrixColorFilter(hairMatrix);
                                            layer.setColorFilter(hairFilter);
                                        }

                                        layer.setLayoutParams(params2);
                                        playerFrame.addView(layer);
                                        /////////////////////////////////////////////////////////////////////////////////////
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

    @NonNull
    private static Map<String, Integer> getStringIntegerMap() {
        Map<String, Integer> targetHeightsDp = new HashMap<>();
        targetHeightsDp.put("Hair", 600);
        targetHeightsDp.put("Eyes1", 700);
        targetHeightsDp.put("Eyes2", 700);
        targetHeightsDp.put("BodyArmourTorso", 800);
        targetHeightsDp.put("HeadArmour", 470);
        targetHeightsDp.put("BodyArmourRightArm", 100);
        targetHeightsDp.put("BodyArmourLeftArm", 100);
        targetHeightsDp.put("BodyArmourLeftShoulder", 100);
        targetHeightsDp.put("LegArmour", 800);
        return targetHeightsDp;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void setBiomeBackground(View background, String biome) {
        int resId;

        switch (biome) {
            case "underworld": resId = R.drawable.underworld; break;
//            case "sky": resId = R.drawable.sky; break;

            case "surface_jungle": resId = R.drawable.surface_jungle; break;
            case "underground_jungle": resId = R.drawable.underground_jungle; break;
            case "cavern_jungle": resId = R.drawable.cavern_jungle; break;

            case "surface_corruption": resId = R.drawable.surface_corruption; break;
            case "underground_corruption": resId = R.drawable.underground_corruption; break;
            case "cavern_corruption": resId = R.drawable.cavern_corruption; break;

            case "surface_crimson": resId = R.drawable.surface_crimson; break;
            case "underground_crimson": resId = R.drawable.underground_crimson; break;
            case "cavern_crimson": resId = R.drawable.cavern_crimson; break;

            case "surface_hallow": resId = R.drawable.surface_hallow; break;
            case "underground_hallow": resId = R.drawable.underground_hallow; break;
            case "cavern_hallow": resId = R.drawable.cavern_hallow; break;

            case "surface_snow": resId = R.drawable.surface_snow; break;
            case "underground_snow": resId = R.drawable.underground_snow; break;
            case "cavern_snow": resId = R.drawable.cavern_snow; break;

            case "surface_desert": resId = R.drawable.surface_desert; break;
            case "underground_desert": resId = R.drawable.underground_desert; break;
            case "cavern_desert": resId = R.drawable.underground_desert; break;

            case "surface_glowing_mushroom": resId = R.drawable.surface_glowing_mushroom; break;
            case "underground_glowing_mushroom": resId = R.drawable.underground_glowing_mushroom; break;
            case "cavern_glowing_mushroom": resId = R.drawable.cavern_glowing_mushroom; break;

            case "underground": resId = R.drawable.underground; break;
            case "cavern": resId = R.drawable.cavern; break;
            case "ocean": resId = R.drawable.ocean; break;
            case "granite": resId = R.drawable.granite; break;
            case "marble": resId = R.drawable.marble; break;
            case "bee_hive": resId = R.drawable.bee_hive; break;

            case "nebula": resId = R.drawable.nebula; break;
            case "solar": resId = R.drawable.solar; break;
            case "vortex": resId = R.drawable.vortex; break;
            case "stardust": resId = R.drawable.stardust; break;

            case "dungeon": resId = R.drawable.dungeon; break;

            default: resId = R.drawable.surface_forest; break;
        }
        Drawable newDrawable = ContextCompat.getDrawable(background.getContext(), resId);
        Drawable currentDrawable = background.getBackground();

        if (currentDrawable == null) {
            background.setBackground(newDrawable);
        } else {
            Drawable[] layers = new Drawable[] { currentDrawable, newDrawable };
            TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
            background.setBackground(transitionDrawable);
            transitionDrawable.startTransition(500);
        }
    }
}

class LayerConfig {
    int targetHeightDp;
    int topMarginDp;
    int leftMarginDp;

    LayerConfig(int height, int top, int left) {
        this.targetHeightDp = height;
        this.topMarginDp = top;
        this.leftMarginDp = left;
    }
}