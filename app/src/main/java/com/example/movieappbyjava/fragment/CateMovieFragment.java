package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.KKPhimClient;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.network.KKPhimApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CateMovieFragment extends Fragment {

    private String categoryKey;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private KKPhimApi api;
    private TextView title;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public CateMovieFragment() {}

    public static CateMovieFragment newInstance(String categoryKey) {
        CateMovieFragment fragment = new CateMovieFragment();
        Bundle args = new Bundle();
        args.putString("category", categoryKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryKey = getArguments().getString("category");
        }
        api = KKPhimClient.getInstance().create(KKPhimApi.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cate_movie, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCateMovie);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        movieAdapter = new MovieAdapter(new ArrayList<>());
        recyclerView.setAdapter(movieAdapter);

        title = view.findViewById(R.id.title_category);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return; // chỉ load khi scroll xuống

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        currentPage++;
                        loadMovies(categoryKey);
                    }
                }
            }
        });

        loadMovies(categoryKey);
        return view;
    }

    private void loadMovies(String category) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponsePhim> call;

        switch (category) {
            case "phim_le":
                call = api.getSingleMovies(currentPage);
                title.setText("Phim Lẻ");
                break;
            case "phim_bo":
                call = api.getSeriasMovies(currentPage);
                title.setText("Phim Bộ");
                break;
            case "anime":
                call = api.getAnimeMovies(currentPage);
                title.setText("Anime");
                break;
            case "tvshows":
                call = api.getTvShowMovies(currentPage);
                title.setText("TV Shows");
                break;
            default:
                Toast.makeText(getContext(), "Danh mục không hợp lệ", Toast.LENGTH_SHORT).show();
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                return;
        }

        call.enqueue(new Callback<ApiResponsePhim>() {
            @Override
            public void onResponse(Call<ApiResponsePhim> call, Response<ApiResponsePhim> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getItems() != null) {

                    List<Movie> movies = response.body().getData().getItems();
                    Log.d("CateMovieFragment", "Page " + currentPage + ": loaded " + movies.size() + " movies");

                    if (currentPage == 1) {
                        movieAdapter.updateMovies(movies);
                    } else {
                        movieAdapter.appendMovies(movies);
                    }

                } else {
                    isLastPage = true;
                    Log.e("CateMovieFragment", "Response invalid or empty");
                    Toast.makeText(getContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponsePhim> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e("CateMovieFragment", "API error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
