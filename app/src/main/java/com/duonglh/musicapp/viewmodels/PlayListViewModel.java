package com.duonglh.musicapp.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;

import java.util.List;

public class PlayListViewModel extends ViewModel {
    private MutableLiveData<List<Song>> currentListSong;
    public MutableLiveData<List<Song>> getCurrentListSong(){
        if (currentListSong == null){
            currentListSong = new MutableLiveData<List<Song>>();
            setValue();
        }
        return currentListSong;
    }



    private void setValue(){
        List<Song> listSongs = Mp3File.getInstance().getListSong();
        currentListSong.setValue(listSongs);
    }
}
