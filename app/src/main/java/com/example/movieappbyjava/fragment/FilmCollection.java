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
}

