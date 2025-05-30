package com.example.movieappbyjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.Category;
import com.example.movieappbyjava.model.Comment;
import com.example.movieappbyjava.model.Episode;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.model.MovieDetailResponse;
import com.example.movieappbyjava.model.PaymentUrlResponse;
import com.example.movieappbyjava.network.ApiService;
import com.example.movieappbyjava.network.KKPhimApi;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
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
    private LinearLayout contentLayout;

    ProgressBar progressBarLoading;

    WebView webTrailer;
    private RatingBar ratingBar;
    private EditText editComment;
    private Button btnSubmitRating, btnDeleteRating;
    private TextView textRatingStats, textCommentsHeader;
    private RecyclerView recyclerComments;
    private View layoutEmptyComments;
    private String currentRatingId = null;
    private String movieSlug; // ✅ Đổi từ movieId thành movieSlug
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

        movieSlug = slug; // ✅ Lưu slug
        fetchMovieDetail(slug);
    }

    private void initializeViews() {
        progressBarLoading = findViewById(R.id.progressBarLoading);
        contentLayout = findViewById(R.id.layoutMainContent);
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";

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
                    movieSlug = slug; // ✅ Lưu slug
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

                // 👉 Click để mở WatchActivity
                epView.setOnClickListener(v -> {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(DetailActivity2.this, "Bạn cần đăng nhập để xem phim", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = currentUser.getUid();

                    FirebaseFirestore.getInstance()
                            .collection("payments")
                            .whereEqualTo("userId", userId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                    Boolean paid = document.getBoolean("paid");
                                    if (paid != null && paid) {
                                        // Đã thanh toán
                                        Intent intent = new Intent(DetailActivity2.this, WatchActivity.class);
                                        intent.putExtra("video_url", episode.getLink_embed());
                                        startActivity(intent);
                                    } else {
                                        // Chưa thanh toán
                                        showPaymentDialog();
                                    }
                                } else {
                                    // Không tìm thấy thông tin thanh toán
                                    showPaymentDialog();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DetailActivity2.this, "Lỗi kiểm tra thanh toán", Toast.LENGTH_SHORT).show();
                            });
                });

                layoutEpisodes.addView(epView);
            }
        }

    }
    private void showPaymentDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(DetailActivity2.this)
                .setTitle("Bạn cần mua gói xem phim")
                .setMessage("Vui lòng thanh toán để tiếp tục xem phim.")
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    callPaymentApiAndOpenUrl();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void callPaymentApiAndOpenUrl() {
        int amount = 100000; // Số tiền thanh toán, có thể lấy từ server hoặc cố định
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String userId = user.getUid();
        ApiClient.getApiService().createPayment(amount, userId).enqueue(new Callback<PaymentUrlResponse>() {
            @Override
            public void onResponse(Call<PaymentUrlResponse> call, Response<PaymentUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String paymentUrl = response.body().getPaymentUrl();
                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                        // Mở trang thanh toán VNPay bằng trình duyệt (hoặc WebView nếu bạn muốn)
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(DetailActivity2.this, "URL thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity2.this, "Lỗi lấy link thanh toán", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentUrlResponse> call, Throwable t) {
                Toast.makeText(DetailActivity2.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                Log.e("PaymentAPI", t.getMessage());
            }
        });
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

    private void loadUserRating() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || movieSlug == null) return;

        String userId = currentUser.getUid();

        ApiClient.getApiService().getUserReview(userId, movieSlug).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Comment existingComment = response.body();
                    currentRatingId = existingComment.getId();

                    // Hiển thị đánh giá hiện có
                    ratingBar.setRating((float) existingComment.getRating());
                    editComment.setText(existingComment.getComment());

                    // Đổi text button
                    btnSubmitRating.setText("Cập nhật đánh giá");
                    btnDeleteRating.setVisibility(View.VISIBLE);

                    Log.d("USER_RATING", "Tải thành công đánh giá hiện có");
                } else {
                    // Chưa có đánh giá
                    btnSubmitRating.setText("Gửi đánh giá");
                    btnDeleteRating.setVisibility(View.GONE);
                    currentRatingId = null;
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Log.e("USER_RATING", "Lỗi khi tải đánh giá người dùng: " + t.getMessage());
            }
        });
    }

    // ✅ Cập nhật để sử dụng slug
    private void submitOrUpdateRating() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentText = editComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo comment object
        Comment comment = new Comment();
        comment.setUserId(currentUser.getUid());
        comment.setUsername(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Người dùng");
        comment.setComment(commentText);
        comment.setRating(rating);
        comment.setSlug(movieSlug);
        comment.setMovieTitle(movieTitle);
        comment.setTimestamp(new Date());

        if (currentRatingId != null) {
            comment.setId(currentRatingId);
        }

        // Hiển thị loading
        progressBarLoading.setVisibility(View.VISIBLE);

        ApiClient.getApiService().submitReview(comment).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                progressBarLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Comment savedComment = response.body();
                    currentRatingId = savedComment.getId();

                    // Update UI
                    btnSubmitRating.setText("Cập nhật đánh giá");
                    btnDeleteRating.setVisibility(View.VISIBLE);

                    Toast.makeText(DetailActivity2.this, "Đánh giá đã được lưu!", Toast.LENGTH_SHORT).show();

                    // Thêm bình luận mới vào đầu danh sách
                    commentAdapter.addComment(savedComment);

                    // Cập nhật số lượng bình luận và ẩn layout trống
                    layoutEmptyComments.setVisibility(View.GONE);
                    recyclerComments.setVisibility(View.VISIBLE);
                    textCommentsHeader.setText("Bình luận (" + commentAdapter.getItemCount() + ")");

                    // Cập nhật thống kê rating
                    updateRatingStats();
                } else {
                    Toast.makeText(DetailActivity2.this, "Lỗi khi lưu đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                progressBarLoading.setVisibility(View.GONE);
                Toast.makeText(DetailActivity2.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                Log.e("SUBMIT_RATING", "Error: " + t.getMessage());
            }
        });
    }

    // ✅ Xóa đánh giá
    private void deleteRating() {
        if (currentRatingId == null) {
            Toast.makeText(this, "Không có đánh giá để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteReview(currentRatingId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Reset UI
                                ratingBar.setRating(0);
                                editComment.setText("");
                                btnSubmitRating.setText("Gửi đánh giá");
                                btnDeleteRating.setVisibility(View.GONE);
                                currentRatingId = null;

                                Toast.makeText(DetailActivity2.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();

                                // ✅ Refresh danh sách comment và rating stats
                                loadComments();
                                updateRatingStats();
                            } else {
                                Toast.makeText(DetailActivity2.this, "Lỗi khi xóa đánh giá", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(DetailActivity2.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ✅ Cập nhật để sử dụng slug
    private void updateRatingStats() {
        if (movieSlug == null) return;

        ApiClient.getApiService().getAverageRating(movieSlug).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();

                    Object avgObj = result.get("average");
                    Object countObj = result.get("count");

                    double average = 0.0;
                    int count = 0;

                    if (avgObj instanceof Double) {
                        average = (Double) avgObj;
                    } else if (avgObj instanceof Integer) {
                        average = ((Integer) avgObj).doubleValue();
                    }

                    if (countObj instanceof Integer) {
                        count = (Integer) countObj;
                    } else if (countObj instanceof Double) {
                        count = ((Double) countObj).intValue();
                    }

                    String statsText = String.format(Locale.getDefault(),
                            "⭐ %.1f/5 (%d đánh giá)", average, count);
                    textRatingStats.setText(statsText);

                    Log.d("RATING_STATS", "Average: " + average + ", Count: " + count);
                } else {
                    textRatingStats.setText("⭐ Chưa có đánh giá");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("RATING_STATS", "Error: " + t.getMessage());
                textRatingStats.setText("⭐ Lỗi tải thống kê");
            }
        });
    }

    // ✅ Cập nhật để sử dụng slug
    private void loadComments() {
        if (movieSlug == null) return;

        Log.d("DEBUG_SLUG", "Slug: " + movieSlug);
        ApiClient.getApiService().getReviews(movieSlug).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // Hiển thị bình luận
                    } else {
                        Log.d("LOAD_COMMENTS", "Response body is null or empty");
                        showEmptyComments();
                    }
                } else {
                    // Thêm thông tin lỗi chi tiết
                    Log.e("LOAD_COMMENTS", "Error code: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("LOAD_COMMENTS", "Error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showEmptyComments();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.e("LOAD_COMMENTS", "Network failure: " + t.getMessage(), t);
                showEmptyComments();
            }
        });
    }

    private void showEmptyComments() {
        layoutEmptyComments.setVisibility(View.VISIBLE);
        recyclerComments.setVisibility(View.GONE);
        textCommentsHeader.setText("Bình luận (0)");
    }
}
