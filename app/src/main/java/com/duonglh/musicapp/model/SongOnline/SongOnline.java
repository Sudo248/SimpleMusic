package com.duonglh.musicapp.model.SongOnline;

import android.graphics.Bitmap;

public class SongOnline {
    private String nameAuthor, nameSong, urlSong;
    private Bitmap image;
    private DownloadStatus status;
    public enum DownloadStatus{
        YES,
        NO,
        NOT_YET
    }
    public SongOnline(String nameSong, String nameAuthor,  Bitmap image, String urlSong, DownloadStatus status) {
        this.nameAuthor = nameAuthor;
        this.nameSong = nameSong;
        this.image = image;
        this.urlSong = urlSong;
        this.status = status;
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

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus downloaded) {
        status = downloaded;
    }
}
