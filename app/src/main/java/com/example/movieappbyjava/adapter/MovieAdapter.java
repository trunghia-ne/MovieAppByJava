package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import nếu bạn muốn dùng Toast

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.movieappbyjava.DetailActivity2;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    public interface OnMovieLongClickListener {
        void onLongClick(Movie movie);
    }

    private OnMovieLongClickListener longClickListener;

    public void setOnMovieLongClickListener(OnMovieLongClickListener listener) {
        this.longClickListener = listener;
    }


    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        if (movie == null) {
            return;
        }

        holder.title.setText(movie.getName());

        String imageUrl = movie.getPoster_url();
        if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.isEmpty()) {
            imageUrl = "https://img.phimapi.com/" + imageUrl;
        } else if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = movie.getThumb_url();
            if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.isEmpty()) {
                imageUrl = "https://img.phimapi.com/" + imageUrl;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_shape)
                .error(R.drawable.placeholder_shape)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            if (movie.getSlug() != null && !movie.getSlug().isEmpty()) {
                Intent intent = new Intent(v.getContext(), DetailActivity2.class);
                intent.putExtra("movie_slug", movie.getSlug());
                intent.putExtra("movie_name", movie.getName());
                intent.putExtra("poster_url", movie.getPoster_url());
                v.getContext().startActivity(intent);
            } else {
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(movie);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size(); // An toàn hơn
    }

    public void updateMovies(List<Movie> newMovies) {
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        this.movies.clear();
        if (newMovies != null) {
            this.movies.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> additionalMovies) {
        if (additionalMovies != null && !additionalMovies.isEmpty()) {
            if (this.movies == null) {
                this.movies = new ArrayList<>();
            }
            int startPosition = this.movies.size();
            this.movies.addAll(additionalMovies);
            notifyItemRangeInserted(startPosition, additionalMovies.size());
        }
    }

    public void clearMovies() {
        if (this.movies != null) {
            this.movies.clear();
            notifyDataSetChanged();
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
        }
    }

    public void appendMovies(List<Movie> newMovies) {
        int oldSize = movies.size();
        movies.addAll(newMovies);
        notifyItemRangeInserted(oldSize, newMovies.size());
    }
}