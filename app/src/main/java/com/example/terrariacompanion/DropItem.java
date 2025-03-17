package com.example.terrariacompanion;

public class DropItem {
    public int id;
    public String name;
    public String image;
    public double droprate;

    public DropItem(int id, String name, String image, double droprate) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.droprate = droprate;
    }
}