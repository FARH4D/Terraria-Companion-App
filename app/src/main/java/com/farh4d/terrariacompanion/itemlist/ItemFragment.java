package com.farh4d.terrariacompanion.itemlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ItemFragment extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private int currentNum;
    private String category;
    private String search;
    private GridLayout gridLayout;
    private EditText searchBar;
    private int trackedItemInt;
    private boolean isReceivingData = false;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_list, container, false);
        setupCategoryClickListeners(view);

        searchBar = view.findViewById(R.id.search_bar);

        if (getArguments() != null) {
            currentNum = getArguments().getInt("currentNum");
            category = getArguments().getString("category");
            search = getArguments().getString("search");
            searchBar.setText(search);
        }
        gridLayout = view.findViewById(R.id.recipe_grid);
        return view;
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
        socketManager.setAgain(false);

        getData();

        Button nextButton = requireView().findViewById(R.id.right_button);
        nextButton.setOnClickListener(v -> {
            if (!socketManager.getAgain()) {
                SoundManager.playClick();
                currentNum = currentNum + 30;
                getData();
            }
        });

        Button backButton = requireView().findViewById(R.id.left_button);
        backButton.setOnClickListener(v -> {
            if (currentNum - 30 < 30) currentNum = 30;
            else {
                SoundManager.playClick();
                socketManager.setAgain(false);
                currentNum = currentNum - 30;
                getData();
            }
        });

        Button searchButton = requireView().findViewById(R.id.center_button);
        searchButton.setOnClickListener(v -> {
            SoundManager.playClick();
            search = searchBar.getText().toString().trim();
            socketManager.setAgain(false);
            currentNum = 30;
            getData();
        });

        ImageButton clearButton = requireView().findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            SoundManager.playClick();
            searchBar.setText("");
            search = "";
            currentNum = 30;
            getData();
        });

    }

    private void setupCategoryClickListeners(View rootView) {
        ViewGroup parentLayout = rootView.findViewById(R.id.recipe_cats);

        if (parentLayout.getChildCount() > 0) {
            ViewGroup scrollView = (ViewGroup) parentLayout.getChildAt(0);

            if (scrollView.getChildCount() > 0) {
                ViewGroup linearLayout = (ViewGroup) scrollView.getChildAt(0);

                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View view = linearLayout.getChildAt(i);
                    if (view instanceof ImageView) {
                        view.setOnClickListener(v -> {
                            SoundManager.playClick();
                            String resourceName = getResources().getResourceEntryName(v.getId());
                            String tempCategory = resourceName.replace("cats_", "");

                            if (category.equals(tempCategory) ) {
                                category = "all";
                                search = searchBar.getText().toString().trim();
                                currentNum = 30;
                            } else {
                                category = tempCategory;
                                search = searchBar.getText().toString().trim();
                                currentNum = 30;
                            }
                            getData();
                        });
                    }
                }
            }
        }
    }

    private void getData() {
        if (!isReceivingData) {
            isReceivingData = true;
            socketManager.setStatus("working");
            socketManager.setAgain(false);
            new Thread(() -> {
                try {
                    socketManager.sendMessage("RECIPES:" + currentNum + ":" + category + "," + search);
                    final ServerResponse server_data = socketManager.receiveMessage();
                    if (socketManager.getStatus().equals("MAX")) {
                        currentNum = Math.max(0, currentNum - 30);
                        socketManager.setAgain(true);
                        isReceivingData = false;
                        return;
                    }

                    if (server_data != null) {
                        List<ItemData> recipe_list = server_data.getRecipeData();
                        if (recipe_list != null && !recipe_list.isEmpty()) {
                            if (isAdded() && getActivity() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    if (getActivity() != null) {
                                        requireActivity().runOnUiThread(() -> {
                                            gridLayout.removeAllViews();
                                            gridLayout.invalidate();
                                            gridLayout.requestLayout();
                                        });
                                        for (ItemData entry : recipe_list) {
                                            FrameLayout itemFrame = new FrameLayout(requireContext());
                                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                                            params.width = 0;
                                            params.height = 200;
                                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                                            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                                            params.setMargins(10, 10, 10, 10);
                                            itemFrame.setLayoutParams(params);
                                            itemFrame.setBackgroundResource(R.drawable.item_frame);

                                            int itemID = entry.getId();
                                            itemFrame.setTag(itemID);

                                            ImageView imageView = new ImageView(requireContext());
                                            int imageSize = (int) (200 * 0.5);
                                            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                                                    imageSize,
                                                    imageSize
                                            );

                                            imageParams.gravity = Gravity.CENTER;
                                            imageView.setLayoutParams(imageParams);

                                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                            imageView.setAdjustViewBounds(true);

                                            if (entry.getImage() != null) {
                                                imageView.setImageBitmap(entry.getImage());
                                            } else {
                                                imageView.setImageResource(R.drawable.no_item);
                                            }

                                            itemFrame.addView(imageView);
                                            gridLayout.addView(itemFrame);

                                            itemFrame.setOnClickListener(v -> {
                                                new Thread(() -> {
                                                    SoundManager.playClick();
                                                    socketManager.setCurrent_page("ITEMINFO");
                                                    if (isAdded()) {
                                                        ItemInfo itemInfoFragment = new ItemInfo();
                                                        Bundle args = new Bundle();
                                                        args.putInt("itemId", itemID);
                                                        args.putString("category", category);
                                                        args.putInt("currentNum", currentNum);
                                                        args.putString("search", search);
                                                        args.putBoolean("bossChecklist", false);
                                                        Bitmap bitmap = entry.getImage();
                                                        args.putByteArray("bitmap", bitmapToByteArray(bitmap));
                                                        itemInfoFragment.setArguments(args);
                                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container, itemInfoFragment).commit();
                                                    }
                                                }).start();
                                            });
                                            isReceivingData = false;
                                        }
                                    }
                                });
                            }
                        } else {
                            if (isAdded() && getActivity() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    gridLayout.removeAllViews();
                                    gridLayout.invalidate();
                                    gridLayout.requestLayout();

                                    Toast.makeText(requireActivity(), "No items found.", Toast.LENGTH_SHORT).show();
                                });
                            }
                            isReceivingData = false;
                        }
                    }
                    else {
                        isReceivingData = false;
                    }
                } catch (Exception e) {
                    isReceivingData = true;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireActivity(), "Connection error.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}


