package com.example.terrariacompanion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BossInfo extends Fragment {

    private SocketManager socketManager;
    private int _bossNum;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.boss_info, container, false);
        if (getArguments() != null) {
            _bossNum = getArguments().getInt("bossNum");
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

        TextView bossTitle = view.findViewById(R.id.boss_title);
        TextView spawnInfo = view.findViewById(R.id.spawn_info);
        LinearLayout spawnContainer = view.findViewById(R.id.spawn_container);
        ImageView bossImage = view.findViewById(R.id.boss_image);
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
                socketManager.sendMessage("BOSSINFO:" + _bossNum + ":" + "null");
                final ServerResponse server_data = socketManager.receiveMessage();
                if (server_data != null) {
                    BossDataManager data = server_data.getBossData();
                    if (data != null) {
                        final BossDataManager finalData = data;
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() != null) {
                                    bossTitle.setText(finalData.name);
                                    byte[] decodedBytes = Base64.decode(finalData.base64Image, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    bossImage.setImageBitmap(bitmap);

                                    String spawnInfoRaw = finalData.spawnInfo;
                                    Pattern pattern = Pattern.compile(":([A-Za-z0-9+/=]+):");
                                    Matcher matcher = pattern.matcher(spawnInfoRaw);

                                    String base64Image = null;
                                    if (matcher.find()) {
                                        base64Image = matcher.group(1);
                                    }

                                    String cleanedText = spawnInfoRaw.replaceAll(":([A-Za-z0-9+/=]+):", "").trim();
                                    spawnInfo.setText(cleanedText);

                                    if (base64Image != null) {
                                        try {
                                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                            Bitmap imgBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                            if (imgBitmap != null) {
                                                ImageView iconView = new ImageView(requireContext());
                                                iconView.setImageBitmap(imgBitmap);
                                                int size = (int) TypedValue.applyDimension(
                                                        TypedValue.COMPLEX_UNIT_DIP, 32, requireContext().getResources().getDisplayMetrics());

                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                                                params.setMargins(0, 8, 0, 0);
                                                params.gravity = Gravity.CENTER_HORIZONTAL;

                                                iconView.setLayoutParams(params);
                                                spawnContainer.addView(iconView);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

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
                                                byte[] decodedBytes2 = android.util.Base64.decode(entry.image, Base64.DEFAULT);
                                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(decodedBytes2, 0, decodedBytes2.length);
                                                imageView.setImageBitmap(bitmap2);
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
                socketManager.setCurrent_page("CHECKLIST");
                if (isAdded()) {
                    BossChecklist checklist = new BossChecklist();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, checklist).commit();
                }
            }).start();
        });

    }
}
