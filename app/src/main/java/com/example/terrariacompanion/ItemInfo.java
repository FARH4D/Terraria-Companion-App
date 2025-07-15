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
import android.widget.FrameLayout;
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
    private String _category;
    private String _search;
    private Bitmap _bitmap;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_info, container, false);
        if (getArguments() != null) {
            _itemId = getArguments().getInt("itemId");
            _currentNum = getArguments().getInt("currentNum");
            _category = getArguments().getString("category");
            _search = getArguments().getString("search");

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

                                    for (List<Map<String, Object>> entryList : finalData.recipes) {
                                        int i = 0;
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
                                                    Toast.makeText(requireContext(), "Recipe clicked " + itemName, Toast.LENGTH_SHORT).show();
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

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
                socketManager.setCurrent_page("RECIPES");
                if (isAdded()) {
                    ItemFragment itemFragment = new ItemFragment();
                    Bundle args = new Bundle();
                    args.putInt("currentNum", _currentNum);
                    args.putString("category", _category);
                    args.putString("search", _search);
                    itemFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, itemFragment).commit();
                }
            }).start();
        });

    }
}
