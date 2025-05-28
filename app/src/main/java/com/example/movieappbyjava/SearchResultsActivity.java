package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager; // Hoặc LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.adapter.MovieAdapter; // Adapter của bạn
import com.example.movieappbyjava.model.ApiResponsePhim; // Model API response của bạn
import com.example.movieappbyjava.model.Movie;           // Model Movie của bạn
import com.example.movieappbyjava.network.KKPhimApi;     // Interface API của bạn

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultsActivity";
    public static final String SEARCH_QUERY = "SEARCH_QUERY_EXTRA"; // Key để truyền và nhận query

    private RecyclerView recyclerSearchResults;
    private MovieAdapter searchAdapter;
    private List<Movie> searchResultsList;
    private KKPhimApi api;
    private TextView textViewSearchQueryInfo;
    private ProgressBar progressBarSearch;
    private TextView textViewNoResults;
    private String currentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_search_results);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ Views
        recyclerSearchResults = findViewById(R.id.recycler_search_results);
        textViewSearchQueryInfo = findViewById(R.id.text_view_search_query_info);
        progressBarSearch = findViewById(R.id.progress_bar_search);
        textViewNoResults = findViewById(R.id.text_view_no_results);

        // Khởi tạo danh sách và adapter
        searchResultsList = new ArrayList<>();
        searchAdapter = new MovieAdapter(searchResultsList); // Sử dụng constructor MovieAdapter(List<Movie> movies)

        // Thiết lập LayoutManager cho RecyclerView (ví dụ: GridLayout 2 cột)
        // Hoặc bạn có thể dùng LinearLayoutManager nếu muốn danh sách dọc đơn giản
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 3));
        // recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchAdapter);

        // Khởi tạo Retrofit và API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/") // Đảm bảo base URL là chính xác
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

        // Nhận query từ Intent
        currentQuery = getIntent().getStringExtra(SEARCH_QUERY);

        if (currentQuery != null && !currentQuery.isEmpty()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Tìm: \"" + currentQuery + "\"");
            }
            textViewSearchQueryInfo.setText("Kết quả tìm kiếm cho: '" + currentQuery + "'");
            performSearch(currentQuery);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Lỗi tìm kiếm");
            }
            textViewSearchQueryInfo.setText("Không có từ khóa tìm kiếm.");
            textViewNoResults.setVisibility(View.VISIBLE);
            progressBarSearch.setVisibility(View.GONE);
        }
    }

    private void performSearch(String keyword) {
        progressBarSearch.setVisibility(View.VISIBLE);
        textViewNoResults.setVisibility(View.GONE);
        recyclerSearchResults.setVisibility(View.GONE); // Ẩn kết quả cũ khi đang tìm kiếm mới

        // Gọi API tìm kiếm từ KKPhimApi
        // Bạn có thể điều chỉnh 'limit' (số lượng kết quả) nếu muốn
        api.searchMovies(keyword, 50).enqueue(new Callback<ApiResponsePhim>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponsePhim> call, @NonNull Response<ApiResponsePhim> response) {
                progressBarSearch.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getItems() != null &&
                        !response.body().getData().getItems().isEmpty()) {

                    // searchResultsList.clear(); // Không cần clear nếu dùng searchAdapter.updateMovies
                    // searchResultsList.addAll(response.body().getData().getItems());
                    // searchAdapter.notifyDataSetChanged();

                    searchAdapter.updateMovies(response.body().getData().getItems()); // Dùng phương thức updateMovies của adapter

                    recyclerSearchResults.setVisibility(View.VISIBLE);
                } else {
                    // Xử lý trường hợp không có kết quả hoặc lỗi từ API
                    String message = "Không tìm thấy kết quả.";
                    if (response != null && !response.isSuccessful()) {
                        message = "Lỗi từ server: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                Log.e(TAG, "Error body: " + response.errorBody().string());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    } else if (response == null || response.body() == null || response.body().getData() == null || response.body().getData().getItems() == null) {
                        message = "Dữ liệu trả về không hợp lệ.";
                    }

                    textViewNoResults.setText(message);
                    textViewNoResults.setVisibility(View.VISIBLE);
                    searchAdapter.clearMovies(); // Xóa kết quả cũ nếu có lỗi hoặc không có kết quả
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponsePhim> call, @NonNull Throwable t) {
                progressBarSearch.setVisibility(View.GONE);
                textViewNoResults.setText("Lỗi mạng: " + t.getMessage());
                textViewNoResults.setVisibility(View.VISIBLE);
                Log.e(TAG, "Lỗi khi gọi API tìm kiếm: ", t);
                Toast.makeText(SearchResultsActivity.this, "Lỗi mạng. Vui lòng kiểm tra kết nối.", Toast.LENGTH_LONG).show();
                searchAdapter.clearMovies();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Xử lý sự kiện click nút back trên Toolbar
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng Activity hiện tại và quay về Activity trước đó (HomeActivity)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}