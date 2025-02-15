package com.example.terrariacompanion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import kotlin.Pair;

public class RecipeFragment extends Fragment {

    private SocketManager socketManager;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recipe_list, container, false);
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


        new Thread(() -> {
            try {
                while (isAdded()) {
                    ServerResponse server_data = socketManager.receiveMessage();
                    if (server_data != null) {
                        List<Pair<String, Integer>> recipe_list = server_data.getRecipeData();
                        System.out.println(recipe_list);
                        if (recipe_list != null) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (getActivity() != null) {
                                    for (Pair<String, Integer> entry : recipe_list) {
                                        //System.out.println(entry.getFirst() + ": " + entry.getSecond());
                                    }
                                }
                                });
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireActivity(), "Disconnected or no response.", Toast.LENGTH_SHORT).show());
                        break;
                    }

                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Connection error.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }
}