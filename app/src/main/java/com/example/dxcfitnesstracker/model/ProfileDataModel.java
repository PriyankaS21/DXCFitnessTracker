package com.example.dxcfitnesstracker.model;

public class ProfileDataModel {
    String list_text;
    int image;

    public ProfileDataModel(String list_text, int image) {
        this.list_text = list_text;
        this.image = image;
    }

    public String getListText() {
        return list_text;
    }

    public int getImage() {
        return image;
    }
}
