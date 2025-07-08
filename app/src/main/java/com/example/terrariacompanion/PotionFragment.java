package com.example.terrariacompanion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PotionFragment extends Fragment {

    private SocketManager socketManager;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.potion_loadout, container, false);
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
                socketManager.setCurrent_page("HOME");
                socketManager.sendMessage("HOME");
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment()).commit();
                }
            }).start();
        });
        ///////////////////////////////////////////////////////////

        try {
            FileInputStream fis = requireContext().openFileInput("potion_loadouts.json");
            InputStreamReader reader = new InputStreamReader(fis);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, List<PotionEntry>>>() {
            }.getType();
            Map<String, List<PotionEntry>> rawMap = gson.fromJson(reader, type);
            reader.close();

            if (rawMap != null) {
                PotionLoadoutDataManager loadoutMap = new PotionLoadoutDataManager();
                for (Map.Entry<String, List<PotionEntry>> entry : rawMap.entrySet()) {
                    loadoutMap.put(entry.getKey(), entry.getValue());
                }

                for (Map.Entry<String, List<PotionEntry>> entry : loadoutMap.getAll().entrySet()) {
                    String loadoutName = entry.getKey();
                    List<PotionEntry> potions = entry.getValue();

                    FrameLayout loadoutFrame = new FrameLayout(requireContext());
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED);
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
                    params.setMargins(10, 10, 10, 10);
                    loadoutFrame.setLayoutParams(params);
                    loadoutFrame.setBackgroundResource(R.drawable.item_frame);

                    ImageView potionImage = new ImageView(requireContext());
                    int imageSize = (int) (200 * 0.5);
                    FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                            imageSize, imageSize
                    );
                    imageParams.gravity = Gravity.CENTER;
                    imageParams.leftMargin = 20;
                    potionImage.setLayoutParams(imageParams);

                    if (!potions.isEmpty()) {
                        String base64Image = potions.get(0).getBase64();
                        byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
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
                    textParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                    textParams.rightMargin = 20;
                    nameText.setLayoutParams(textParams);
                    nameText.setText(loadoutName);
                    nameText.setTextColor(Color.WHITE);
                    nameText.setTextSize(18);
                    nameText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.andy_bold));

                    loadoutFrame.addView(potionImage);
                    loadoutFrame.addView(nameText);

                    loadoutGrid.addView(loadoutFrame);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.new_button).setOnClickListener(v -> {
            new Thread(() -> {
                socketManager.flushSocket();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketManager.setCurrent_page("CREATEPOTION");
                socketManager.flushSocket();
                if (isAdded()) {
                    CreatePotionFragment createPotionFragment = new CreatePotionFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, createPotionFragment).commit();
                }
            }).start();
        });
    }
}
