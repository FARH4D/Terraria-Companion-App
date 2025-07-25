package com.farh4d.terrariacompanion.homeData;

public class SessionData {
    private static boolean hasBossChecklist = false;
    private static boolean hasBeenSet = false;

    public static void setHasBossChecklist(boolean value) {
        if (!hasBeenSet) {
            hasBossChecklist = value;
            hasBeenSet = true;
        }
    }

    public static boolean hasBossChecklist() {
        return hasBossChecklist;
    }

    public static void resetBossChecklistFlag() {
        hasBeenSet = false;
    }
}