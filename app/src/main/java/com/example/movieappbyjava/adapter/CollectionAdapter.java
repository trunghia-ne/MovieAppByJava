package com.example.movieappbyjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.CollectionFilm;
import android.content.Context;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<CollectionFilm> collections;
    private Context context;

    private ImageButton btnBack;

    public CollectionAdapter(Context context, List<CollectionFilm> collections) {
        this.context = context;
        this.collections = collections;
    }

    @NonNull
    @Override
    public CollectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CollectionAdapter.ViewHolder holder, int position) {
        CollectionFilm collection = collections.get(position);

        holder.tvName.setText(collection.getCollection_name());

        Timestamp createdAt = collection.getCreatedAt();
        if (createdAt != null) {
            Date date = createdAt.toDate();  // Chuyển Timestamp sang Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDesc.setText("Created At: " + sdf.format(date));
        } else {
            holder.tvDesc.setText("");
        }
    }


    @Override
    public int getItemCount() {
        return collections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvName, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgCollectionThumbnail);
            tvName = itemView.findViewById(R.id.tvCollectionName);
            tvDesc = itemView.findViewById(R.id.tvCollectionDesc);
        }
    }
}

