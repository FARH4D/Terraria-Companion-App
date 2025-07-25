package com.farh4d.terrariacompanion.potions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EditPotionFragment extends Fragment {

    private SocketManager socketManager;
    private Set<PotionEntry> selectedPotions = new HashSet<>();
    private String loadoutToEditName;
    private int trackedItemInt;

    public EditPotionFragment(String loadoutName) {
        this.loadoutToEditName = loadoutName;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_potion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        socketManager = SocketManagerSingleton.getInstance();

        if (socketManager == null || !socketManager.isConnected()) {
            Toast.makeText(requireActivity(), "No active connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        GridLayout loadoutGrid = view.findViewById(R.id.loadout_grid);

        // NAVBAR CODE ////////////////////////////////////////////
        view.findViewById(R.id.nav_home).setOnClickListener(v -> {
            new Thread(() -> {
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



        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            new Thread(() -> {
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

        TextView potionTitle = view.findViewById(R.id.potion_title);
        potionTitle.setText("Editing: " + loadoutToEditName);

        PotionLoadoutDataManager loadoutMap = new PotionLoadoutDataManager();
        loadoutMap.loadFromJson(requireContext());
        List<PotionEntry> currentLoadout = loadoutMap.getAll().getOrDefault(loadoutToEditName, new ArrayList<>());
        selectedPotions.addAll(currentLoadout);

        Button saveButton = view.findViewById(R.id.save_button);
        EditText potionNameEntry = view.findViewById(R.id.potionNameEntry);
        potionNameEntry.setText(loadoutToEditName);
        potionNameEntry.setEnabled(false);

        saveButton.setOnClickListener(v -> {
            List<PotionEntry> loadoutList = new ArrayList<>(selectedPotions);
            loadoutMap.loadFromJson(requireContext());
            loadoutMap.put(loadoutToEditName, loadoutList);
            loadoutMap.saveToJson(requireContext());

            if (isAdded()) {
                PotionFragment potionFragment = new PotionFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, potionFragment).commit();
            }
        });

        Set<String> seenPotions = new HashSet<>();
        for (PotionEntry potion : loadoutMap.getAllPotionsFlatList()) {
            String uniqueKey = potion.getMod() + ":" + potion.getInternalName();
            if (!seenPotions.add(uniqueKey)) {
                continue;   // This is to skip duplicates so you can't add more than 1 of the same potion to a loadout when editing
            }

            FrameLayout potionFrame = new FrameLayout(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 250;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            params.setMargins(10, 10, 10, 10);
            potionFrame.setLayoutParams(params);

            String potionKey = potion.getMod() + ":" + potion.getInternalName();
            boolean isSelected = selectedPotions.stream().anyMatch(p -> (p.getMod() + ":" + p.getInternalName()).equals(potionKey));
            // ^ basically turns the user's loadout potions into a stream so they can be iterated, then anyMatch returns true if they match the condition above, program won't be confused between
            // potions in other loadouts and potions in this loadout because it will have a unique identity.
            potionFrame.setBackgroundResource(isSelected ? R.drawable.item_frame_selected : R.drawable.item_frame);

            ImageView potionImage = new ImageView(requireContext());
            int imageSize = (int) (200 * 0.5);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    imageSize, imageSize
            );
            imageParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            imageParams.topMargin = 16;
            potionImage.setLayoutParams(imageParams);

            String base64 = potion.getBase64();

            if (base64 != null && !base64.isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    potionImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    potionImage.setImageResource(R.drawable.no_item);
                }
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
            nameText.setText(potion.getName());
            nameText.setTextColor(Color.WHITE);
            nameText.setTextSize(14);
            nameText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

            potionFrame.setOnClickListener(v -> {
                Optional<PotionEntry> match = selectedPotions.stream().filter(p -> (p.getMod() + ":" + p.getInternalName()).equals(potionKey)).findFirst();

                if (match.isPresent()) {
                    selectedPotions.remove(match.get());
                    potionFrame.setBackgroundResource(R.drawable.item_frame);
                } else {
                    selectedPotions.add(potion);
                    potionFrame.setBackgroundResource(R.drawable.item_frame_selected);
                }
            });

            potionFrame.addView(potionImage);
            potionFrame.addView(nameText);

            loadoutGrid.addView(potionFrame);
        }
    }
}
