package com.example.movieappbyjava;

import android.app.Activity; // Cần cho Activity.RESULT_OK nếu bạn muốn kiểm tra resultCode
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<Movie> seriesMoviesList = new ArrayList<>(); // Đổi tên để tránh trùng với biến đã khai báo
    private List<Movie> animeMoviesList = new ArrayList<>();  // Đổi tên
    private List<Movie> tvShowMoviesList = new ArrayList<>(); // Đổi tên

    private EditText searchEditText;
    private KKPhimApi api;
    private ActivityResultLauncher<Intent> searchResultsLauncher;

    private void setupMovieSection(RecyclerView rv, List<Movie> list, MovieAdapter movieAdapterInstance, Call<ApiResponsePhim> apiCall) {
        // Khởi tạo adapter nếu nó chưa được khởi tạo
        if (movieAdapterInstance == null) {
            // Giả sử MovieAdapter có constructor nhận List<Movie>
            movieAdapterInstance = new MovieAdapter(list);
            // Nếu MovieAdapter cần Context, bạn có thể truyền this:
            // movieAdapterInstance = new MovieAdapter(list, this);
        }
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(movieAdapterInstance);

        final MovieAdapter finalAdapter = movieAdapterInstance; // Để sử dụng trong inner class

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
                        } catch (Exception e) { /* Bỏ qua lỗi đọc errorBody */ }
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
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_actaivity); // Đảm bảo tên layout chính xác
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Retrofit và API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

        // Đăng ký ActivityResultLauncher để xử lý kết quả từ SearchResultsActivity
        searchResultsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Callback này sẽ được gọi khi SearchResultsActivity đóng lại.
                    // Không cần kiểm tra result.getResultCode() nếu bạn luôn muốn xóa text.
                    if (searchEditText != null) {
                        searchEditText.setText(""); // Xóa nội dung EditText
                    }
                }
        );

        // Setup EditText tìm kiếm
        searchEditText = findViewById(R.id.searchText);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                        }

                        Intent intent = new Intent(HomeActaivity.this, SearchResultsActivity.class);
                        intent.putExtra(SearchResultsActivity.SEARCH_QUERY, query);

                        searchResultsLauncher.launch(intent); // Sử dụng launcher

                        searchEditText.clearFocus();
                        return true;
                    }
                    return false;
                }
                return false;
            }
        });

        // Setup BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) { // Thay bằng ID item profile của bạn
                startActivity(new Intent(HomeActaivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_home) { // Thay bằng ID item home của bạn
                // Đang ở Home rồi
                return true;
            }
            // Thêm các item khác nếu có trong menu của bạn
            return false;
        });

        // Setup ImageSlider (Banner)
        ImageSlider imageSlider = findViewById(R.id.imageSlider);
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=1", "Hình 1", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=2", "Hình 2", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=3", "Hình 3", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://picsum.photos/800/400?random=4", "Hình 4", ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
        // imageSlider.startSliding(3000); // Bỏ comment nếu muốn tự động trượt

        // Setup RecyclerView cho Phim mới (Latest Movies)
        recyclerViewLatestMovies = findViewById(R.id.recyclerBestMovies); // Đảm bảo ID này đúng với layout của bạn
        recyclerViewLatestMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        latestMoviesAdapter = new MovieAdapter(latestMovieList); // Sử dụng constructor MovieAdapter(List<Movie> movies)
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
                        } catch (Exception e) { /* Bỏ qua */ }
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

        // Setup các mục phim khác sử dụng setupMovieSection
        recyclerSingleMovies = findViewById(R.id.recyclerSingleMovies);
        singleMovieAdapter = new MovieAdapter(singleMovieList); // Khởi tạo adapter
        setupMovieSection(recyclerSingleMovies, singleMovieList, singleMovieAdapter, api.getSingleMovies());

        recyclerSeriesMovies = findViewById(R.id.recyclerSeriesMovies);
        seriesMovieAdapter = new MovieAdapter(seriesMoviesList); // Khởi tạo adapter
        setupMovieSection(recyclerSeriesMovies, seriesMoviesList, seriesMovieAdapter, api.getSeriasMovies());

        recyclerAnimeMovies = findViewById(R.id.recyclerAnimeMovies);
        animeMovieAdapter = new MovieAdapter(animeMoviesList); // Khởi tạo adapter
        setupMovieSection(recyclerAnimeMovies, animeMoviesList, animeMovieAdapter, api.getAnimeMovies());

        recyclerTvShowMovies = findViewById(R.id.recyclerTvShowMovies);
        tvShowMovieAdapter = new MovieAdapter(tvShowMoviesList); // Khởi tạo adapter
        setupMovieSection(recyclerTvShowMovies, tvShowMoviesList, tvShowMovieAdapter, api.getTvShowMovies());
    }
}