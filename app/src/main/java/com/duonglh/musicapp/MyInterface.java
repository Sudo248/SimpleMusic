package com.duonglh.musicapp;

import android.view.KeyEvent;

public class MyInterface {

    public interface UpdateView {
        void update();
    }

    public interface MediaPlayerAction {
        void play();
        void nextSong();
        void previousSong();

    }

    public interface RequestSearch{
        String requestSearch();
    }
    public interface ResponseSearch{
        void response(String textSearch);
    }

    public interface UpdateDownloadBar {
        void setProcess(int process);
        void setMax(int max);
    }

    public interface onKeyDown {
        boolean press();
    }
}
