package com.example.movieappbyjava.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.ApiResponseMessage;
import com.example.movieappbyjava.model.Movie;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilmCollection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilmCollection extends Fragment {

    private static final String ARG_COLLECTION_ID = "collectionId";
    private static final String ARG_COLLECTION_NAME = "collectionName";

    private String collectionId;
    private String collectionName;

    private RecyclerView rvMovies;
    private TextView tvCollectionTitle;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();

    public FilmCollection() {
        // Required empty public constructor
    }

    public static FilmCollection newInstance(String collectionId, String collectionName) {
        FilmCollection fragment = new FilmCollection();
        Bundle args = new Bundle();
        args.putString(ARG_COLLECTION_ID, collectionId);
        args.putString(ARG_COLLECTION_NAME, collectionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectionId = getArguments().getString(ARG_COLLECTION_ID);
            collectionName = getArguments().getString(ARG_COLLECTION_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_film_collection, container, false);

        tvCollectionTitle = view.findViewById(R.id.tvCollectionTitle);
        rvMovies = view.findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new MovieAdapter(movieList);
        adapter.setOnMovieLongClickListener(movie -> showDeleteMovieDialog(movie));
        rvMovies.setAdapter(adapter);

        // Hiển thị tên bộ sưu tập lên UI
        if (collectionName != null && !collectionName.isEmpty()) {
            tvCollectionTitle.setText(collectionName);
        }
        Log.d("FilmCollection", "onCreateView called");
        loadMovies();

        return view;
    }

    private void loadMovies() {
        Log.d("FilmCollection", "loadMovies called for collectionId=" + collectionId);
        ApiClient.getApiService().getFilmsByCollectionId(collectionId)
                .enqueue(new Callback<List<Movie>>() {
                    @Override
                    public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateMovies(response.body());
                            Log.d("FilmCollection", "Movies loaded: " + response.body().size());
                        } else {
                            Toast.makeText(getContext(), "Lấy phim thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Movie>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteMovieDialog(Movie movie) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xoá phim")
                .setMessage("Bạn có chắc muốn xoá \"" + movie.getName() + "\" khỏi bộ sưu tập?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    ApiClient.getApiService()
                            .deleteFilmFromCollection(collectionId, movie.getSlug())
                            .enqueue(new Callback<ApiResponseMessage>() {
                                @Override
                                public void onResponse(Call<ApiResponseMessage> call, Response<ApiResponseMessage> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        loadMovies(); // refresh lại danh sách
                                    } else {
                                        Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponseMessage> call, Throwable t) {
                                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

