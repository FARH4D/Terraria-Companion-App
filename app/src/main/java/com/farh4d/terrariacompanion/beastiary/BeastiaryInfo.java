package com.farh4d.terrariacompanion.beastiary;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.farh4d.terrariacompanion.HomeFragment;
import com.farh4d.terrariacompanion.R;
import com.farh4d.terrariacompanion.server.ServerResponse;
import com.farh4d.terrariacompanion.server.SocketManager;
import com.farh4d.terrariacompanion.server.SocketManagerSingleton;

import java.util.Locale;

public class BeastiaryInfo extends Fragment {

    private SocketManager socketManager;
    private int _npcId;
    private int _currentNum;
    private String _search;
    private Bitmap _bitmap;
    private int trackedItemInt;
    
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beastiary_info, container, false);
        if (getArguments() != null) {
            _npcId = getArguments().getInt("npcId");
            _currentNum = getArguments().getInt("currentNum");
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

        TextView npcTitle = view.findViewById(R.id.npc_name);
        ImageView npcImage = view.findViewById(R.id.npc_image);
        TextView npcHp = view.findViewById(R.id.npc_hp);
        TextView npcDefense = view.findViewById(R.id.npc_defense);
        TextView npcAttack = view.findViewById(R.id.npc_attack);
        TextView npcKnockback = view.findViewById(R.id.npc_knockback);
        LinearLayout drops_layout = view.findViewById(R.id.drops_layout);

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
        ///////////////////////////////////////////////////////////

        new Thread(() -> {
            try {
                socketManager.sendMessage("BEASTIARYINFO:" + _npcId + ":" + "null");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    NpcDataManager data = server_data.getNpcData();
                    if (data != null) {
                        final NpcDataManager finalData = data;
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {

                                    npcTitle.setText(finalData.name);
                                    npcImage.setImageBitmap(_bitmap);
                                    npcHp.setText(String.valueOf(finalData.health));
                                    npcDefense.setText(String.valueOf(finalData.defense));
                                    npcAttack.setText(String.valueOf(finalData.attack));
                                    npcKnockback.setText(finalData.knockback);

                                    for (DropItem entry : finalData.drop_list) {
                                        FrameLayout dropFrame = new FrameLayout(requireContext());
                                        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                200
                                        );
                                        frameParams.setMargins(10, 10, 10, 10);
                                        dropFrame.setLayoutParams(frameParams);
                                        dropFrame.setBackgroundResource(R.drawable.home_frames);

                                        LinearLayout horizontalLayout = new LinearLayout(requireContext());
                                        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                200
                                        ));
                                        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

                                        ImageView imageView = new ImageView(requireContext());
                                        int imageSize = (int) (200 * 0.7); // Scales the image to around 70% of the frame height
                                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                                        imageParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                                        imageParams.leftMargin = 30;
                                        imageView.setLayoutParams(imageParams);

                                        if (entry.image != null) {
                                            try {
                                                byte[] decodedBytes = android.util.Base64.decode(entry.image, Base64.DEFAULT);
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                                imageView.setImageBitmap(bitmap);
                                            } catch (IllegalArgumentException e) {
                                                e.printStackTrace();
                                                imageView.setImageResource(R.drawable.no_item);
                                            }
                                        } else {
                                            imageView.setImageResource(R.drawable.no_item);
                                        }
                                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                        TextView dropRateView = new TextView(requireContext());
                                        dropRateView.setText(String.format(Locale.getDefault(), "Drop Rate: %.2f%%", entry.droprate));
                                        dropRateView.setTextSize(22);
                                        dropRateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                                        dropRateView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

                                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                                        textParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                                        textParams.rightMargin = 30;
                                        dropRateView.setLayoutParams(textParams);

                                        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.andy_bold);
                                        dropRateView.setTypeface(typeface);

                                        horizontalLayout.addView(imageView);
                                        horizontalLayout.addView(dropRateView);
                                        dropFrame.addView(horizontalLayout);
                                        drops_layout.addView(dropFrame);

                                        dropFrame.setOnClickListener(v -> {
                                            Toast.makeText(requireContext(), entry.name, Toast.LENGTH_SHORT).show();
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
                    args.putString("search", _search);
                    beastiaryFragment.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, beastiaryFragment).commit();
                }
            }).start();
        });
    }
}
