package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.WatchHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WatchHistoryAdapter extends RecyclerView.Adapter<WatchHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<WatchHistory> watchHistories;

    public WatchHistoryAdapter(Context context, List<WatchHistory> watchHistories) {
        this.context = context;
        this.watchHistories = watchHistories;
    }

    @NonNull
    @Override
    public WatchHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_watch_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchHistoryAdapter.ViewHolder holder, int position) {
        WatchHistory history = watchHistories.get(position);

        holder.tvName.setText(history.getName());

        // Format ngày
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (history.getWatchedAt() != null) {
            holder.tvWatchedAt.setText("Xem lúc: " + sdf.format(history.getWatchedAt()));
        } else {
            holder.tvWatchedAt.setText("Xem lúc: Không rõ");
        }

        // Hiển thị tiến độ
        long watchedMin = history.getWatchedDuration() / 60;
        long totalMin = history.getTotalDuration() / 60;
        holder.tvProgress.setText("Đã xem: " + watchedMin + " / " + totalMin + " phút");

        // Trạng thái
        holder.tvStatus.setText("Trạng thái: " + history.getStatus());

        // Thiết bị
        holder.tvDevice.setText("Thiết bị: " + history.getDevice());

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(history.getPosterUrl())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(holder.imgPoster);
    }

    @Override
    public int getItemCount() {
        return watchHistories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvName, tvWatchedAt, tvProgress, tvStatus, tvDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvName = itemView.findViewById(R.id.tvMovieName);
            tvWatchedAt = itemView.findViewById(R.id.tvWatchedAt);
            tvProgress = itemView.findViewById(R.id.tvWatchedTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDevice = itemView.findViewById(R.id.tvDevice);
        }
    }
}