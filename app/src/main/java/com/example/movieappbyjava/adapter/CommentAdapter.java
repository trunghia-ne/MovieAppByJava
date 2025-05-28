package com.example.movieappbyjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.Comment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUser, textComment, textTime;
        RatingBar ratingUser;

        public ViewHolder(View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.textUser);
            textComment = itemView.findViewById(R.id.textComment);
            textTime = itemView.findViewById(R.id.textTime);
            ratingUser = itemView.findViewById(R.id.ratingUser); // ánh xạ RatingBar mới
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment c = comments.get(position);
        holder.textUser.setText(c.getUsername());
        holder.textComment.setText(c.getComment());

        if (c.getCreatedAt() != null) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(c.getCreatedAt());
            holder.textTime.setText(formattedDate);
        } else {
            holder.textTime.setText("");
        }

        holder.ratingUser.setRating((float) c.getRating());
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }
}
