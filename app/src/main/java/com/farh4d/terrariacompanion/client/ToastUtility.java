package com.farh4d.terrariacompanion.client;

import android.content.Context;
import android.widget.Toast;

public class ToastUtility {
    private static Toast currentToast;

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT); // uses the default duration
    }

    public static void showToast(Context context, String message, int duration) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(context.getApplicationContext(), message, duration);
        currentToast.show();
    }
}
