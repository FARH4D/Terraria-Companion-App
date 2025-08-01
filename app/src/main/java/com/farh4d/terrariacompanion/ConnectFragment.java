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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farh4d.terrariacompanion.client.SoundManager;
import com.farh4d.terrariacompanion.client.ToastUtility;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

public class ConnectFragment extends Fragment {

    private int trackedItemInt;
    private boolean buttonCooldown = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connect_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SoundManager.init(getContext());
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ImageButton helpButton = view.findViewById(R.id.help_button);

        SharedPreferences connectionPrefs = requireActivity().getSharedPreferences("ConnectionPrefs", Context.MODE_PRIVATE);
        String lastEntry = connectionPrefs.getString("last_ip_entry", "");

        ((MainActivity) requireActivity()).setFullscreen(false);

        Button connect_button = view.findViewById(R.id.connect_button);
        EditText ip_form = view.findViewById(R.id.ip_form);
        ip_form.setText(lastEntry);

        connect_button.setOnClickListener(v -> {
            if (buttonCooldown) return;

            buttonCooldown = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> { buttonCooldown = false; }, 2000); // 2 second cooldown for pressing buttons to prevent spamming

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
                                    connectionPrefs.edit().putString("last_ip_entry", input_text).apply();

                                    socketManager.setCurrent_page("HOME");
                                    socketManager.sendMessage("HOME:" + trackedItemInt + ":null");

                                    requireActivity().runOnUiThread(() -> {
                                        ToastUtility.showToast(requireContext(), "Connected!", Toast.LENGTH_SHORT);

                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new HomeFragment()).commit();
                                    });
                                } else {
                                    requireActivity().runOnUiThread(() ->
                                            ToastUtility.showToast(requireContext(), "Failed to connect to server. Try double checking the IP and Port", Toast.LENGTH_SHORT));
                                }
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        ToastUtility.showToast(requireContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT));
                            }
                        }).start();

                    } catch (NumberFormatException e) {
                        ToastUtility.showToast(requireContext(), "Invalid Port Number.", Toast.LENGTH_SHORT);
                    }
                } else {
                    ToastUtility.showToast(requireContext(), "Invalid format, must use IP:Port", Toast.LENGTH_SHORT);
                }
            } else {
                ToastUtility.showToast(requireContext(), "Invalid format, must use IP:Port", Toast.LENGTH_SHORT);
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