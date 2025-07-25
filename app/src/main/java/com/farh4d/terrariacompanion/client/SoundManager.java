package com.farh4d.terrariacompanion.client;

import android.content.Context;
import android.media.SoundPool;

import com.farh4d.terrariacompanion.R;

public class SoundManager {
    private static SoundPool soundPool;
    private static int clickSoundId;
    private static int drinkSoundId;
    private static boolean isLoaded = false;
    public static void init(Context context) {
        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        clickSoundId = soundPool.load(context, R.raw.menu_tick, 1);
        drinkSoundId = soundPool.load(context, R.raw.potion_drink, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) isLoaded = true;
        });
    }

    public static void playClick() {
        if (isLoaded) soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
    }

    public static void playDrink() {
        if (isLoaded) soundPool.play(drinkSoundId, 1, 1, 0, 0, 1);
    }

    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            isLoaded = false;
        }
    }
}
