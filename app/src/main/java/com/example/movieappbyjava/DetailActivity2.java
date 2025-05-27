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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.model.Category;
import com.example.movieappbyjava.model.Episode;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.model.MovieDetailResponse;
import com.example.movieappbyjava.network.KKPhimApi;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity2 extends AppCompatActivity {

    private ImageView imagePoster, btnWatchTrailer;
    private TextView textTitle, textSummary, textInfo;
    private FlexboxLayout layoutGenres, layoutActors, layoutDirectors, layoutEpisodes;
    ScrollView contentLayout;
    ProgressBar progressBarLoading;

    WebView webTrailer;

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

        String slug = getIntent().getStringExtra("movie_slug");
        if (slug == null) {
            Toast.makeText(this, "Không có thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchMovieDetail(slug);
    }

    private void fetchMovieDetail(String slug) {
        progressBarLoading.setVisibility(View.VISIBLE);  // Hiện loading
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
                contentLayout.setVisibility(View.VISIBLE);  // Hiện nội dung khi có dữ liệu

                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body().getMovie();

                    textTitle.setText(movie.getName());
                    textSummary.setText(movie.getContent());
                    textInfo.setText("4.5    •   " + movie.getTime() + "   •   " + movie.getYear() + "   •   " + movie.getView() + " view");

                    Glide.with(DetailActivity2.this)
                            .load(movie.getPoster_url())
                            .into(imagePoster);

                    // Hiển thị Category
                    layoutGenres.removeAllViews();
                    for (Category category : movie.getCategory()) {
                        layoutGenres.addView(createChip(category.getName()));
                    }

                    // Hiển thị Actors
                    layoutActors.removeAllViews();
                    for (String actor : movie.getActor()) {
                        layoutActors.addView(createChip(actor));
                    }

                    layoutDirectors.removeAllViews();
                    for (String director : movie.getDirector()) {
                        layoutDirectors.addView(createChip(director));
                    }

                    btnWatchTrailer.setVisibility(View.VISIBLE);
                    btnWatchTrailer.setOnClickListener(v -> {
                        btnWatchTrailer.setVisibility(View.GONE);
                        imagePoster.setVisibility(View.GONE);
                        webTrailer.setVisibility(View.VISIBLE);

                        String rawUrl = movie.getTrailer_url();

                        // 👉 Nếu là link YouTube thường, chuyển sang embed
                        if (rawUrl.contains("watch?v=")) {
                            rawUrl = rawUrl.replace("watch?v=", "embed/");
                        }

                        // 👉 Nhúng vào WebView
                        String html = "<html><body style='margin:0;padding:0;'>"
                                + "<iframe width='100%' height='100%' "
                                + "src='" + rawUrl + "' frameborder='0' "
                                + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' "
                                + "allowfullscreen></iframe>"
                                + "</body></html>";

                        webTrailer.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                    });
                } else {
                    Toast.makeText(DetailActivity2.this, "Lỗi tải phim", Toast.LENGTH_SHORT).show();
                }

                layoutEpisodes.removeAllViews();

                if (response.body().getEpisodes() != null && !response.body().getEpisodes().isEmpty()) {
                    List<Episode> episodeList = response.body().getEpisodes().get(0).getServer_data();
                    for (int i = 0; i < episodeList.size(); i++) {
                        Episode episode = episodeList.get(i);

                        // 👉 Hiển thị số thứ tự: 01, 02, 03,...
                        String label = String.format("%02d", i + 1);

                        TextView epView = new TextView(DetailActivity2.this);
                        epView.setText(label);  // ⚠️ bỏ chữ "Tập", chỉ còn "01", "02",...

                        epView.setTextColor(Color.WHITE);
                        epView.setTextSize(13); // nhỏ gọn vừa phải
                        epView.setTypeface(null, Typeface.BOLD); // cho đậm dễ nhìn
                        epView.setBackgroundResource(R.drawable.bg_episode_chip);

                        // 👉 Tăng kích thước khung (padding lớn hơn)
                        epView.setPadding(36, 20, 36, 20);  // trái, trên, phải, dưới

                        // 👉 Tăng margin giữa các chip
                        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                FlexboxLayout.LayoutParams.WRAP_CONTENT
                        );
                        lp.setMargins(12, 10, 12, 10);
                        epView.setLayoutParams(lp);

                        // 👉 Click để mở WatchActivity
                        epView.setOnClickListener(v -> {
                            Intent intent = new Intent(DetailActivity2.this, WatchActivity.class);
                            intent.putExtra("video_url", episode.getLink_embed());
                            startActivity(intent);
                        });

                        layoutEpisodes.addView(epView);
                    }
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
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(12, 12, 12, 12);
        chip.setLayoutParams(lp);
        return chip;
    }
}
