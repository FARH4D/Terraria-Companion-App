package com.example.terrariacompanion;

import android.graphics.Bitmap;

public class ItemData {
    private String name;
    private int id;
    private Bitmap image;

    public ItemData(String name, int id, Bitmap image) {
        this.name = name;
        this.id = id;
        this.image = image;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public Bitmap getImage() { return image; }
}