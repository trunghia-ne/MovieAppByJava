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

import java.util.ArrayList; // Cần thiết cho new ArrayList<>()
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    // Context có thể không cần thiết nếu bạn luôn lấy từ itemView.getContext()
    // private Context context;

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
        // if (this.movies == null) { // Đảm bảo movies không bao giờ null ngay từ đầu
        //     this.movies = new ArrayList<>();
        // }
        // this.context = context; // Nếu bạn truyền context qua constructor
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
        // Kiểm tra movie null để tránh lỗi hiếm gặp
        if (movie == null) {
            return;
        }

        holder.title.setText(movie.getName());

        // Xử lý URL ảnh
        String imageUrl = movie.getPoster_url();
        if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.isEmpty()) {
            imageUrl = "https://img.phimapi.com/" + imageUrl;
        } else if (imageUrl == null || imageUrl.isEmpty()) {
            // Fallback sang thumb_url nếu poster_url không có
            imageUrl = movie.getThumb_url();
            if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.isEmpty()) {
                imageUrl = "https://img.phimapi.com/" + imageUrl;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_shape) // Ảnh giữ chỗ
                .error(R.drawable.placeholder_shape)       // Ảnh khi lỗi (có thể dùng chung)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            if (movie.getSlug() != null && !movie.getSlug().isEmpty()) {
                Intent intent = new Intent(v.getContext(), DetailActivity2.class);
                intent.putExtra("movie_slug", movie.getSlug());
                v.getContext().startActivity(intent);
            } else {
                // Tùy chọn: Thông báo cho người dùng
                // Toast.makeText(v.getContext(), "Không có thông tin chi tiết cho phim này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size(); // An toàn hơn
    }

    /**
     * Cập nhật toàn bộ danh sách phim trong adapter.
     * Dữ liệu cũ sẽ bị xóa.
     * @param newMovies Danh sách phim mới để hiển thị.
     */
    public void updateMovies(List<Movie> newMovies) {
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        this.movies.clear();
        if (newMovies != null) {
            this.movies.addAll(newMovies);
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật toàn bộ giao diện
    }

    /**
     * Thêm một danh sách phim vào cuối danh sách hiện tại.
     * Hữu ích cho việc triển khai tải thêm trang (pagination).
     * @param additionalMovies Danh sách phim cần thêm.
     */
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

    /**
     * Xóa tất cả các phim khỏi adapter.
     * RecyclerView sẽ trở nên trống.
     */
    public void clearMovies() { // <<<< PHƯƠNG THỨC ĐƯỢC THÊM VÀO >>>>
        if (this.movies != null) {
            this.movies.clear();
            notifyDataSetChanged();
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        // Bạn có thể thêm các View khác ở đây nếu item_movie.xml của bạn có
        // ví dụ: TextView yearTextView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo các ID này khớp với ID trong file R.layout.item_movie của bạn
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            // yearTextView = itemView.findViewById(R.id.year); // Ví dụ
        }
    }
}