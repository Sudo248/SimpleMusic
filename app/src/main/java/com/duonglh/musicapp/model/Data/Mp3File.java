package com.duonglh.musicapp.model.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.duonglh.musicapp.model.MyMediaPlay;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongDataBase;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Mp3File {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ArrayList<Song> listSong;
    // get Data From Storage
    @RequiresApi(api = Build.VERSION_CODES.R)
    public ArrayList<Song> getAllData(Context context){
        this.context = context;
        // get From DataBase
        listSong = (ArrayList<Song>) SongDataBase.getInstance(context).songDAO().getListSongs();
        listSong.addAll(getFromMediaAudio());
        listSong.addAll(getFromOtherLocation(Environment.getExternalStorageDirectory()));
        listSong = (ArrayList<Song>) listSong.stream().sorted(Comparator.comparing(Song::getNameSong))
                .collect(Collectors.toList());
        MyMediaPlay.listSong = listSong;
        return listSong;

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private ArrayList<Song> getFromMediaAudio(){
        ArrayList<Song> listSongs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        @SuppressLint("Recycle")
        Cursor cursor = context.getContentResolver().query(uri,null, "is_music != 0",null,null);
        if(cursor != null && cursor.moveToFirst()){
            do{

                String path         = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                byte[] image        = null;//getImageSong(path);
                String nameSong     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String nameAuthor   = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                Song song = new Song(path,image,nameSong,nameAuthor, duration);
                if(!hasSong(song)){
                    listSongs.add(song);
                    SongDataBase.getInstance(context).songDAO().insertSong(song);
                }

            }while (cursor.moveToNext());
        }
        return listSongs;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private ArrayList<Song> getFromOtherLocation(File localFile){
        ArrayList<Song> listSongs = new ArrayList<>();

        File[] files = localFile.listFiles();
        if(files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    String nameDirector = singleFile.getName();
                    if(nameDirector.equals("Zing MP3") || nameDirector.equals("Music")) {
                        listSongs.addAll(getFromOtherLocation(singleFile));
                    }
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        Song song = getDataSong(singleFile.toString());
                        if(!hasSong(song)){
                            listSongs.add(song);
                            SongDataBase.getInstance(context).songDAO().insertSong(song);
                        }
                    }
                }
            }
        }
        return listSongs;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean hasSong(@NotNull Song song){
        List<Song> list = listSong.stream()
                .filter(item -> item.getNameSong().equals(song.getNameSong()))
                .collect(Collectors.toList());

        return list.size() > 0;
    }

    private Song getDataSong(String path){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String nameSong     = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String nameAuthor   = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        byte[] image        = retriever.getEmbeddedPicture();
        String duration     = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        return new Song(path, image, nameSong, nameAuthor, duration);
    }

}
