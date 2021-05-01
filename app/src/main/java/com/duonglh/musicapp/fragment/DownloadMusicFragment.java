package com.duonglh.musicapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.duonglh.musicapp.Interface;
import com.duonglh.musicapp.MainActivity;
import com.duonglh.musicapp.R;
import com.duonglh.musicapp.model.SongOnline.SongOnline;
import com.duonglh.musicapp.model.SongOnline.SongOnlineAdapter;
import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongDataBase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DownloadMusicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadMusicFragment extends Fragment implements Interface.ResponseSearch {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button searchButton;
    private ProgressBar downloadBar;
    private MainActivity mainActivity;
    private String Url;
    private View mainView;
    private List<SongOnline> listSongOnline;
    private Context mainContext;
    private RecyclerView recyclerView;
    private SongOnlineAdapter songOnlineAdapter;
    private SongOnline songDownload;
    private ProgressBar loadSongOnline;
    private LinearLayout iconGroup;
    private boolean isDownloading = false;
    private int positionDownload;

    public DownloadMusicFragment() {
        // Required empty public constructor
    }

    public interface UpdateDownloadBar {
        void setProcess(int process);
        void setMax(int max);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DownloadMusic.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadMusicFragment newInstance(String param1, String param2) {
        DownloadMusicFragment fragment = new DownloadMusicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mainContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mainActivity = (MainActivity)getActivity();
        assert mainActivity != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment;
         mainView = inflater.inflate(R.layout.fragment_download_music, container, false);
        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("Duong", "onActivityCreated");
        mapping();
        prepareUI();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
//        if(listSongOnline != null ) {
//            listSongOnline.clear();
//            songOnlineAdapter.notifyDataSetChanged();
//            iconGroup.setVisibility(View.VISIBLE);
//        }
    }

    private void mapping(){
        searchButton    = mainView.findViewById(R.id.searchOnlineButton);
        downloadBar     = mainView.findViewById(R.id.downloadBar);
        recyclerView    = mainView.findViewById(R.id.rev_songOnline);
        loadSongOnline  = mainView.findViewById(R.id.loadSongOnline);
        iconGroup       = mainView.findViewById(R.id.icon_group);
    }

    private void prepareUI(){

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Url == null || Url.equals("")){
                    Toast t = Toast.makeText(getContext(),"Nhập tên bài hát", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP, 0,0);
                    t.show();
                }
                else {
                    String urlSearch = "https://tainhac123.com/tim-kiem/"+Url.trim();
                    new ShowAllSong().execute(urlSearch);
                }
                mainActivity.hideKeyBoard();
            }
        });

        songOnlineAdapter = new SongOnlineAdapter(mainContext, new Interface.IsOnClickItem() {
            @Override
            public void click(int position) {
                positionDownload = position;
                List<Song> listSong = Mp3File.getInstance().getListSong();
                int size = listSong.size();
                for(int i=0;i<size;i++){
                    Song song = listSong.get(i);
                    SongOnline songOnline = listSongOnline.get(position);
                    if(song.getNameSong().equals(songOnline.getNameSong()) && song.getNameAuthor().equals(songOnline.getNameAuthor())){
                        mainActivity.StartPlayingActivity(i, true);
                        return;
                    }
                }
            }
        }, new SongOnlineAdapter.Download() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void start(int position) {
                if (isDownloading) {
                    Toast t = Toast.makeText(mainContext, "Downloading...", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP,50,50);
                    t.show();
                } else {
                    isDownloading = true;
                    songDownload = listSongOnline.get(position);
                    downloadBar.setVisibility(View.VISIBLE);
                    downloadBar.setProgress(0);
                    if (Mp3File.getInstance().noSong(songDownload.getNameSong(), songDownload.getNameAuthor())) {
                        new Download(new UpdateDownloadBar() {
                            @Override
                            public void setProcess(int process) {
                                downloadBar.setProgress(process);
                            }
                            @Override
                            public void setMax(int max) {
                                downloadBar.setMax(max);
                            }
                        }).execute();
                    }
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainContext, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        if(listSongOnline != null && !listSongOnline.isEmpty()){
            displaySongOnline();
        }
        else{
            iconGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void response(String textSearch) {
        Url = textSearch;
    }

    @SuppressLint("StaticFieldLeak")
    private class ShowAllSong extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            listSongOnline = new ArrayList<>();
            iconGroup.setVisibility(View.GONE);
            loadSongOnline.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... strings) {
            try {

                Document document = Jsoup.connect(strings[0]).timeout(2000).get();
                if(document == null) return null;

                Elements allElements = document.getElementsByClass("detail-thumb thumb");
                if(allElements == null) return  null;

                Elements allSingles   = document.getElementsByClass("single");

                for(int i = 0 ;i < allElements.size(); i++){
                    Element element = allElements.get(i);
                    Element a = element.select("a").first();
                    if(a == null) return null;
                    Element img = a.select("img").first();
                    String urlSong = a.attr("href");
                    String urlImage = img.attr("src");
                    String nameSong = a.attr("title");
                    String nameAuthor = allSingles.get(i).html();
                    try {
                        URL url = new URL(urlImage);
                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        if(Mp3File.getInstance().noSong(nameSong, nameAuthor))
                            listSongOnline.add(new SongOnline(nameSong, nameAuthor, image, urlSong, SongOnline.DownloadStatus.NOT_YET));
                        else listSongOnline.add(new SongOnline(nameSong, nameAuthor, image, urlSong, SongOnline.DownloadStatus.YES));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return "ok";
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadSongOnline.setVisibility(View.GONE);
            if(s == null || listSongOnline.isEmpty()){
                showNotFoundDialog(Gravity.CENTER_VERTICAL);
            }
            else{
                displaySongOnline();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Download extends AsyncTask<String, Void, String>{

        UpdateDownloadBar updateDownloadBar;

        public Download(UpdateDownloadBar updateDownloadBar){
            this.updateDownloadBar = updateDownloadBar;
        }

        private String path;
        private byte[] image = new byte[1024];
        int total = 0;
        int lenghtOfFile;
        @Override
        protected String doInBackground(String... strings) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(songDownload != null){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            songDownload.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
                            image = stream.toByteArray();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            // Download Song
            Document documentSong = null;
            try {
                documentSong = Jsoup.connect(songDownload.getUrlSong()).get();
                Element elementSong = documentSong.getElementById("audio-player-container");
                String URLDownload =  elementSong.attr("data-src");

                URL urlDownloadSong = new URL(URLDownload);
                URLConnection connection = urlDownloadSong.openConnection();
                connection.connect();
                lenghtOfFile = connection.getContentLength();
                byte data[] = new byte[1024];
                int count;
                total = 0;
                if(lenghtOfFile < 1000000) return null;// neu thoi gian nho hon 1p thi k tai
                String dir = Environment.getExternalStoragePublicDirectory("Simple Music").toString();
                path = dir+"/"+songDownload.getNameSong()+".mp3";

                InputStream input = new BufferedInputStream(urlDownloadSong.openStream());
                OutputStream output = new FileOutputStream(path);
                int i = 0;
                updateDownloadBar.setMax(lenghtOfFile);
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                    updateDownloadBar.setProcess(total);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "ok";
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                Toast t = Toast.makeText(mainContext, "The Song invalid", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER_VERTICAL,0,0);
                t.show();
                songDownload.setStatus(SongOnline.DownloadStatus.NO);
                songOnlineAdapter.notifyDataSetChanged();
                downloadBar.setVisibility(View.INVISIBLE);
            }
            else{
                super.onPostExecute(s);
                Log.e("Path: ", path);
                String duration = Mp3File.getInstance().getDurationSong(path);
                Song song = new Song(path,image,songDownload.getNameSong(),songDownload.getNameAuthor(), duration);
                Mp3File.getInstance().addSong(song);
                SongDataBase.getInstance(getContext()).songDAO().insertSong(song);
                downloadBar.setVisibility(View.INVISIBLE);
                songDownload.setStatus(SongOnline.DownloadStatus.YES);
                mainActivity.isDownloaded = true;
                songOnlineAdapter.notifyDataSetChanged();
                isDownloading = false;
                mainActivity.musicService.updateListSong();
                Toast t = Toast.makeText(getContext(), "Download success", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER_VERTICAL,0,0);
                t.show();
            }
        }
    }

    private void displaySongOnline(){
        songOnlineAdapter.setData(listSongOnline);
        recyclerView.setAdapter(songOnlineAdapter);
    }

    @SuppressLint("SetTextI18n")
    private void showNotFoundDialog(int gravity){
        final Dialog dialog = new Dialog(mainContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notfound_music);

        Window window = dialog.getWindow();
        if(window == null) return;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(gravity == Gravity.CENTER_VERTICAL){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }

        Button okButton = dialog.findViewById(R.id.okNotFound);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

}