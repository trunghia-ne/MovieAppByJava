package com.example.movieappbyjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.adapter.CommentAdapter;
import com.example.movieappbyjava.model.Category;
import com.example.movieappbyjava.model.Comment;
import com.example.movieappbyjava.model.Episode;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.model.MovieDetailResponse;
import com.example.movieappbyjava.network.KKPhimApi;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity2 extends AppCompatActivity {

    private ImageView imagePoster, btnWatchTrailer;
    private View backToHome;
    private TextView textTitle, textSummary, textInfo;
    private FlexboxLayout layoutGenres, layoutActors, layoutDirectors, layoutEpisodes;
    ScrollView contentLayout;
    ProgressBar progressBarLoading;

    WebView webTrailer;
    private RatingBar ratingBar;
    private EditText editComment;
    private Button btnSubmitRating, btnDeleteRating;
    private TextView textRatingStats, textCommentsHeader;
    private RecyclerView recyclerComments;
    private View layoutEmptyComments;
    private String currentRatingId = null;
    private String movieId;
    private String movieTitle;
    private CommentAdapter commentAdapter;
    private List<Comment> commentsList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();

        String slug = getIntent().getStringExtra("movie_slug");
        if (slug == null) {
            Toast.makeText(this, "Không có thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchMovieDetail(slug);
    }

    private void initializeViews() {
        progressBarLoading = findViewById(R.id.progressBarLoading);
        contentLayout = findViewById(R.id.main);
        imagePoster = findViewById(R.id.imagePoster);
        textTitle = findViewById(R.id.textTitle);
        textSummary = findViewById(R.id.textSummary);
        layoutGenres = findViewById(R.id.layoutGenres);
        layoutActors = findViewById(R.id.layoutActors);
        layoutDirectors = findViewById(R.id.layoutDirectors);
        layoutEpisodes = findViewById(R.id.layoutEpisodes);
        textInfo = findViewById(R.id.textInfo);
        btnWatchTrailer = findViewById(R.id.btnWatchTrailer);
        webTrailer = findViewById(R.id.webTrailer);
        webTrailer.getSettings().setJavaScriptEnabled(true);
        webTrailer.getSettings().setLoadWithOverviewMode(true);
        webTrailer.getSettings().setUseWideViewPort(true);

        ratingBar = findViewById(R.id.ratingBar);
        editComment = findViewById(R.id.editComment);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        btnDeleteRating = findViewById(R.id.btnDeleteRating);
        textRatingStats = findViewById(R.id.textRatingStats);
        textCommentsHeader = findViewById(R.id.textCommentsHeader);
        recyclerComments = findViewById(R.id.recyclerComments);
        layoutEmptyComments = findViewById(R.id.layoutEmptyComments);
        backToHome = findViewById(R.id.backToHome);

        btnSubmitRating.setOnClickListener(v -> submitOrUpdateRating());
        btnDeleteRating.setOnClickListener(v -> deleteRating());
        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity2.this, HomeActaivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        commentAdapter = new CommentAdapter(commentsList, currentUserId);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentAdapter);
    }


    private void fetchMovieDetail(String slug) {
        progressBarLoading.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        KKPhimApi api = retrofit.create(KKPhimApi.class);

        api.getMovieDetail(slug).enqueue(new Callback<MovieDetailResponse>() {
            @Override
            public void onResponse(Call<MovieDetailResponse> call, Response<MovieDetailResponse> response) {
                progressBarLoading.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body().getMovie();
                    movieId = slug;
                    movieTitle = movie.getName();

                    setupMovieDetails(movie, response.body());
                    loadUserRating();
                    updateRatingStats();
                    loadComments();

                } else {
                    Toast.makeText(DetailActivity2.this, "Lỗi tải phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetailResponse> call, Throwable t) {
                progressBarLoading.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                Toast.makeText(DetailActivity2.this, "Không gọi được API", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void setupMovieDetails(Movie movie, MovieDetailResponse response) {
        textTitle.setText(movie.getName());
        textSummary.setText(movie.getContent());
        textInfo.setText(movie.getTime() + "   •   " + movie.getYear() + "   •   " + movie.getView() + " view");

        Glide.with(DetailActivity2.this)
                .load(movie.getPoster_url())
                .into(imagePoster);

        setupGenres(movie);
        setupActors(movie);
        setupDirectors(movie);
        setupTrailer(movie);
        setupEpisodes(response);
    }

    private void setupGenres(Movie movie) {
        layoutGenres.removeAllViews();
        for (Category category : movie.getCategory()) {
            layoutGenres.addView(createChip(category.getName()));
        }
    }

    private void setupActors(Movie movie) {
        layoutActors.removeAllViews();
        for (String actor : movie.getActor()) {
            layoutActors.addView(createChip(actor));
        }
    }

    private void setupDirectors(Movie movie) {
        layoutDirectors.removeAllViews();
        for (String director : movie.getDirector()) {
            layoutDirectors.addView(createChip(director));
        }
    }

    private void setupTrailer(Movie movie) {
        btnWatchTrailer.setVisibility(View.VISIBLE);
        btnWatchTrailer.setOnClickListener(v -> {
            btnWatchTrailer.setVisibility(View.GONE);
            imagePoster.setVisibility(View.GONE);
            webTrailer.setVisibility(View.VISIBLE);

            String rawUrl = movie.getTrailer_url();
            if (rawUrl.contains("watch?v=")) {
                rawUrl = rawUrl.replace("watch?v=", "embed/");
            }

            String html = "<html><body style='margin:0;padding:0;'>"
                    + "<iframe width='100%' height='100%' "
                    + "src='" + rawUrl + "' frameborder='0' "
                    + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' "
                    + "allowfullscreen></iframe>"
                    + "</body></html>";

            webTrailer.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        });
    }

    private void setupEpisodes(MovieDetailResponse response) {
        layoutEpisodes.removeAllViews();
        if (response.getEpisodes() != null && !response.getEpisodes().isEmpty()) {
            List<Episode> episodeList = response.getEpisodes().get(0).getServer_data();
            for (int i = 0; i < episodeList.size(); i++) {
                Episode episode = episodeList.get(i);
                String label = String.format("%02d", i + 1);

                TextView epView = new TextView(DetailActivity2.this);
                epView.setText(label);
                epView.setTextColor(Color.WHITE);
                epView.setTextSize(13);
                epView.setTypeface(null, Typeface.BOLD);
                epView.setBackgroundResource(R.drawable.bg_episode_chip);
                epView.setPadding(36, 20, 36, 20);

                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(12, 10, 12, 10);
                epView.setLayoutParams(lp);

                epView.setOnClickListener(v -> {
                    Intent intent = new Intent(DetailActivity2.this, WatchActivity.class);
                    intent.putExtra("video_url", episode.getLink_embed());
                    startActivity(intent);
                });

                layoutEpisodes.addView(epView);
            }
        }
    }

    private TextView createChip(String text) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(Color.WHITE);
        chip.setBackgroundResource(R.drawable.bg_episode_chip);
        chip.setTextSize(12);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setSingleLine(true);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        chip.setPadding(32, 14, 32, 14);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(12, 12, 12, 12);
        chip.setLayoutParams(lp);
        return chip;
    }

    private String getUserId() {
        return getSharedPreferences("users", MODE_PRIVATE).getString("uid", "anonymous_" + System.currentTimeMillis());
    }

    private String getUserName() {
        String username = getSharedPreferences("users", MODE_PRIVATE).getString("username", null);
        return username != null ? username : "Người dùng " + System.currentTimeMillis();
    }

    private void submitOrUpdateRating() {
        String comment = editComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng đánh giá sao", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = getUserId();
        String username = getUserName();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("username", username);
        data.put("rating", rating);
        data.put("comment", comment);
        data.put("movieId", movieId);
        data.put("movieTitle", movieTitle);
        data.put("timestamp", FieldValue.serverTimestamp());

        if (currentRatingId == null) {
            // Add new rating
            db.collection("reviews")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        currentRatingId = documentReference.getId();
                        Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        editComment.setText("");
                        ratingBar.setRating(0);
                        btnDeleteRating.setVisibility(View.VISIBLE);

                        // Add comment to list and update UI immediately
                        Comment newComment = new Comment(username, comment, rating, new Date(), uid, movieTitle);
                        commentAdapter.addComment(newComment);
                        recyclerComments.scrollToPosition(0);

                        updateRatingStats();
                        updateCommentsUI();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE_ERROR", "Error adding rating", e);
                    });
        } else {
            // Update existing rating
            db.collection("reviews").document(currentRatingId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        editComment.setText("");
                        ratingBar.setRating(0);
                        loadComments();
                        updateRatingStats();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE_ERROR", "Error updating rating", e);
                    });
        }
    }

    private void deleteRating() {
        if (currentRatingId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews").document(currentRatingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xoá đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    currentRatingId = null;
                    btnDeleteRating.setVisibility(View.GONE);
                    editComment.setText("");
                    ratingBar.setRating(0);
                    loadComments();
                    updateRatingStats();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_ERROR", "Error deleting rating", e);
                });
    }

    private void loadUserRating() {
        String uid = getUserId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        currentRatingId = doc.getId();

                        Double rating = doc.getDouble("rating");
                        String comment = doc.getString("comment");

                        if (rating != null) ratingBar.setRating(rating.floatValue());
                        if (comment != null) editComment.setText(comment);

                        btnDeleteRating.setVisibility(View.VISIBLE);
                        btnSubmitRating.setText("Cập nhật đánh giá");
                    }
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_ERROR", "Error loading user rating", e));
    }

    private void updateRatingStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalReviews = queryDocumentSnapshots.size();

                    if (totalReviews > 0) {
                        double totalRating = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Double rating = doc.getDouble("rating");
                            if (rating != null) {
                                totalRating += rating;
                            }
                        }

                        double averageRating = totalRating / totalReviews;
                        textRatingStats.setText(String.format(Locale.getDefault(),
                                "%.1f ⭐ | %d đánh giá", averageRating, totalReviews));
                    } else {
                        textRatingStats.setText("Chưa có đánh giá");
                    }
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_ERROR", "Error updating rating stats", e));
    }

    private void loadComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentsList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String username = doc.getString("username");
                        String comment = doc.getString("comment");
                        Double rating = doc.getDouble("rating");
                        Date timestamp = doc.getDate("timestamp");
                        String userId = doc.getString("userId");
                        String movieTitle = doc.getString("movieTitle");

                        if (username != null && comment != null && rating != null && timestamp != null && userId != null) {
                            Comment cmt = new Comment(username, comment, rating, timestamp, userId, movieTitle);
                            commentsList.add(cmt);
                        }
                    }

                    commentAdapter.notifyDataSetChanged();
                    updateCommentsUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải bình luận", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_ERROR", "Error loading comments", e);
                });
    }

    private void updateCommentsUI() {
        if (commentsList.isEmpty()) {
            layoutEmptyComments.setVisibility(View.VISIBLE);
            recyclerComments.setVisibility(View.GONE);
            textCommentsHeader.setText("Chưa có bình luận");
        } else {
            layoutEmptyComments.setVisibility(View.GONE);
            recyclerComments.setVisibility(View.VISIBLE);
            textCommentsHeader.setText("Bình luận (" + commentsList.size() + ")");
        }
    }
}
