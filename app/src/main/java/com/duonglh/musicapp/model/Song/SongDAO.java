package com.duonglh.musicapp.model.Song;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDAO {
    @Insert
    void insertSong(Song song);

    @Query("SELECT * FROM songs")
    List<Song> getListSongs();

    @Query("SELECT * FROM songs WHERE nameSong = :nameSong")
    List<Song> getListSongSame(String nameSong);

    @Update
    void updateSong(Song song);

    @Delete
    void deleteSong(Song song);

    @Query("DELETE FROM songs")
    void deleteAllSong();
}
