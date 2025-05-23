    package com.example.movieappbyjava;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.viewpager2.widget.ViewPager2;

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
    import java.util.Arrays;
    import java.util.List;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;

    public class HomeActaivity extends AppCompatActivity {
        private RecyclerView recyclerView,recyclerSingleMovies,recyclerSeriesMovies, recyclerAnimeMovies, recyclerTvShowMovies;
        private MovieAdapter adapter, singleMovieAdapter,seriesMovieAdapter, animeMovieAdapter, tvShowMovieAdapter;
        private List<Movie> movieList = new ArrayList<>();
        private List<Movie> singleMovieList = new ArrayList<>();
        private List<Movie> seriesMovies = new ArrayList<>();
        private List<Movie> animeMovies = new ArrayList<>();
        private List<Movie> tvShowMovies = new ArrayList<>();


        private void setupMovieSection(RecyclerView recyclerView, List<Movie> list, MovieAdapter adapter, Call<ApiResponsePhim> apiCall) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);

            apiCall.enqueue(new Callback<ApiResponsePhim>() {
                @Override
                public void onResponse(Call<ApiResponsePhim> call, Response<ApiResponsePhim> response) {
                    if (response.isSuccessful() && response.body() != null &&
                            response.body().getData() != null &&
                            response.body().getData().getItems() != null) {
                        list.clear();
                        list.addAll(response.body().getData().getItems());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("API", "Response null hoặc không có dữ liệu");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponsePhim> call, Throwable t) {
                    Log.e("API", "Lỗi khi gọi API: " + t.getMessage());
                }
            });
        }



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_home_actaivity);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Chuyen trang khi an vao menu
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeActaivity.this, ProfileActivity.class));
                    return true;
                }

                // Các item khác nếu muốn xử lý
                return false;
            });

            //Banner
            ImageSlider imageSlider = findViewById(R.id.imageSlider);

            List<SlideModel> slideModels = new ArrayList<>();
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=1", "Hình 1", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=2", "Hình 2", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=3", "Hình 3", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=4", "Hình 4", ScaleTypes.FIT));

            imageSlider.setImageList(slideModels, ScaleTypes.FIT);
            imageSlider.startSliding(3000);

            //Phim moi
            recyclerView = findViewById(R.id.recyclerBestMovies);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            adapter = new MovieAdapter(movieList);
            recyclerView.setAdapter(adapter);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://phimapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            KKPhimApi api = retrofit.create(KKPhimApi.class);

            api.getLatestMovies(1).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        movieList.clear();
                        movieList.addAll(response.body().getItems());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("API", "Lỗi khi gọi API: " + t.getMessage());
                }
            });

            // Single Movies
            recyclerSingleMovies = findViewById(R.id.recyclerSingleMovies);
            singleMovieAdapter = new MovieAdapter(singleMovieList);
            setupMovieSection(recyclerSingleMovies, singleMovieList, singleMovieAdapter, api.getSingleMovies());

// Series Movies
            recyclerSeriesMovies = findViewById(R.id.recyclerSeriesMovies);
            seriesMovieAdapter = new MovieAdapter(seriesMovies);
            setupMovieSection(recyclerSeriesMovies, seriesMovies, seriesMovieAdapter, api.getSeriasMovies());

// Anime Movies
            recyclerAnimeMovies = findViewById(R.id.recyclerAnimeMovies);
            animeMovieAdapter = new MovieAdapter(animeMovies);
            setupMovieSection(recyclerAnimeMovies, animeMovies, animeMovieAdapter, api.getAnimeMovies());

// TV Show
            recyclerTvShowMovies = findViewById(R.id.recyclerTvShowMovies);
            tvShowMovieAdapter = new MovieAdapter(tvShowMovies);
            setupMovieSection(recyclerTvShowMovies, tvShowMovies, tvShowMovieAdapter, api.getTvShowMovies());
        }
    }