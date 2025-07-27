package com.farh4d.terrariacompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

public class ConnectFragment extends Fragment {

    private int trackedItemInt;
    private boolean buttonCooldown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connect_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SoundManager.init(getContext());
        ImageButton helpButton = view.findViewById(R.id.help_button);

        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        Button connect_button = view.findViewById(R.id.connect_button);
        EditText ip_form = view.findViewById(R.id.ip_form);

        connect_button.setOnClickListener(v -> {
            if (buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 500); // 500ms cooldown for pressing buttons to prevent spamming

            SoundManager.playClick();
            String input_text = ip_form.getText().toString().trim();
            if (input_text.contains(":")) {
                String[] parts = input_text.split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    try {
                        int port = Integer.parseInt(parts[1]);

                        new Thread(() -> {
                            SocketManager socketManager = SocketManagerSingleton.getInstance();
                            try {
                                if (socketManager.connect(ip, port)) {
                                    SocketManagerSingleton.setInstance(socketManager);

                                    SharedPreferences prefs = requireActivity().getSharedPreferences("TrackedItemPrefs", Context.MODE_PRIVATE);
                                    trackedItemInt = prefs.getInt("tracked_item_id", 1);

                                    socketManager.setCurrent_page("HOME");
                                    socketManager.sendMessage("HOME:" + trackedItemInt + ":null");

                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show();

                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new HomeFragment()).commit();
                                    });
                                } else {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(),
                                                    "Failed to connect to server. Try double checking your IP and Port.", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show());
                            }
                        }).start();

                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid Port Number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid format, must use IP:Port", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Invalid format, must use IP:Port", Toast.LENGTH_SHORT).show();
            }
        });

        helpButton.setOnClickListener(v -> {
            requireActivity().runOnUiThread(() -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HelpFragment()).commit();
            });
        });
    }
}