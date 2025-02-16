package com.example.terrariacompanion;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import kotlin.Pair;

public class RecipeFragment extends Fragment {

    private SocketManager socketManager;
    private int currentNum = 30;
    private GridLayout gridLayout;
    private boolean isReceivingData = false;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recipe_list, container, false);

        gridLayout = view.findViewById(R.id.recipe_grid);
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

        getData(currentNum);


        Button nextButton = requireView().findViewById(R.id.right_button);
        nextButton.setOnClickListener(v -> {
            currentNum = currentNum + 30;
            getData(currentNum);

        });

        Button backButton = requireView().findViewById(R.id.left_button);
        backButton.setOnClickListener(v -> {
            currentNum = currentNum - 30;
            getData(currentNum);

        });

    }

    private void getData(int currentNum) {
        if (!isReceivingData) {
            isReceivingData = true;
            new Thread(() -> {
                try {
                    socketManager.sendMessage("RECIPES:" + currentNum);
                    final ServerResponse server_data = socketManager.receiveMessage();
                    if (server_data != null) {
                        List<ItemData> recipe_list = server_data.getRecipeData();
                        if (recipe_list != null && !recipe_list.isEmpty()) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (getActivity() != null) {
                                        requireActivity().runOnUiThread(() -> {
                                            gridLayout.removeAllViews();
                                            gridLayout.invalidate();
                                            gridLayout.requestLayout();  // Ensures the layout is re-measured and redrawn
                                        });
                                        for (ItemData entry : recipe_list) {
                                            FrameLayout itemFrame = new FrameLayout(requireContext());
                                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                                            params.width = 0;
                                            params.height = 200;
                                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Equal width
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
                                                int clickedItemID = (int) v.getTag();
                                                Toast.makeText(getActivity(), "Item ID: " + clickedItemID, Toast.LENGTH_SHORT).show();
                                            });
                                            isReceivingData = false;
                                        }
                                    }
                                });
                            }
                        }
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
}


