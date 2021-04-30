package com.duonglh.musicapp.model.Song;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> implements Filterable {

    private List<Song> listSong = null;
    private List<Song> listSongsOld = null;
    private Context context;
    private boolean isSearching = false;
    public void setData(Context context, List<Song> listSongs) {
        this.context = context;
        this.listSong = listSongs;
        this.listSongsOld = listSongs;
        notifyDataSetChanged();
    }

    public interface IsClickFavorite{
        void updateFavorite(Song song);
    }
    public interface IsOnClickItem{
        void onClickItem(int position);
    }
    private final IsClickFavorite isClickFavorite;
    private final IsOnClickItem isOnClickItem;

    public SongAdapter(IsClickFavorite isClickFavorite, IsOnClickItem isOnClickItem) {
        this.isClickFavorite = isClickFavorite;
        this.isOnClickItem = isOnClickItem;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_information, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        final Song song = listSong.get(position);
        if(song == null){
            return;
        }
        if(song.getImage() != null){
            Glide.with(context).asBitmap()
                    .load(song.getImage())
                    .into(holder.imageViewSong);
        }
        else{
            holder.imageViewSong.setImageResource(R.drawable.avatar);
        }
        holder.textViewNameSong.setText(song.getNameSong());
        holder.textViewNameAuthor.setText(song.getNameAuthor());
        holder.textViewTime.setText(String.valueOf(song.getDuration()));

        if(song.isFavorite()) holder.imageViewStart.setImageResource(R.drawable.ic_baseline_star_32);
        else holder.imageViewStart.setImageResource(R.drawable.ic_baseline_star_border_32);

        holder.imageViewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(song.isFavorite()){
                    song.setFavorite(false);
                    holder.imageViewStart.setImageResource(R.drawable.ic_baseline_star_border_32);
                }
                else{
                    song.setFavorite(true);
                    holder.imageViewStart.setImageResource(R.drawable.ic_baseline_star_32);
                }
                isClickFavorite.updateFavorite(song);// update vao database
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                if(isSearching){
                    int id = listSong.get(position).getId();
                    for(int i=0; i < listSongsOld.size(); i++){
                        if(listSongsOld.get(i).getId() == id){
                            isOnClickItem.onClickItem(i);
                            return;
                        }
                    }
                }
                else isOnClickItem.onClickItem(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(listSong != null){
            return listSong.size();
        }
        return 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder{

        private final ImageView imageViewSong, imageViewStart;
        private final TextView textViewNameSong, textViewNameAuthor, textViewTime;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSong       = itemView.findViewById(R.id.image_song);
            imageViewStart      = itemView.findViewById(R.id.star);
            textViewNameSong    = itemView.findViewById(R.id.name_song);
            textViewNameAuthor  = itemView.findViewById(R.id.name_author);
            textViewTime        = itemView.findViewById(R.id.time);
            textViewNameSong.setSelected(true);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String keySearch = constraint.toString();
                if(keySearch.isEmpty()){
                    isSearching = false;
                    listSong = listSongsOld;
                }
                else{
                    List<Song> list = new ArrayList<>();
                    isSearching = true;
                    for(Song song:listSongsOld){
                        if(song.getNameSong().toLowerCase().contains(keySearch.toLowerCase()) ||
                           song.getNameAuthor().toLowerCase().contains(keySearch.toLowerCase())){
                            list.add(song);
                        }
                    }
                    listSong = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listSong;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listSong = (ArrayList<Song>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
