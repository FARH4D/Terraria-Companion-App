package com.farh4d.terrariacompanion.itemlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
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

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.beastiary.BeastiaryFragment;
import com.farh4d.terrariacompanion.bosschecklist.BossChecklist;
import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;
import com.farh4d.terrariacompanion.bosschecklist.BossInfo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemInfo extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private int _itemId;
    private int _currentNum;
    private String _category;
    private String _search;
    private boolean _bossChecklist;
    private int _bossNum;
    private Bitmap _bitmap;
    private List<Map<String, Object>> firstRecipe;
    private String trackedItemName;
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> { canNavigate = true; }, 1300); // Make the user wait a second for everything to load before using navbar
        SoundManager.init(getContext());

        View view = inflater.inflate(R.layout.item_info, container, false);
        if (getArguments() != null) {
            _itemId = getArguments().getInt("itemId");
            _currentNum = getArguments().getInt("currentNum");
            _category = getArguments().getString("category");
            _search = getArguments().getString("search");
            _bossChecklist = getArguments().getBoolean("bossChecklist");
            _bossNum = getArguments().getInt("bossNum");

            byte[] byteArray = getArguments().getByteArray("bitmap");
            if (byteArray != null) {
                _bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            }
        }
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

        TextView itemTitle = view.findViewById(R.id.item_name);
        ImageView itemImage = view.findViewById(R.id.item_image);
        LinearLayout drops_layout = view.findViewById(R.id.drops_layout);

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

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                socketManager.sendMessage("ITEMINFO:" + _itemId + ":" + "null");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    ItemDataManager data = server_data.getItemData();
                    if (data != null) {
                        final ItemDataManager finalData = data;
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {
                                    itemTitle.setText(finalData.name);
                                    itemImage.setImageBitmap(_bitmap);

                                    if (finalData.recipes != null && !finalData.recipes.isEmpty()) {
                                        firstRecipe = finalData.recipes.get(0);
                                    } else {
                                        firstRecipe = new ArrayList<>(); // Becomes null if there are no recipes so it doesn't crash
                                    }
                                    trackedItemName = finalData.name;
                                    trackedItemInt = finalData.id;

                                    for (List<Map<String, Object>> entryList : finalData.recipes) {
                                        LinearLayout dropFrame = new LinearLayout(requireContext());
                                        dropFrame.setOrientation(LinearLayout.VERTICAL);
                                        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        frameParams.setMargins(10, 10, 10, 10);
                                        dropFrame.setLayoutParams(frameParams);
                                        dropFrame.setBackgroundResource(R.drawable.grey_frames);

                                        LinearLayout craftingStationLayout = new LinearLayout(requireContext());
                                        craftingStationLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        LinearLayout.LayoutParams craftingStationParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        craftingStationLayout.setLayoutParams(craftingStationParams);
                                        craftingStationLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                                        LinearLayout ingredientLayout = new LinearLayout(requireContext());
                                        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        LinearLayout.LayoutParams ingredientLayoutParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        ingredientLayout.setLayoutParams(ingredientLayoutParams);

                                        for (Map<String, Object> entry : entryList) {
                                            Integer quantity = entry.containsKey("quantity") && entry.get("quantity") instanceof Integer ? (Integer) entry.get("quantity") : 0;

                                            if (quantity == 0) {
                                                String entryName = (String) entry.get("name");
                                                if (entryName.equals("None")) {
                                                    TextView noStationTextView = new TextView(requireContext());
                                                    noStationTextView.setText("No Crafting Station");
                                                    noStationTextView.setGravity(Gravity.CENTER);
                                                    noStationTextView.setTextSize(25f);
                                                    noStationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                                    ));
                                                    Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                                    noStationTextView.setTypeface(typeface);
                                                    noStationTextView.setPadding(0, 10, 0, 20);
                                                    craftingStationLayout.addView(noStationTextView);
                                                } else {
                                                    String craftingStationImage = (String) entry.get("image");
                                                    ImageView craftingStationImageView = new ImageView(requireContext());
                                                    int craftingImageSize = (int) (300 * 0.7);
                                                    LinearLayout.LayoutParams craftingImageParams = new LinearLayout.LayoutParams(craftingImageSize, craftingImageSize);
                                                    craftingImageParams.gravity = Gravity.CENTER;
                                                    craftingStationImageView.setLayoutParams(craftingImageParams);

                                                    if (craftingStationImage != null && !craftingStationImage.trim().isEmpty()) {
                                                        craftingStationImage = craftingStationImage.trim();
                                                        try {
                                                            byte[] decodedBytes = android.util.Base64.decode(craftingStationImage, Base64.DEFAULT);
                                                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                                            craftingStationImageView.setImageBitmap(bitmap);
                                                        } catch (IllegalArgumentException e) {
                                                            e.printStackTrace();
                                                            craftingStationImageView.setImageResource(R.drawable.no_item);
                                                        }
                                                    } else {
                                                        craftingStationImageView.setImageResource(R.drawable.no_item);
                                                    }
                                                    craftingStationImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                    craftingStationLayout.addView(craftingStationImageView);
                                                }
                                            } else {
                                                String itemName = (String) entry.get("name");
                                                String current_itemImage = (String) entry.get("image");

                                                FrameLayout itemFrame = new FrameLayout(requireContext());
                                                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                                                        150, 150
                                                );
                                                itemParams.setMargins(10, 10, 10, 10);
                                                itemFrame.setLayoutParams(itemParams);
                                                itemFrame.setBackgroundResource(R.drawable.item_frame);

                                                ImageView itemImageView = new ImageView(requireContext());
                                                int itemImageSize = (int) (150 * 0.7);
                                                FrameLayout.LayoutParams itemImageParams = new FrameLayout.LayoutParams(
                                                        itemImageSize, itemImageSize);
                                                itemImageParams.gravity = Gravity.CENTER;
                                                itemImageView.setLayoutParams(itemImageParams);

                                                if (current_itemImage != null) {
                                                    try {
                                                        byte[] decodedBytes = android.util.Base64.decode(current_itemImage, Base64.DEFAULT);
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                                        itemImageView.setImageBitmap(bitmap);
                                                    } catch (IllegalArgumentException e) {
                                                        e.printStackTrace();
                                                        itemImageView.setImageResource(R.drawable.no_item);
                                                    }
                                                } else {
                                                    itemImageView.setImageResource(R.drawable.no_item);
                                                }
                                                itemImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                                TextView itemQuantityView = new TextView(requireContext());
                                                itemQuantityView.setText(String.format(Locale.getDefault(), "%d", quantity != null ? quantity : 0));
                                                itemQuantityView.setTextSize(23);
                                                Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                                itemQuantityView.setTypeface(typeface);
                                                itemQuantityView.setPadding(0, 10, 10, 0);
                                                itemQuantityView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                                                FrameLayout.LayoutParams quantityParams = new FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                                );
                                                quantityParams.gravity = Gravity.BOTTOM | Gravity.END;
                                                itemQuantityView.setLayoutParams(quantityParams);

                                                itemFrame.addView(itemImageView);
                                                itemFrame.addView(itemQuantityView);

                                                ingredientLayout.addView(itemFrame);

                                                itemFrame.setOnClickListener(v -> {
                                                    if (v.getBackground() == null || !(v.getBackground() instanceof ColorDrawable) || ((ColorDrawable) v.getBackground()).getColor() != Color.parseColor("#185502")) {
                                                        v.setBackgroundColor(Color.parseColor("#185502"));
                                                    } else {
                                                        Bundle currentArgs = getArguments();
                                                        if (currentArgs != null) {
                                                            Bundle savedState = new Bundle(currentArgs);
                                                            ItemNavigationStack.itemStack.push(savedState);
                                                        }

                                                        new Thread(() -> {
                                                            SoundManager.playClick();
                                                            socketManager.setCurrent_page("ITEMINFO");
                                                            if (isAdded()) {
                                                                ItemInfo itemInfoFragment = new ItemInfo();
                                                                Bundle args = new Bundle();
                                                                args.putInt("itemId", (int) entry.get("id"));
                                                                args.putString("category", _category);
                                                                args.putInt("currentNum", _currentNum);
                                                                args.putString("search", _search);
                                                                args.putBoolean("bossChecklist", false);
                                                                String base64Image = (String) entry.get("image");
                                                                Bitmap bitmap = base64ToBitmap(base64Image);
                                                                args.putByteArray("bitmap", bitmapToByteArray(bitmap));
                                                                itemInfoFragment.setArguments(args);

                                                                requireActivity().runOnUiThread(() ->
                                                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                                                .replace(R.id.fragment_container, itemInfoFragment).commit()
                                                                );
                                                            }
                                                        }).start();
                                                    }
                                                });
                                            }
                                        }
                                        dropFrame.addView(craftingStationLayout);

                                        dropFrame.addView(ingredientLayout);

                                        drops_layout.addView(dropFrame);
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

        view.findViewById(R.id.track_button).setOnClickListener(btnView -> {
            SoundManager.playClick();
            SharedPreferences prefs = requireActivity().getSharedPreferences("TrackedItemPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("tracked_item_name", trackedItemName);
            editor.putInt("tracked_item_id", trackedItemInt);

            int index = 0;
            for (Map<String, Object> ingredient : firstRecipe) {
                if (ingredient.containsKey("quantity") && ingredient.containsKey("name")) {
                    int qty = (ingredient.get("quantity") instanceof Integer) ? (Integer) ingredient.get("quantity") : 0;

                    if (qty == 0) continue; // Future note: this ensures crafting stations are skipped

                    String name = (String) ingredient.get("name");

                    editor.putString("ingredient_" + index + "_name", name);
                    editor.putInt("ingredient_" + index + "_qty", qty);
                    index++;
                }
            }

            editor.putInt("ingredient_count", index);
            editor.apply();

            Toast.makeText(requireContext(), "Tracking: " + trackedItemName, Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
                SoundManager.playClick();
                if (!ItemNavigationStack.itemStack.isEmpty()) {
                    Bundle previousArgs = ItemNavigationStack.itemStack.pop();

                    socketManager.setCurrent_page("ITEMINFO");

                    ItemInfo itemInfoFragment = new ItemInfo();
                    itemInfoFragment.setArguments(previousArgs);

                    requireActivity().runOnUiThread(() -> requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, itemInfoFragment).commit());
                } else {
                    if (_bossChecklist) {
                        socketManager.setCurrent_page("BOSSINFO");

                        BossInfo bossInfoFragment = new BossInfo();
                        Bundle args = new Bundle();
                        args.putInt("bossNum", _bossNum);
                        bossInfoFragment.setArguments(args);

                        requireActivity().runOnUiThread(() -> requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, bossInfoFragment).commit());
                    }
                    else {
                        socketManager.setCurrent_page("RECIPES");

                        ItemFragment itemFragment = new ItemFragment();
                        Bundle args = new Bundle();
                        args.putInt("currentNum", _currentNum);
                        args.putString("category", _category);
                        args.putString("search", _search);
                        itemFragment.setArguments(args);

                        requireActivity().runOnUiThread(() -> requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, itemFragment).commit());
                    }
                }
            }).start();
        });
    }

    public static Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
