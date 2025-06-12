package com.example.movieappbyjava.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.KKPhimClient;
import com.example.movieappbyjava.network.KKPhimApi;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchGenreFragment extends Fragment {
    private static final String ARG_GENRE = "genre";
    private String genre;

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private KKPhimApi api;
    private TextView title;

    public static SearchGenreFragment newInstance(String genre) {
        SearchGenreFragment fragment = new SearchGenreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GENRE, genre);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        genre = getArguments() != null ? getArguments().getString(ARG_GENRE) : "";
        api = KKPhimClient.getInstance().create(KKPhimApi.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_genre, container, false);
        title = view.findViewById(R.id.titleGenre);
        recyclerView = view.findViewById(R.id.recyclerViewGenre);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MovieAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        title.setText(genre.substring(0, 1).toUpperCase() + genre.substring(1));

        loadGenreMovies(genre);
        return view;
    }

    private void loadGenreMovies(String genre) {
        api.searchMovies(genre, 30).enqueue(new Callback<ApiResponsePhim>() {
            @Override
            public void onResponse(Call<ApiResponsePhim> call, Response<ApiResponsePhim> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getItems() != null) {

                    adapter.updateMovies(response.body().getData().getItems());
                }
            }

            @Override
            public void onFailure(Call<ApiResponsePhim> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
