package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
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

public class SearchResultsFragment extends Fragment {

    private static final String TAG = "SearchResultsFragment";
    private static final String ARG_QUERY = "search_query"; // Key để truyền và nhận query

    private RecyclerView recyclerSearchResults;
    private MovieAdapter searchAdapter;
    private KKPhimApi api;
    private TextView textViewSearchQueryInfo;
    private ProgressBar progressBarSearch;
    private LinearLayout layoutNoResults; // Dùng layout cha để dễ ẩn/hiện
    private TextView textViewNoResults;
    private String currentQuery;

    // Phương thức factory để tạo Fragment với tham số một cách an toàn
    public static SearchResultsFragment newInstance(String query) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận query từ arguments
        if (getArguments() != null) {
            currentQuery = getArguments().getString(ARG_QUERY);
        }
        // Báo cho hệ thống biết rằng Fragment này muốn tham gia vào việc xử lý menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar_search_results);
        // Cần ép kiểu requireActivity() thành AppCompatActivity để dùng setSupportActionBar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ Views
        recyclerSearchResults = view.findViewById(R.id.recycler_search_results);
        textViewSearchQueryInfo = view.findViewById(R.id.text_view_search_query_info);
        progressBarSearch = view.findViewById(R.id.progress_bar_search);
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        textViewNoResults = view.findViewById(R.id.text_view_no_results);


        // Khởi tạo danh sách và adapter
        searchAdapter = new MovieAdapter(new ArrayList<>());

        // Thiết lập LayoutManager và Adapter cho RecyclerView
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        recyclerSearchResults.setAdapter(searchAdapter);

        // Khởi tạo Retrofit và API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

        // Thực hiện tìm kiếm
        if (currentQuery != null && !currentQuery.isEmpty()) {
            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Tìm: \"" + currentQuery + "\"");
            }
            textViewSearchQueryInfo.setText("Kết quả tìm kiếm cho: '" + currentQuery + "'");
            performSearch(currentQuery);
        } else {
            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Lỗi tìm kiếm");
            }
            textViewSearchQueryInfo.setText("Không có từ khóa tìm kiếm.");
            layoutNoResults.setVisibility(View.VISIBLE);
            progressBarSearch.setVisibility(View.GONE);
        }
    }

    private void performSearch(String keyword) {
        progressBarSearch.setVisibility(View.VISIBLE);
        layoutNoResults.setVisibility(View.GONE);
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
                    if (!response.isSuccessful()) {
                        message = "Lỗi từ server: " + response.code();
                    }
                    textViewNoResults.setText(message);
                    layoutNoResults.setVisibility(View.VISIBLE);
                    searchAdapter.clearMovies();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponsePhim> call, @NonNull Throwable t) {
                progressBarSearch.setVisibility(View.GONE);
                textViewNoResults.setText("Lỗi mạng: " + t.getMessage());
                layoutNoResults.setVisibility(View.VISIBLE);
                Log.e(TAG, "Lỗi khi gọi API tìm kiếm: ", t);
                Toast.makeText(requireContext(), "Lỗi mạng. Vui lòng kiểm tra kết nối.", Toast.LENGTH_LONG).show();
                searchAdapter.clearMovies();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý sự kiện click nút back trên Toolbar
        if (item.getItemId() == android.R.id.home) {
            // Quay về màn hình trước đó
            requireActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}