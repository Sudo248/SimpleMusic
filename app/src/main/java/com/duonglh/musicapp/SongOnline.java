package com.duonglh.musicapp;

import android.graphics.Bitmap;

public class SongOnline {
    private String nameAuthor, nameSong, urlSong;
    private Bitmap image;
    public SongOnline(String nameSong, String nameAuthor,  Bitmap image, String urlSong) {
        this.nameAuthor = nameAuthor;
        this.nameSong = nameSong;
        this.image = image;
        this.urlSong = urlSong;
    }

    public String getNameAuthor() {
        return nameAuthor;
    }

    public void setNameAuthor(String nameAuthor) {
        this.nameAuthor = nameAuthor;
    }

    public String getNameSong() {
        return nameSong;
    }

    public void setNameSong(String nameSong) {
        this.nameSong = nameSong;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getUrlSong() {
        return urlSong;
    }

    public void setUrlSong(String urlSong) {
        this.urlSong = urlSong;
    }
}
