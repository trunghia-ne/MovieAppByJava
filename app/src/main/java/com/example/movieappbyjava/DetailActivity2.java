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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.adapter.CommentAdapter;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.ApiResponseMessage;
import com.example.movieappbyjava.model.Category;
import com.example.movieappbyjava.model.CollectionFilm;
import com.example.movieappbyjava.model.Comment;
import com.example.movieappbyjava.model.Episode;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.model.MovieDetailResponse;
import com.example.movieappbyjava.model.PaymentUrlResponse;
import com.example.movieappbyjava.model.WatchHistory;
import com.example.movieappbyjava.network.KKPhimApi;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    private ImageButton btnBack, btnFav;
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
    private String movieSlug, movieName, poster_url;
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
        String movieTitle = getIntent().getStringExtra("movie_name");
        String poster_Url = getIntent().getStringExtra("poster_url");
        if (slug == null) {
            Toast.makeText(this, "Không có thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        movieSlug = slug; // ✅ Lưu slug
        movieName = movieTitle;
        poster_url = poster_Url;
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
        btnBack = findViewById(R.id.btnBack);
        btnFav = findViewById(R.id.btnFav);
        btnSubmitRating.setOnClickListener(v -> submitOrUpdateRating());
        btnDeleteRating.setOnClickListener(v -> deleteRating());
        //Quay ve giao dien truoc do
        btnBack.setOnClickListener(v -> onBackPressed());

        //Them phim vao collection
        btnFav.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                Log.d("USER_ID", "UID: " + userId); // In ra UID

                // Gọi hàm getCollectionsByUser từ ApiService
                ApiClient.getApiService().getCollectionsByUser(userId).enqueue(new Callback<List<CollectionFilm>>() {
                    @Override
                    public void onResponse(Call<List<CollectionFilm>> call, Response<List<CollectionFilm>> response) {
                        Log.d("API_RESPONSE", "Code: " + response.code());
                        if (response.isSuccessful()) {
                            List<CollectionFilm> collections = response.body();
                            if (collections != null) {
                                showCollectionDialog(collections);
                            } else {
                                Toast.makeText(DetailActivity2.this, "Không lấy được bộ sưu tập", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DetailActivity2.this, "Không lấy được bộ sưu tập", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CollectionFilm>> call, Throwable t) {
                        Toast.makeText(DetailActivity2.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(DetailActivity2.this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
        checkIfFilmIsInCollections();
    }


    private void checkIfFilmIsInCollections() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || movieSlug == null) return;

        String userId = user.getUid();

        ApiClient.getApiService().isFilmInUserCollections(userId, movieSlug)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                            // ✅ Phim đã có trong bộ sưu tập → Đổi icon thành trái tim đỏ
                            btnFav.setImageResource(R.drawable.ic_heart_fill);
                        } else {
                            // ❌ Phim chưa có → Để icon trái tim trắng
                            btnFav.setImageResource(R.drawable.ic_favorite);
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.e("CHECK_COLLECTION", "Lỗi: " + t.getMessage());
                        // Gán mặc định là trái tim trắng
                        btnFav.setImageResource(R.drawable.ic_favorite);
                    }
                });
    }


    //Hien thi Popup Modal chon bo suu tap
    private void showCollectionDialog(List<CollectionFilm> collections) {
        List<String> names = new ArrayList<>();
        for (CollectionFilm cf : collections) {
            names.add(cf.getCollection_name());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn bộ sưu tập")
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    CollectionFilm selected = collections.get(which);
                    String collectionId = selected.getId();
                    if (collectionId == null || collectionId.isEmpty()) {
                        Toast.makeText(this, "ID bộ sưu tập không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Đã chọn: " + selected.getCollection_name(), Toast.LENGTH_SHORT).show();

                    Movie filmToAddFav = new Movie();
                    filmToAddFav.setName(movieTitle);
                    filmToAddFav.setSlug(movieSlug);
                    filmToAddFav.setPoster_url(poster_url);

                    ApiClient.getApiService().addFilmToCollection(collectionId, filmToAddFav)
                            .enqueue(new Callback<ApiResponseMessage>() {
                                @Override
                                public void onResponse(Call<ApiResponseMessage> call, Response<ApiResponseMessage> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(DetailActivity2.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                    } else if (response.code() == 409) {
                                        Toast.makeText(DetailActivity2.this, "Phim đã tồn tại trong bộ sưu tập.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(DetailActivity2.this, "Lỗi khi thêm phim: " + response.code(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponseMessage> call, Throwable t) {
                                    Toast.makeText(DetailActivity2.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void setupRecyclerView() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";

        commentAdapter = new CommentAdapter(commentsList, currentUserId, movieSlug, movieSlug);
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
        checkIfFilmIsInCollections();
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

            String userId = getCurrentUserId();
            if (userId == null) {
                Toast.makeText(DetailActivity2.this, "Bạn cần đăng nhập để lưu lịch sử xem", Toast.LENGTH_SHORT).show();
                return;
            }
            saveWatchHistory(movie, 0, 0, "TRAILER");
        });
    }

    // PHƯƠNG THỨC LƯU PHIM ĐƯỢC CLICK VÀO LỊCH SỬ
    private void saveWatchHistory(Movie movie, long watchedDuration, long totalDuration, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        WatchHistory history = new WatchHistory(
                userId,
                movie.getId(),
                movie.getName(),
                movie.getPoster_url(),
                watchedDuration,
                totalDuration,
                status,
                new Date(),
                "Android"
        );

        db.collection("watch_history")
                .add(history)
                .addOnSuccessListener(documentReference -> {
                    Log.d("WatchHistory", "Lưu thành công: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("WatchHistory", "Lưu thất bại", e);
                });
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        Log.d("MovieApi", "Current userId: " + (userId != null ? userId : "null"));
        return userId;
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
                    // Kiểm tra null cho rating
                    Double rating = existingComment.getRating();
                    ratingBar.setRating(rating != null ? (float) (double) rating : 0f);
                    editComment.setText(existingComment.getComment() != null ? existingComment.getComment() : "");
                    btnSubmitRating.setText("Cập nhật đánh giá");
                    btnDeleteRating.setVisibility(View.VISIBLE);
                    Log.d("USER_RATING", "Tải thành công đánh giá hiện có");
                } else {
                    btnSubmitRating.setText("Gửi đánh giá");
                    btnDeleteRating.setVisibility(View.GONE);
                    currentRatingId = null;
                    ratingBar.setRating(0);
                    editComment.setText("");
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Log.e("USER_RATING", "Lỗi khi tải đánh giá người dùng: " + t.getMessage());
                btnSubmitRating.setText("Gửi đánh giá");
                btnDeleteRating.setVisibility(View.GONE);
                currentRatingId = null;
                ratingBar.setRating(0);
                editComment.setText("");
            }
        });
    }

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

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy tên người dùng từ Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = "Người dùng"; // Giá trị mặc định
                    if (documentSnapshot.exists()) {
                        username = documentSnapshot.getString("name") != null ? documentSnapshot.getString("name") : "Người dùng";
                    }

                    Comment comment = new Comment();
                    comment.setUserId(userId);
                    comment.setUsername(username); // Sử dụng tên từ Firestore
                    comment.setComment(commentText);
                    comment.setRating((double) rating);
                    comment.setSlug(movieSlug);
                    comment.setMovieTitle(movieTitle);

                    if (currentRatingId != null) {
                        comment.setId(currentRatingId);
                    }

                    progressBarLoading.setVisibility(View.VISIBLE);

                    ApiClient.getApiService().submitReview(comment).enqueue(new Callback<Comment>() {
                        @Override
                        public void onResponse(Call<Comment> call, Response<Comment> response) {
                            progressBarLoading.setVisibility(View.GONE);

                            if (response.isSuccessful() && response.body() != null) {
                                Comment savedComment = response.body();
                                currentRatingId = savedComment.getId();
                                btnSubmitRating.setText("Cập nhật đánh giá");
                                btnDeleteRating.setVisibility(View.VISIBLE);
                                Toast.makeText(DetailActivity2.this, "Đánh giá đã được lưu!", Toast.LENGTH_SHORT).show();
                                commentAdapter.addComment(savedComment);
                                layoutEmptyComments.setVisibility(View.GONE);
                                recyclerComments.setVisibility(View.VISIBLE);
                                textCommentsHeader.setText("Bình luận (" + commentAdapter.getItemCount() + ")");
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
                })
                .addOnFailureListener(e -> {
                    progressBarLoading.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity2.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    Log.e("FETCH_USER", "Error: " + e.getMessage());
                });
    }

    private void deleteRating() {
        if (currentRatingId == null) {
            Toast.makeText(this, "Không có đánh giá để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteReview(currentRatingId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                ratingBar.setRating(0);
                                editComment.setText("");
                                btnSubmitRating.setText("Gửi đánh giá");
                                btnDeleteRating.setVisibility(View.GONE);
                                currentRatingId = null;
                                Toast.makeText(DetailActivity2.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                                loadComments();
                                updateRatingStats();
                            } else {
                                Log.e("DELETE_RATING", "Error code: " + response.code());
                                String errorMessage = response.code() == 400 ? "Dữ liệu không hợp lệ" : "Lỗi server, vui lòng thử lại";
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Log.e("DELETE_RATING", "Error body: " + errorBody);
                                } catch (IOException e) {
                                    Log.e("DELETE_RATING", "Error reading error body", e);
                                }
                                Toast.makeText(DetailActivity2.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("DELETE_RATING", "Network failure: " + t.getMessage(), t);
                            Toast.makeText(DetailActivity2.this, "Lỗi kết nối server, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

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

    private void loadComments() {
        if (movieSlug == null || movieSlug.isEmpty()) {
            Log.e("LOAD_COMMENTS", "MovieSlug is null or empty");
            showEmptyComments();
            Toast.makeText(this, "Không thể tải bình luận: Phim không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LOAD_COMMENTS", "Loading comments for slug: " + movieSlug); // Thêm log để debug
        progressBarLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getReviews(movieSlug).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                progressBarLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    commentsList.clear();
                    commentsList.addAll(response.body());
                    commentAdapter.updateComments(commentsList);
                    textCommentsHeader.setText("Bình luận (" + commentsList.size() + ")");
                    if (commentsList.isEmpty()) {
                        showEmptyComments();
                        Log.d("LOAD_COMMENTS", "No comments found for slug: " + movieSlug);
                    } else {
                        layoutEmptyComments.setVisibility(View.GONE);
                        recyclerComments.setVisibility(View.VISIBLE);
                        Log.d("LOAD_COMMENTS", "Loaded " + commentsList.size() + " comments for slug: " + movieSlug);
                    }
                } else {
                    Log.e("LOAD_COMMENTS", "Error code: " + response.code() + " for slug: " + movieSlug);
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("LOAD_COMMENTS", "Error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("LOAD_COMMENTS", "Error reading error body", e);
                    }
                    showEmptyComments();
                    String errorMessage = response.code() == 404
                            ? "Không có bình luận nào hoặc cần kiểm tra chỉ mục Firestore cho slug: " + movieSlug
                            : "Lỗi tải bình luận (Mã lỗi: " + response.code() + ")";
                    Toast.makeText(DetailActivity2.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                progressBarLoading.setVisibility(View.GONE);
                Log.e("LOAD_COMMENTS", "Network failure for slug: " + movieSlug + ", error: " + t.getMessage(), t);
                showEmptyComments();
                Toast.makeText(DetailActivity2.this, "Lỗi kết nối mạng, vui lòng thử lại", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void showEmptyComments() {
        layoutEmptyComments.setVisibility(View.VISIBLE);
        recyclerComments.setVisibility(View.GONE);
        textCommentsHeader.setText("Bình luận (0)");
    }
}