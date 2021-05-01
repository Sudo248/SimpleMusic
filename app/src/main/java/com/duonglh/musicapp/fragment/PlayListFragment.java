package com.duonglh.musicapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.duonglh.musicapp.Interface;
import com.duonglh.musicapp.MainActivity;
import com.duonglh.musicapp.R;
import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongAdapter;
import com.duonglh.musicapp.model.Song.SongDataBase;
import com.duonglh.musicapp.viewmodels.PlayListViewModel;

import java.io.File;
import java.util.List;

public class PlayListFragment extends Fragment implements Interface.ResponseSearch {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private View mainView;
    private MainActivity mainActivity;
    private PlayListViewModel model;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        model = new ViewModelProvider(this).get(PlayListViewModel.class);
        //Ham nay se chay dc dau tien va nen khoi tao nhung gia tri lien quan den bien quan trong cua fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_play_list, container, false);
        return mainView;
    }

    /**
     * https://developer.android.com/guide/fragments/lifecycle
     *
     * function nay thuc hien do view cho nguoi dung thay
     * */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapping();
        displayListSongs();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mainActivity.isDownloaded){
            if(songAdapter!=null){
                songAdapter.setData(getContext(), Mp3File.getInstance().getListSong());
                songAdapter.notifyDataSetChanged();
            }
            if(mainActivity != null){
                mainActivity.hideKeyBoard();
            }
            assert mainActivity != null;
            mainActivity.isDownloaded = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void mapping() {
        recyclerView = mainView.findViewById(R.id.listViewSongs);
    }

    private void displayListSongs() {
        songAdapter = new SongAdapter(new Interface.IsClickFavorite() {
            @Override
            public void update(Song song) {
                SongDataBase.getInstance(getContext()).songDAO().updateSong(song);
            }
        }, new Interface.IsOnClickItem() {
            @Override
            public void click(int position) {
                mainActivity.StartPlayingActivity(position, true);
            }
        });

       // List<Song> listSongs = Mp3File.getInstance().getListSong();

        final Observer<List<Song>> songObserver = new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                songAdapter.setData(getContext(),songs);
            }
        };

        model.getCurrentListSong().observe(this, songObserver);
       // songAdapter.setData(getContext(),listSongs);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        // KO DC NHE HIEN TAI NO DANG CHAY TREN LUONG CHINH
        //songAdapter.setData(getContext(), Mp3File.getInstance().getListSong());

        recyclerView.setAdapter(songAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteSongDialog(Gravity.CENTER_VERTICAL, position);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    public void response(String textSearch) {
        if(songAdapter != null){
            songAdapter.getFilter().filter(textSearch);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showDeleteSongDialog(int gravity, int position){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_song);

        Window window = dialog.getWindow();
        if(window == null) return;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(gravity == Gravity.CENTER_VERTICAL ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }

        TextView titleDialog = dialog.findViewById(R.id.titleDialog);
        ImageButton agreeButton = dialog.findViewById(R.id.agreeButton);
        ImageButton cancelButton = dialog.findViewById(R.id.cancelButton);

        titleDialog.setText("Delete " + Mp3File.getInstance().getListSong().get(position).getNameSong());

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSong(position);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void deleteSong(int position){
        Song song = Mp3File.getInstance().getListSong().get(position);
        Mp3File.getInstance().getListSong().remove(position);
        mainActivity.musicService.removeSong(position);
        songAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SongDataBase.getInstance(requireContext()).songDAO().deleteSong(song);
                new File(song.getPath()).delete();
            }
        }).start();
        Toast t = Toast.makeText(requireContext(),"Delete "+ song.getNameSong() + " success",Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,50);
        t.show();
    }

}