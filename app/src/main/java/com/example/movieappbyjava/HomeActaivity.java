    package com.example.movieappbyjava;

import android.content.Context; // Import cần thiết
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;     // Import cần thiết
import android.view.inputmethod.EditorInfo; // Import cần thiết
import android.view.inputmethod.InputMethodManager; // Import cần thiết
import android.widget.EditText;   // Import cần thiết
import android.widget.TextView;   // Import cần thiết (vì EditText kế thừa từ TextView)

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// ViewPager2 không được sử dụng trong code bạn gửi, có thể xóa import này nếu không dùng
// import androidx.viewpager2.widget.ViewPager2;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponse;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.Movie; // Đảm bảo bạn dùng model Movie
import com.example.movieappbyjava.network.KKPhimApi;
// Đảm bảo SearchResultsActivity.java đã được tạo
// import com.example.movieappbyjava.SearchResultsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
// Arrays import không được sử dụng, có thể xóa nếu không dùng
// import java.util.Arrays;
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

        private EditText searchEditText; // <<<< THÊM BIẾN CHO EDITTEXT TÌM KIẾM
        private KKPhimApi api;           // <<<< ĐƯA BIẾN API RA LÀM BIẾN TOÀN CỤC CỦA CLASS


        // Phương thức setupMovieSection của bạn giữ nguyên
        private void setupMovieSection(RecyclerView rv, List<Movie> list, MovieAdapter movieAdapterInstance, Call<ApiResponsePhim> apiCall) {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(movieAdapterInstance); // Sử dụng biến movieAdapterInstance được truyền vào

            apiCall.enqueue(new Callback<ApiResponsePhim>() {
                @Override
                public void onResponse(Call<ApiResponsePhim> call, Response<ApiResponsePhim> response) {
                    if (response.isSuccessful() && response.body() != null &&
                            response.body().getData() != null &&
                            response.body().getData().getItems() != null) {
                        list.clear();
                        list.addAll(response.body().getData().getItems());
                        movieAdapterInstance.notifyDataSetChanged(); // Sử dụng biến movieAdapterInstance
                    } else {
                        String errorMsg = "Response null hoặc không có dữ liệu";
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
                    Log.e("API_MovieSection", "Lỗi khi gọi API: " + t.getMessage(), t);
                }
            });
        }



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            // Đảm bảo tên layout là chính xác (ví dụ: activity_home_actaivity.xml)
            setContentView(R.layout.activity_home_actaivity);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Khởi tạo Retrofit và API service (chỉ một lần)
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://phimapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(KKPhimApi.class); // Gán vào biến api của class


            // <<<< PHẦN CODE XỬ LÝ EDITTEXT TÌM KIẾM ĐƯỢC THÊM VÀO ĐÂY >>>>
            searchEditText = findViewById(R.id.searchText); // Lấy EditText từ layout bằng ID
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        String query = searchEditText.getText().toString().trim();
                        if (!query.isEmpty()) {
                            // Ẩn bàn phím
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                            }

                            // Chuyển sang SearchResultsActivity
                            Intent intent = new Intent(HomeActaivity.this, SearchResultsActivity.class);
                            // Đảm bảo SearchResultsActivity.SEARCH_QUERY là một hằng số public static final String
                            // đã được định nghĩa trong SearchResultsActivity.java
                            intent.putExtra(SearchResultsActivity.SEARCH_QUERY, query);
                            startActivity(intent);

                            searchEditText.clearFocus(); // Bỏ focus khỏi EditText
                            // searchEditText.setText(""); // Tùy chọn: Xóa nội dung đã nhập trong EditText
                            return true; // Sự kiện đã được xử lý
                        }
                        return false; // Query rỗng, không làm gì
                    }
                    return false; // Các action khác không được xử lý
                }
            });
            // <<<< KẾT THÚC PHẦN CODE XỬ LÝ EDITTEXT TÌM KIẾM >>>>


            // Chuyen trang khi an vao menu
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            // Đảm bảo bạn đã có file menu R.menu.bottom_nav_menu và các ID item
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_profile) { // Thay bằng ID item profile của bạn
                    startActivity(new Intent(HomeActaivity.this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_home) { // Thay bằng ID item home của bạn
                    // Đang ở Home rồi, không cần làm gì hoặc có thể reload
                    return true;
                }
                // Các item khác nếu muốn xử lý
                return false;
            });

            //Banner
            ImageSlider imageSlider = findViewById(R.id.imageSlider);
            List<SlideModel> slideModels = new ArrayList<>();
            // Giữ nguyên slideModels của bạn
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=1", "Hình 1", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=2", "Hình 2", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=3", "Hình 3", ScaleTypes.FIT));
            slideModels.add(new SlideModel("https://picsum.photos/800/400?random=4", "Hình 4", ScaleTypes.FIT));
            imageSlider.setImageList(slideModels, ScaleTypes.FIT);
            // imageSlider.startSliding(3000); // Bỏ comment nếu muốn banner tự trượt


            //Phim moi
            recyclerView = findViewById(R.id.recyclerBestMovies);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // Sử dụng constructor MovieAdapter(List<Movie> movies) của bạn
            adapter = new MovieAdapter(movieList);
            recyclerView.setAdapter(adapter);

            api.getLatestMovies(1).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                        movieList.clear();
                        movieList.addAll(response.body().getItems());
                        adapter.notifyDataSetChanged();
                    } else {
                        String errorMsg = "Response null hoặc không có dữ liệu (LatestMovies)";
                        if (response != null && response.errorBody() != null) {
                            try {
                                errorMsg += " - Error: " + response.errorBody().string();
                            } catch (Exception e) { /* Bỏ qua lỗi đọc errorBody */ }
                        } else if (response != null) {
                            errorMsg += " - Code: " + response.code();
                        }
                        Log.e("API_LatestMovies", errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("API_LatestMovies", "Lỗi khi gọi API: " + t.getMessage(), t);
                }
            });

            // Single Movies
            recyclerSingleMovies = findViewById(R.id.recyclerSingleMovies);
            singleMovieAdapter = new MovieAdapter(singleMovieList); // Sử dụng constructor của bạn
            setupMovieSection(recyclerSingleMovies, singleMovieList, singleMovieAdapter, api.getSingleMovies());

            // Series Movies
            recyclerSeriesMovies = findViewById(R.id.recyclerSeriesMovies);
            seriesMovieAdapter = new MovieAdapter(seriesMovies); // Sử dụng constructor của bạn
            setupMovieSection(recyclerSeriesMovies, seriesMovies, seriesMovieAdapter, api.getSeriasMovies());

            // Anime Movies
            recyclerAnimeMovies = findViewById(R.id.recyclerAnimeMovies);
            animeMovieAdapter = new MovieAdapter(animeMovies); // Sử dụng constructor của bạn
            setupMovieSection(recyclerAnimeMovies, animeMovies, animeMovieAdapter, api.getAnimeMovies());

            // TV Show
            recyclerTvShowMovies = findViewById(R.id.recyclerTvShowMovies);
            tvShowMovieAdapter = new MovieAdapter(tvShowMovies); // Sử dụng constructor của bạn
            setupMovieSection(recyclerTvShowMovies, tvShowMovies, tvShowMovieAdapter, api.getTvShowMovies());
        }
    }