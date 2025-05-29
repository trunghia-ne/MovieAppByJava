package com.example.movieappbyjava.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final List<Comment> comments;
    private final String currentUserId; // ✅ Thêm biến này

    // ✅ Constructor mới có thêm currentUserId
    public CommentAdapter(List<Comment> comments, String currentUserId) {
        this.comments = comments;
        this.currentUserId = currentUserId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textUser, textComment, textTime;
        RatingBar ratingUser;

        public ViewHolder(View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            textUser = itemView.findViewById(R.id.textUser);
            textComment = itemView.findViewById(R.id.textComment);
            textTime = itemView.findViewById(R.id.textTime);
            ratingUser = itemView.findViewById(R.id.ratingUser);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        // ✅ Đánh dấu "(Bạn)" nếu là người dùng hiện tại
        if (comment.getUserId() != null && comment.getUserId().equals(currentUserId)) {
            holder.textUser.setText(comment.getUsername() + " (Bạn)");
            holder.textUser.setTextColor(Color.parseColor("#2196F3")); // Màu xanh
        } else {
            holder.textUser.setText(comment.getUsername() != null ? comment.getUsername() : "Người dùng ẩn danh");
            holder.textUser.setTextColor(Color.parseColor("#333333")); // Màu đen
        }

        // Set comment text
        holder.textComment.setText(comment.getComment());

        // Set rating
        holder.ratingUser.setRating((float) comment.getRating());

        // Set timestamp
        if (comment.getTimestamp() != null) {
            String timeAgo = getTimeAgo(comment.getTimestamp());
            holder.textTime.setText(timeAgo);
        } else {
            holder.textTime.setText("Vừa xong");
        }

        // Load avatar using Glide
        Glide.with(holder.itemView.getContext())
                .load(comment.getAvatarUrl())
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(holder.imageAvatar);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    // Helper method to format time ago
    private String getTimeAgo(Date date) {
        long timeAgo = System.currentTimeMillis() - date.getTime();

        if (timeAgo < 60000) {
            return "Vừa xong";
        } else if (timeAgo < 3600000) {
            int minutes = (int) (timeAgo / 60000);
            return minutes + " phút trước";
        } else if (timeAgo < 86400000) {
            int hours = (int) (timeAgo / 3600000);
            return hours + " giờ trước";
        } else if (timeAgo < 604800000) {
            int days = (int) (timeAgo / 86400000);
            return days + " ngày trước";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    // Method to add new comment and notify adapter
    public void addComment(Comment comment) {
        if (comments != null) {
            comments.add(0, comment);
            notifyItemInserted(0);
        }
    }

    // Method to update a specific comment
    public void updateComment(Comment updatedComment) {
        if (comments != null) {
            for (int i = 0; i < comments.size(); i++) {
                Comment comment = comments.get(i);
                if (comment.getUserId() != null && comment.getUserId().equals(updatedComment.getUserId())) {
                    comments.set(i, updatedComment);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    // Method to update comments list
    public void updateComments(List<Comment> newComments) {
        if (comments != null) {
            comments.clear();
            if (newComments != null) {
                comments.addAll(newComments);
            }
            notifyDataSetChanged();
        }
    }

    // Method to clear all comments
    public void clearComments() {
        if (comments != null) {
            comments.clear();
            notifyDataSetChanged();
        }
    }
}
