package com.example.terrariacompanion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemInfo extends Fragment {

    private SocketManager socketManager;
    private int _itemId;
    private int _currentNum;
    private Bitmap _bitmap;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_info, container, false);
        if (getArguments() != null) {
            _itemId = getArguments().getInt("itemId");
            _currentNum = getArguments().getInt("currentNum");
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

        new Thread(() -> {
            try {
                socketManager.sendMessage("ITEMINFO:" + _itemId + ":" + "null");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    DataManager3 data = server_data.getItemData();
                    if (data != null) {
                        final DataManager3 finalData = data;
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {
                                    itemTitle.setText(finalData.name);
                                    itemImage.setImageBitmap(_bitmap);

                                    for (List<Map<String, Object>> entryList : finalData.recipes) {
                                        FrameLayout dropFrame = new FrameLayout(requireContext());
                                        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                200
                                        );
                                        frameParams.setMargins(10, 10, 10, 10);
                                        dropFrame.setLayoutParams(frameParams);
                                        dropFrame.setBackgroundResource(R.drawable.home_frames);

                                        LinearLayout verticalLayout = new LinearLayout(requireContext());
                                        verticalLayout.setOrientation(LinearLayout.VERTICAL);
                                        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));

                                        for (Map<String, Object> entry : entryList) {
                                            String craftingStationImage = (String) entry.get("craftingStationImage");

                                            if (craftingStationImage != null && !craftingStationImage.trim().isEmpty()) {
                                                craftingStationImage = craftingStationImage.trim();
                                                ImageView craftingStationImageView = new ImageView(requireContext());
                                                int craftingImageSize = (int) (200 * 0.7);
                                                LinearLayout.LayoutParams craftingImageParams = new LinearLayout.LayoutParams(craftingImageSize, craftingImageSize);
                                                craftingImageParams.gravity = Gravity.CENTER_HORIZONTAL;
                                                craftingStationImageView.setLayoutParams(craftingImageParams);

                                                try {
                                                    byte[] decodedBytes = android.util.Base64.decode(craftingStationImage, Base64.DEFAULT);
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                                    craftingStationImageView.setImageBitmap(bitmap);
                                                } catch (IllegalArgumentException e) {
                                                    e.printStackTrace();
                                                    craftingStationImageView.setImageResource(R.drawable.no_item);
                                                }

                                                craftingStationImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                verticalLayout.addView(craftingStationImageView);
                                            } else {
                                                ImageView craftingStationImageView = new ImageView(requireContext());
                                                craftingStationImageView.setImageResource(R.drawable.no_item);

                                                int craftingImageSize = (int) (200 * 0.7);
                                                LinearLayout.LayoutParams craftingImageParams = new LinearLayout.LayoutParams(craftingImageSize, craftingImageSize);
                                                craftingImageParams.gravity = Gravity.CENTER_HORIZONTAL;
                                                craftingStationImageView.setLayoutParams(craftingImageParams);

                                                craftingStationImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                verticalLayout.addView(craftingStationImageView);
                                            }
                                        }

                                        LinearLayout itemsLayout = new LinearLayout(requireContext());
                                        itemsLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        itemsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        itemsLayout.setGravity(Gravity.CENTER_VERTICAL);

                                        for (Map<String, Object> itemEntry : entryList) {
                                            String itemName = (String) itemEntry.get("itemName");
                                            String current_itemImage = (String) itemEntry.get("itemImage");
                                            Integer quantity = (Integer) itemEntry.get("quantity");
                                            Double dropRate = (Double) itemEntry.get("dropRate");

                                            LinearLayout itemLayout = new LinearLayout(requireContext());
                                            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                                            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                            ));
                                            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

                                            ImageView itemImageView = new ImageView(requireContext());
                                            int itemImageSize = (int) (150 * 0.7);
                                            LinearLayout.LayoutParams itemImageParams = new LinearLayout.LayoutParams(itemImageSize, itemImageSize);
                                            itemImageParams.gravity = Gravity.START;
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
                                            itemQuantityView.setText(String.format(Locale.getDefault(), "x%d", quantity != null ? quantity : 0));
                                            itemQuantityView.setTextSize(18);
                                            itemQuantityView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                                            LinearLayout.LayoutParams quantityParams = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            );
                                            quantityParams.leftMargin = 10;
                                            itemQuantityView.setLayoutParams(quantityParams);

                                            itemLayout.addView(itemImageView);
                                            itemLayout.addView(itemQuantityView);

                                            itemsLayout.addView(itemLayout);
                                        }

                                        verticalLayout.addView(itemsLayout);
                                        dropFrame.addView(verticalLayout);
                                        drops_layout.addView(dropFrame);

                                        dropFrame.setOnClickListener(v -> {
                                            Toast.makeText(requireContext(), "Recipe clicked", Toast.LENGTH_SHORT).show();
                                        });
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

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
                socketManager.setCurrent_page("BEASTIARY");
                if (isAdded()) {
                    BeastiaryFragment beastiaryFragment = new BeastiaryFragment();
                    Bundle args = new Bundle();
                    args.putInt("currentNum", _currentNum);
                    beastiaryFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, beastiaryFragment).commit();
                }
            }).start();
        });

    }
}
