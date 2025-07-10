package com.example.terrariacompanion;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private SocketManager socketManager;
    private boolean isReceivingData = false;
    private ProgressBar health_bar;
    private ProgressBar mana_bar;
    private TextView health_status;
    private TextView mana_status;
    private LinearLayout player_names_view;
    private FrameLayout playerFrame;
    private ImageView background;

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
        playerFrame = view.findViewById(R.id.player_frame);
        background = view.findViewById(R.id.main_background);

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
                socketManager.setCurrent_page("CHECKLIST");
                socketManager.sendMessage("CHECKLIST");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null && "No BossChecklist".equals(server_data.getChecklistError())) {
                    socketManager.setCurrent_page("HOME");
                    socketManager.sendMessage("HOME");
                    isReceivingData = true;
                    getData();
                } else {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BossChecklist()).commit();
                }
            }).start();
        });

        view.findViewById(R.id.loadout_frame).setOnClickListener(v -> {
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

                                    setBiomeBackground(background, data.biome);

                                    if (playerFrame.getChildCount() > 0) {
                                        playerFrame.removeAllViews();
                                    }

                                    ImageView avatarView = new ImageView(getContext());
                                    avatarView.setImageResource(R.drawable.avatar_male);

                                    String skinColorHex = data.cosmetics.optString("SkinColor", "#FFFFFF");
                                    int tintColor = Color.parseColor(skinColorHex);

                                    float r = Color.red(tintColor) / 255f;
                                    float g = Color.green(tintColor) / 255f;
                                    float b = Color.blue(tintColor) / 255f;

                                    ColorMatrix matrix = new ColorMatrix(new float[]{
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

                                    String[] layerKeys = new String[] {
                                            "Hair", "HeadArmour", "BodyArmourRightArm", "BodyArmourTorso", "BodyArmourLeftArm", "BodyArmourLeftShoulder", "BodyArmourRightShoulder", "LegArmour"
                                    };

                                    Map<String, Integer> targetHeightsDp = new HashMap<>();
                                    targetHeightsDp.put("Hair", 35);
                                    targetHeightsDp.put("HeadArmour", 470);
                                    targetHeightsDp.put("BodyArmourRightArm", 100);
                                    targetHeightsDp.put("BodyArmourTorso", 800);
                                    targetHeightsDp.put("BodyArmourLeftArm", 100);
                                    targetHeightsDp.put("LegArmour", 800);

                                    Map<String, Integer> topOffsetsDp = new HashMap<>();
                                    topOffsetsDp.put("Hair", 40);
                                    topOffsetsDp.put("HeadArmour", -60);
                                    topOffsetsDp.put("BodyArmourRightArm", 0);
                                    topOffsetsDp.put("BodyArmourTorso", 0);
                                    topOffsetsDp.put("BodyArmourLeftArm", 0);
                                    topOffsetsDp.put("LegArmour", 0);

                                    Map<String, Integer> leftOffsetsDp = new HashMap<>();
                                    leftOffsetsDp.put("Hair", 0);
                                    leftOffsetsDp.put("HeadArmour", -10);
                                    leftOffsetsDp.put("BodyArmourRightArm", 0);
                                    leftOffsetsDp.put("BodyArmourTorso", 0);
                                    leftOffsetsDp.put("LegArmour", 0);

                                    for (String key : layerKeys) {
                                        String base64 = data.cosmetics.optString(key, null);
                                        if (base64 == null) continue;

                                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                        Bitmap originalBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                                        if (originalBitmap != null) {
                                            Bitmap scaledBitmap;
                                            FrameLayout.LayoutParams params2;

                                            if (key.equals("LegArmour")) {
                                                int targetHeightDp = 800;
                                                int targetHeightPx = dpToPx(targetHeightDp);
                                                float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();
                                                int targetWidthPx = (int) (targetHeightPx * aspectRatio);
                                                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, false);

                                                params2 = new FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                                );

                                                params2.gravity = Gravity.TOP | Gravity.START;

                                                params2.topMargin = dpToPx(-60);
                                                params2.leftMargin = dpToPx(-18);
                                            }
                                            else if (key.equals("BodyArmourLeftArm")) {
                                                int targetHeightDp = 300;
                                                int targetHeightPx = dpToPx(targetHeightDp);
                                                float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();
                                                int targetWidthPx = (int) (targetHeightPx * aspectRatio);
                                                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, false);

                                                params2 = new FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                                );

                                                params2.gravity = Gravity.TOP | Gravity.START;

                                                params2.topMargin = dpToPx(-15);
                                                params2.leftMargin = dpToPx(50);
                                            }
                                            else if (key.equals("BodyArmourRightArm")) {
                                                int targetHeightDp = 300;
                                                int targetHeightPx = dpToPx(targetHeightDp);
                                                float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();
                                                int targetWidthPx = (int) (targetHeightPx * aspectRatio);
                                                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, false);

                                                params2 = new FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                                );

                                                params2.gravity = Gravity.TOP | Gravity.START;

                                                params2.topMargin = dpToPx(-15);
                                                params2.leftMargin = dpToPx(65);
                                            }
                                            else {
                                                int targetHeightDp = targetHeightsDp.getOrDefault(key, 30);
                                                int targetHeightPx = dpToPx(targetHeightDp);
                                                float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();
                                                int targetWidthPx = (int) (targetHeightPx * aspectRatio);
                                                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, false);

                                                params2 = new FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                                );
                                                params2.gravity = Gravity.TOP | Gravity.START;
                                                params2.topMargin = dpToPx(topOffsetsDp.getOrDefault(key, 0));
                                                params2.leftMargin = dpToPx(leftOffsetsDp.getOrDefault(key, 0));
                                            }

                                            ImageView layer = new ImageView(getContext());
                                            layer.setImageBitmap(scaledBitmap);

                                            if (key.equals("Hair")) {
                                                String hairColorHex = data.cosmetics.optString("HairColor", "#FFFFFF");
                                                int hairTint = Color.parseColor(hairColorHex);
                                                layer.setColorFilter(new PorterDuffColorFilter(hairTint, PorterDuff.Mode.SRC_ATOP));
                                            }

                                            layer.setLayoutParams(params2);
                                            playerFrame.addView(layer);
                                        }
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
