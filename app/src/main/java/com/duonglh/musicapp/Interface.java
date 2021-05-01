package com.duonglh.musicapp;

import com.duonglh.musicapp.model.Song.Song;

public class Interface {

    public interface UpdateView {
        void update();
    }

    public interface IsClickFavorite{
        void update(Song song);
    }

    public interface IsOnClickItem{
        void click(int position);
    }

    public interface MediaPlayerAction {
        void play();
        void nextSong();
        void previousSong();
        void cancel();
    }

    public interface RequestSearch{
        String requestSearch();
    }

    public interface ResponseSearch{
        void response(String textSearch);
    }

    public interface onBackPress {
        boolean press();
    }
}
