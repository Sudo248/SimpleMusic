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

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayListFragment extends Fragment implements Interface.ResponseSearch {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private View mainView;
    private Context mainContext;
    private MainActivity mainActivity;

    public PlayListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayList.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayListFragment newInstance(String param1, String param2) {
        PlayListFragment fragment = new PlayListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainContext = context;
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_play_list, container, false);
        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapping();
        displayListSongs();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mainActivity.isDownloaded){
            if(songAdapter!=null){
                songAdapter.setData(mainContext, Mp3File.getInstance().getListSong());
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
                SongDataBase.getInstance(mainContext).songDAO().updateSong(song);
            }
        }, new Interface.IsOnClickItem() {
            @Override
            public void click(int position) {
                mainActivity.StartPlayingActivity(position, true);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainContext, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        songAdapter.setData(mainContext, Mp3File.getInstance().getListSong());
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
        final Dialog dialog = new Dialog(mainContext);
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
                SongDataBase.getInstance(mainContext).songDAO().deleteSong(song);
                new File(song.getPath()).delete();
            }
        }).start();
        Toast t = Toast.makeText(mainContext,"Delete "+ song.getNameSong() + " success",Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,50);
        t.show();
    }

}