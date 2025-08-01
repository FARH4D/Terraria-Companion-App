package com.farh4d.terrariacompanion.bosschecklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.MainActivity;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.beastiary.DropItem;
import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.client.ToastUtility;
import com.farh4d.terrariacompanion.itemlist.ItemData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.itemlist.ItemInfo;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BossInfo extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private int _bossNum;
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
        View view = inflater.inflate(R.layout.boss_info, container, false);
        if (getArguments() != null) {
            _bossNum = getArguments().getInt("bossNum");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).setFullscreen(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar
        SoundManager.init(getContext());

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            ToastUtility.showToast(requireActivity(), "No active connection!", Toast.LENGTH_SHORT);
            return;
        }

        TextView bossTitle = view.findViewById(R.id.boss_title);
        TextView spawnInfo = view.findViewById(R.id.spawn_info);
        LinearLayout spawnContainer = view.findViewById(R.id.spawn_container);
        ImageView bossImage = view.findViewById(R.id.boss_image);
        LinearLayout drops_layout = view.findViewById(R.id.drops_layout);

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
        ///////////////////////////////////////////////////////////

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                socketManager.sendMessage("BOSSINFO:" + _bossNum + ":" + "null");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    BossDataManager data = server_data.getBossData();
                    if (data != null) {
                        final BossDataManager finalData = data;
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {
                                    bossTitle.setText(finalData.name);
                                    byte[] decodedBytes = Base64.decode(finalData.base64Image, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    bossImage.setImageBitmap(bitmap);

                                    String spawnInfoRaw = finalData.spawnInfo;
                                    Pattern pattern = Pattern.compile(":([A-Za-z0-9+/=]+):");
                                    Matcher matcher = pattern.matcher(spawnInfoRaw);

                                    String base64Image = null;
                                    if (matcher.find()) {
                                        base64Image = matcher.group(1);
                                    }

                                    String cleanedText = spawnInfoRaw.replaceAll(":([A-Za-z0-9+/=]+):", "").trim();
                                    spawnInfo.setText(cleanedText);

                                    if (base64Image != null) {
                                        try {
                                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                            Bitmap imgBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                            if (imgBitmap != null) {
                                                FrameLayout itemFrame = new FrameLayout(requireContext());
                                                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                                );
                                                frameParams.gravity = Gravity.CENTER_HORIZONTAL;
                                                frameParams.topMargin = 16;
                                                itemFrame.setLayoutParams(frameParams);
                                                itemFrame.setBackgroundResource(R.drawable.item_frame_selected);

                                                ImageView iconView = new ImageView(requireContext());
                                                int size = (int) TypedValue.applyDimension(
                                                        TypedValue.COMPLEX_UNIT_DIP, 64, requireContext().getResources().getDisplayMetrics());

                                                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(size, size);
                                                imageParams.gravity = Gravity.CENTER;
                                                iconView.setLayoutParams(imageParams);

                                                iconView.setImageBitmap(imgBitmap);
                                                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                iconView.setAdjustViewBounds(true);

                                                itemFrame.addView(iconView);
                                                spawnContainer.addView(itemFrame);

                                                if (finalData.spawnItems != null && !finalData.spawnItems.isEmpty()) {
                                                    ItemData spawnItem = finalData.spawnItems.get(0);
                                                    int itemID = spawnItem.getId();

                                                    itemFrame.setOnClickListener(v -> {
                                                        new Thread(() -> {
                                                            SoundManager.playClick();
                                                            socketManager.setCurrent_page("ITEMINFO");
                                                            if (isAdded()) {
                                                                ItemInfo itemInfoFragment = new ItemInfo();
                                                                Bundle args = new Bundle();
                                                                args.putInt("itemId", itemID);
                                                                args.putString("category", "all");
                                                                args.putInt("currentNum", 30);
                                                                args.putString("search", "");
                                                                args.putBoolean("bossChecklist", true);
                                                                args.putInt("bossNum", _bossNum);
                                                                Bitmap bitmap2 = spawnItem.getImage();
                                                                args.putByteArray("bitmap", bitmapToByteArray(bitmap2));
                                                                itemInfoFragment.setArguments(args);
                                                                requireActivity().runOnUiThread(() ->
                                                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                                                .replace(R.id.fragment_container, itemInfoFragment).commit()
                                                                );
                                                            }
                                                        }).start();
                                                    });
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    for (DropItem entry : finalData.drop_list) {
                                        FrameLayout dropFrame = new FrameLayout(requireContext());
                                        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                200
                                        );
                                        frameParams.setMargins(10, 10, 10, 10);
                                        dropFrame.setLayoutParams(frameParams);
                                        dropFrame.setBackgroundResource(R.drawable.home_frames);

                                        LinearLayout horizontalLayout = new LinearLayout(requireContext());
                                        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                200
                                        ));
                                        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

                                        ImageView imageView = new ImageView(requireContext());
                                        int imageSize = (int) (200 * 0.7); // Scales the image to around 70% of the frame height
                                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                                        imageParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                                        imageParams.leftMargin = 30;
                                        imageView.setLayoutParams(imageParams);

                                        if (entry.image != null) {
                                            try {
                                                byte[] decodedBytes2 = android.util.Base64.decode(entry.image, Base64.DEFAULT);
                                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(decodedBytes2, 0, decodedBytes2.length);
                                                imageView.setImageBitmap(bitmap2);
                                            } catch (IllegalArgumentException e) {
                                                e.printStackTrace();
                                                imageView.setImageResource(R.drawable.no_item);
                                            }
                                        } else {
                                            imageView.setImageResource(R.drawable.no_item);
                                        }
                                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                        TextView dropRateView = new TextView(requireContext());
                                        dropRateView.setText(String.format(Locale.getDefault(), "Drop Rate: %.2f%%", entry.droprate));
                                        dropRateView.setTextSize(22);
                                        dropRateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                                        dropRateView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

                                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                                        textParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                                        textParams.rightMargin = 30;
                                        dropRateView.setLayoutParams(textParams);

                                        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                        dropRateView.setTypeface(typeface);

                                        horizontalLayout.addView(imageView);
                                        horizontalLayout.addView(dropRateView);
                                        dropFrame.addView(horizontalLayout);
                                        drops_layout.addView(dropFrame);

                                        dropFrame.setOnClickListener(v -> {
                                            ToastUtility.showToast(requireContext(), entry.name, Toast.LENGTH_SHORT);
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        ToastUtility.showToast(requireActivity(), "Connection error.", Toast.LENGTH_SHORT));
                e.printStackTrace();
            }
        }).start();

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
                SoundManager.playClick();
                socketManager.setCurrent_page("CHECKLIST");
                if (isAdded()) {
                    BossChecklist checklist = new BossChecklist();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, checklist).commit();
                }
            }).start();
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
