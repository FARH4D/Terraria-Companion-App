package com.farh4d.terrariacompanion.beastiary;

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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.MainActivity;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.bosschecklist.BossChecklist;
import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.client.ToastUtility;
import com.farh4d.terrariacompanion.homeData.SessionData;
import com.farh4d.terrariacompanion.itemlist.ItemFragment;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;
import com.farh4d.terrariacompanion.itemlist.ItemData;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class BeastiaryFragment extends Fragment {

    private SocketManager socketManager;
    private boolean canNavigate = false;
    private boolean buttonCooldown = false;
    private int currentNum;
    private String search;
    private GridLayout gridLayout;
    private EditText searchBar;
    private int trackedItemInt;
    private boolean isReceivingData = false;

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
        View view = inflater.inflate(R.layout.beastiary, container, false);

        searchBar = view.findViewById(R.id.search_bar);

        if (getArguments() != null) {
            currentNum = getArguments().getInt("currentNum");
            search = getArguments().getString("search");
            searchBar.setText(search);
        }
        gridLayout = view.findViewById(R.id.beastiary_grid);
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

    private void getData() {
        if (!isReceivingData) {
            isReceivingData = true;
            socketManager.setStatus("working");
            socketManager.setAgain(false);
            new Thread(() -> {
                try {
                    socketManager.sendMessage("BEASTIARY:" + currentNum + ":null," + search);
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

                                            int npcID = entry.getId();
                                            itemFrame.setTag(npcID);

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
                                                    socketManager.setCurrent_page("BEASTIARYINFO");
                                                    if (isAdded()) {
                                                        BeastiaryInfo beastiaryInfoFragment = new BeastiaryInfo();
                                                        Bundle args = new Bundle();
                                                        args.putInt("npcId", npcID);
                                                        args.putInt("currentNum", currentNum);
                                                        args.putString("search", search);
                                                        Bitmap bitmap = entry.getImage();
                                                        args.putByteArray("bitmap", bitmapToByteArray(bitmap));
                                                        beastiaryInfoFragment.setArguments(args);
                                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container, beastiaryInfoFragment).commit();
                                                    }
                                                }).start();
                                            });
                                            isReceivingData = false;
                                        }
                                    }
                                });
                            }
                        } else {
                            isReceivingData = false;
                        }
                    }
                    else {
                        isReceivingData = false;
                    }
                } catch (Exception e) {
                    isReceivingData = true;
                    requireActivity().runOnUiThread(() ->
                            ToastUtility.showToast(requireActivity(), "Connection error.", Toast.LENGTH_SHORT));
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
