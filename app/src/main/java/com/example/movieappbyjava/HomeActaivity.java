package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup; // Import cho ViewGroup.MarginLayoutParams

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout; // Import cho CoordinatorLayout.LayoutParams
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout; // Import cho AppBarLayout
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponse;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.network.KKPhimApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActaivity extends AppCompatActivity {
    private RecyclerView recyclerViewLatestMovies, recyclerSingleMovies, recyclerSeriesMovies, recyclerAnimeMovies, recyclerTvShowMovies;
    private MovieAdapter latestMoviesAdapter, singleMovieAdapter, seriesMovieAdapter, animeMovieAdapter, tvShowMovieAdapter;
    private List<Movie> latestMovieList = new ArrayList<>();
    private List<Movie> singleMovieList = new ArrayList<>();
    private List<Movie> seriesMoviesList = new ArrayList<>();
    private List<Movie> animeMoviesList = new ArrayList<>();
    private List<Movie> tvShowMoviesList = new ArrayList<>();

    private KKPhimApi api;

    private void setupMovieSection(RecyclerView rv, List<Movie> list, MovieAdapter movieAdapterInstance, Call<ApiResponsePhim> apiCall) {
        if (movieAdapterInstance == null) {
            movieAdapterInstance = new MovieAdapter(list);
        }
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(movieAdapterInstance);
        final MovieAdapter finalAdapter = movieAdapterInstance;

        apiCall.enqueue(new Callback<ApiResponsePhim>() {
            @Override
            public void onResponse(Call<ApiResponsePhim> call, Response<ApiResponsePhim> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getItems() != null) {
                    list.clear();
                    list.addAll(response.body().getData().getItems());
                    finalAdapter.notifyDataSetChanged();
                } else {
                    String errorMsg = "Response null hoặc không có dữ liệu (setupMovieSection)";
                    if (response != null && response.errorBody() != null) {
                        try {
                            errorMsg += " - Error: " + response.errorBody().string();
                        } catch (Exception e) { /* Bỏ qua */ }
                    } else if (response != null) {
                        errorMsg += " - Code: " + response.code();
                    }
                    Log.e("API_MovieSection", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponsePhim> call, Throwable t) {
                Log.e("API_MovieSection", "Lỗi khi gọi API (setupMovieSection): " + t.getMessage(), t);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this); // Kích hoạt chế độ edge-to-edge
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_actaivity);

        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, v.getPaddingTop(), systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActaivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });

        ImageSlider imageSlider = findViewById(R.id.imageSlider);
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=1", "Hình 1", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=2", "Hình 2", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=3", "Hình 3", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=4", "Hình 4", ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        recyclerViewLatestMovies = findViewById(R.id.recyclerBestMovies);
        recyclerViewLatestMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        latestMoviesAdapter = new MovieAdapter(latestMovieList);
        recyclerViewLatestMovies.setAdapter(latestMoviesAdapter);

        api.getLatestMovies(1).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    latestMovieList.clear();
                    latestMovieList.addAll(response.body().getItems());
                    latestMoviesAdapter.notifyDataSetChanged();
                } else {
                    String errorMsg = "Response null hoặc không có dữ liệu (LatestMovies)";
                    if (response != null && response.errorBody() != null) {
                        try {
                            errorMsg += " - Error: " + response.errorBody().string();
                        } catch (Exception e) {}
                    } else if (response != null) {
                        errorMsg += " - Code: " + response.code();
                    }
                    Log.e("API_LatestMovies", errorMsg);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_LatestMovies", "Lỗi khi gọi API (LatestMovies): " + t.getMessage(), t);
            }
        });

        recyclerSingleMovies = findViewById(R.id.recyclerSingleMovies);
        singleMovieAdapter = new MovieAdapter(singleMovieList);
        setupMovieSection(recyclerSingleMovies, singleMovieList, singleMovieAdapter, api.getSingleMovies());

        recyclerSeriesMovies = findViewById(R.id.recyclerSeriesMovies);
        seriesMovieAdapter = new MovieAdapter(seriesMoviesList);
        setupMovieSection(recyclerSeriesMovies, seriesMoviesList, seriesMovieAdapter, api.getSeriasMovies());

        recyclerAnimeMovies = findViewById(R.id.recyclerAnimeMovies);
        animeMovieAdapter = new MovieAdapter(animeMoviesList);
        setupMovieSection(recyclerAnimeMovies, animeMoviesList, animeMovieAdapter, api.getAnimeMovies());

        recyclerTvShowMovies = findViewById(R.id.recyclerTvShowMovies);
        tvShowMovieAdapter = new MovieAdapter(tvShowMoviesList);
        setupMovieSection(recyclerTvShowMovies, tvShowMoviesList, tvShowMovieAdapter, api.getTvShowMovies());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home_search) { // ID của item search trong menu của bạn
            Intent intent = new Intent(HomeActaivity.this, SearchInputActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}