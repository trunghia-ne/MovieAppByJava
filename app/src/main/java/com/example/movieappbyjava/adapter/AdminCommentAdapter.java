package com.example.movieappbyjava.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.Comment;

import java.util.List;

public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.ViewHolder> {

    private List<Comment> comments;
    private final OnToggleHideClickListener toggleHideClickListener;

    public interface OnToggleHideClickListener {
        void onToggleHideClick(String commentId, boolean isHidden);
    }

    public AdminCommentAdapter(List<Comment> comments, OnToggleHideClickListener listener) {
        this.comments = comments;
        this.toggleHideClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        if (comment.isHidden()) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        holder.itemView.setVisibility(View.VISIBLE);

        holder.textUser.setText(comment.getUsername());
        holder.textComment.setText(comment.getComment());
        holder.textMovieTitle.setText(comment.getMovieTitle());

        // Format timestamp
        if (comment.getTimestamp() != null) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    comment.getTimestamp().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.textTime.setText(relativeTime);
        } else {
            holder.textTime.setText("Không có thời gian");
        }

        holder.ratingBar.setRating(comment.getRating() != null ? comment.getRating().floatValue() : 0f);
        Glide.with(holder.itemView.getContext())
                .load(comment.getAvatarUrl())
                .placeholder(R.drawable.ic_placeholder) // Placeholder khi tải ảnh
                .error(R.drawable.ic_error) // Hiển thị khi tải lỗi
                .into(holder.imageAvatar);

        // Update button state based on hidden status
        boolean isHidden = comment.isHidden();
        holder.btnToggleHide.setText(isHidden ? "Hiện" : "Ẩn");
        holder.btnToggleHide.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(
                isHidden ? R.color.orange : R.color.purple));

        holder.btnToggleHide.setOnClickListener(v -> toggleHideClickListener.onToggleHideClick(comment.getId(), isHidden));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textUser, textComment, textTime, textMovieTitle;
        Button btnToggleHide;
        android.widget.RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            textUser = itemView.findViewById(R.id.textUser);
            textComment = itemView.findViewById(R.id.textComment);
            textTime = itemView.findViewById(R.id.textTime);
            textMovieTitle = itemView.findViewById(R.id.textMovieTitle);
            btnToggleHide = itemView.findViewById(R.id.btnToggleHide);
            ratingBar = itemView.findViewById(R.id.ratingUser);
        }
    }
}