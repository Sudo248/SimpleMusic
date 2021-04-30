package com.duonglh.musicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongOnlineAdapter extends RecyclerView.Adapter<SongOnlineAdapter.SongOnlineViewHolder>{
    private List<SongOnline> listSongOnline;
    private Context context;

    public SongOnlineAdapter(Context context, Download download) {
        this.download = download;
        this.context = context;
    }

    public interface Download{
        void start(int position);
    }
    private final Download download;

    public void setData(List<SongOnline> listSongOnline){
        this.listSongOnline = listSongOnline;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public SongOnlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_online_information, parent, false);
        return new SongOnlineAdapter.SongOnlineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongOnlineViewHolder holder, int position) {
        final SongOnline songOnline = listSongOnline.get(position);
        if(songOnline == null) return;
        holder.imageViewSongOnline.setImageBitmap(songOnline.getImage());
        holder.textViewNameSongOnline.setText(songOnline.getNameSong());
        holder.textViewNameAuthorOnline.setText(songOnline.getNameAuthor());
        if(songOnline.isDownloaded()){
            holder.imageViewDownload.setImageResource(R.drawable.ic_baseline_download_done_24);
        }

        holder.imageViewDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download.start(position);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Hãy tải về để nghe",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(listSongOnline != null){
            return listSongOnline.size();
        }
        return 0;
    }

    public static class SongOnlineViewHolder extends RecyclerView.ViewHolder{

        private final ImageView imageViewSongOnline, imageViewDownload;
        private final TextView textViewNameSongOnline, textViewNameAuthorOnline;

        public SongOnlineViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSongOnline       = itemView.findViewById(R.id.image_song_online);
            imageViewDownload         = itemView.findViewById(R.id.downloadButton);
            textViewNameSongOnline    = itemView.findViewById(R.id.name_song_online);
            textViewNameAuthorOnline  = itemView.findViewById(R.id.name_author_online);
            textViewNameSongOnline.setSelected(true);
        }
    }

}
