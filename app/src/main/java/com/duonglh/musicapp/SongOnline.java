package com.duonglh.musicapp;

import android.graphics.Bitmap;

public class SongOnline {
    private String nameAuthor, nameSong, urlSong;
    private Bitmap image;
    private boolean isDownloaded;
    public SongOnline(String nameSong, String nameAuthor,  Bitmap image, String urlSong, boolean isDownloaded) {
        this.nameAuthor = nameAuthor;
        this.nameSong = nameSong;
        this.image = image;
        this.urlSong = urlSong;
        this.isDownloaded = isDownloaded;
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

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }
}
