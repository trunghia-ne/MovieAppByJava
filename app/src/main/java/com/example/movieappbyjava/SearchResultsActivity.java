package com.example.movieappbyjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Import cho OnBackPressedCallback
import androidx.activity.OnBackPressedCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.network.KKPhimApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultsActivity";
    public static final String SEARCH_QUERY = "SEARCH_QUERY_EXTRA";

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

        Toolbar toolbar = findViewById(R.id.toolbar_search_results);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerSearchResults = findViewById(R.id.recycler_search_results);
        textViewSearchQueryInfo = findViewById(R.id.text_view_search_query_info);
        progressBarSearch = findViewById(R.id.progress_bar_search);
        textViewNoResults = findViewById(R.id.text_view_no_results);

        searchResultsList = new ArrayList<>();
        searchAdapter = new MovieAdapter(searchResultsList);
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerSearchResults.setAdapter(searchAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

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

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void performSearch(String keyword) {
        progressBarSearch.setVisibility(View.VISIBLE);
        textViewNoResults.setVisibility(View.GONE);
        recyclerSearchResults.setVisibility(View.GONE);

        api.searchMovies(keyword, 50).enqueue(new Callback<ApiResponsePhim>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponsePhim> call, @NonNull Response<ApiResponsePhim> response) {
                progressBarSearch.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getItems() != null &&
                        !response.body().getData().getItems().isEmpty()) {
                    searchAdapter.updateMovies(response.body().getData().getItems());
                    recyclerSearchResults.setVisibility(View.VISIBLE);
                } else {
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
                    searchAdapter.clearMovies();
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

    // Xử lý nút back trên Toolbar (nút Up)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // <<<< GHI ĐÈ PHƯƠNG THỨC finish() ĐỂ ĐẢM BẢO setResult LUÔN ĐƯỢC GỌI >>>>
    @Override
    public void finish() {
        setResult(Activity.RESULT_OK); // Báo cho HomeActivity biết là đã hoàn thành và nên xóa text
        super.finish(); // Gọi hành động finish mặc định của Activity
    }

}