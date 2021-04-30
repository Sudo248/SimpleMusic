package com.duonglh.musicapp.model.Song;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_song")
public class Song {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private byte[] image;
    private String path;
    private String nameSong;
    private String nameAuthor;
    private String duration;
    private boolean favorite;

    public Song(String path, byte[] image, String nameSong, String nameAuthor, String duration) {
        this.path = path;
        this.image = image;
        if(nameSong == null) this.nameSong = "Unknown";
        else this.nameSong = nameSong;
        if(nameAuthor == null) this.nameAuthor = "<unknown>";
        else this.nameAuthor = nameAuthor;
        handlingDuration(duration);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNameSong() {
        return nameSong;
    }

    public void setNameSong(String nameSong) {
        if(nameSong == null) this.nameSong = "Unknown";
        else this.nameSong = nameSong;
    }

    public String getNameAuthor() {
        return nameAuthor;
    }

    public void setNameAuthor(String nameAuthor) {
        if(nameAuthor == null) this.nameAuthor = "<unknown>";
        else this.nameAuthor = nameAuthor;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        handlingDuration(duration);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    private void handlingDuration(String duration){
        if(duration == null || duration.equals("Nah")){
            this.duration = "Nah";
        }
        else {
            if (duration.contains(":")) {
                this.duration = duration;
            } else {
                int miliseconds = Integer.parseInt(duration);
                int hours = miliseconds / 1000 / 60 / 60;
                int minutes = miliseconds / 1000 / 60 % 60;
                int seconds = miliseconds / 1000 % 60;
                if(hours == 0){
                    this.duration = minutes + ":" + (seconds < 10 ? ("0"+ seconds) : (""+seconds));
                }
                else{
                    this.duration = hours + ":" + (minutes < 10 ? ("0"+ minutes) : (""+minutes)) + ":" + (seconds < 10 ? ("0"+ seconds) : (""+seconds));
                }
            }
        }
    }
}
