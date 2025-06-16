package com.example.movieappbyjava.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.SearchInputActivity;
import com.example.movieappbyjava.adapter.MovieAdapter;
import com.example.movieappbyjava.model.ApiResponse;
import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.network.KKPhimApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewLatestMovies, recyclerSingleMovies, recyclerSeriesMovies, recyclerAnimeMovies, recyclerTvShowMovies;
    private MovieAdapter latestMoviesAdapter, singleMovieAdapter, seriesMovieAdapter, animeMovieAdapter, tvShowMovieAdapter;
    private final List<Movie> latestMovieList = new ArrayList<>();
    private final List<Movie> singleMovieList = new ArrayList<>();
    private final List<Movie> seriesMoviesList = new ArrayList<>();
    private final List<Movie> animeMoviesList = new ArrayList<>();
    private final List<Movie> tvShowMoviesList = new ArrayList<>();

    private TextView tabAll, tabDrama, tabRomance, tabHorror ,tabComedy;

    private KKPhimApi api;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Retrofit init
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://phimapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KKPhimApi.class);

        setupUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
    }

    private void setupUI(View view) {
        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar_home);
        toolbar.inflateMenu(R.menu.home_toolbar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_home_search) {
                startActivity(new Intent(getActivity(), SearchInputActivity.class));
                return true;
            }
            return false;
        });

        // Image Slider
        ImageSlider imageSlider = view.findViewById(R.id.imageSlider);
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://th.bing.com/th/id/R.5b20f7749e3029daa7a906e4416bf49d?rik=E4IeKlJbqGuQrg&pid=ImgRaw&r=0", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://www.vintagemovieposters.co.uk/wp-content/uploads/2021/03/IMG_1741-1536x1144.jpeg", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://www.slugmag.com/wp/wp-content/uploads/2017/04/screen-shot-2016-11-27-at-1-42-50-pm.png", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://th.bing.com/th/id/OIP.cZpUAaVR9bn1rKscWipudgHaEK?r=0&rs=1&pid=ImgDetMain",ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // RecyclerView init
        recyclerViewLatestMovies = view.findViewById(R.id.recyclerBestMovies);
        recyclerSingleMovies = view.findViewById(R.id.recyclerSingleMovies);
        recyclerSeriesMovies = view.findViewById(R.id.recyclerSeriesMovies);
        recyclerAnimeMovies = view.findViewById(R.id.recyclerAnimeMovies);
        recyclerTvShowMovies = view.findViewById(R.id.recyclerTvShowMovies);

        latestMoviesAdapter = new MovieAdapter(latestMovieList);
        singleMovieAdapter = new MovieAdapter(singleMovieList);
        seriesMovieAdapter = new MovieAdapter(seriesMoviesList);
        animeMovieAdapter = new MovieAdapter(animeMoviesList);
        tvShowMovieAdapter = new MovieAdapter(tvShowMoviesList);

        tabAll = view.findViewById(R.id.tab_all);
        tabDrama = view.findViewById(R.id.tab_drama);
        tabRomance = view.findViewById(R.id.tab_romance);
        tabHorror = view.findViewById(R.id.tab_horror);
        tabComedy = view.findViewById(R.id.tab_comedy);

        tabDrama.setOnClickListener(v -> openGenreFragment("drama"));
        tabRomance.setOnClickListener(v -> openGenreFragment("romance"));
        tabComedy.setOnClickListener(v -> openGenreFragment("comedy"));
        tabHorror.setOnClickListener(v -> openGenreFragment("horror"));


        recyclerViewLatestMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSingleMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSeriesMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerAnimeMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTvShowMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        recyclerViewLatestMovies.setAdapter(latestMoviesAdapter);
        recyclerSingleMovies.setAdapter(singleMovieAdapter);
        recyclerSeriesMovies.setAdapter(seriesMovieAdapter);
        recyclerAnimeMovies.setAdapter(animeMovieAdapter);
        recyclerTvShowMovies.setAdapter(tvShowMovieAdapter);
    }

    private void fetchData() {
        // Latest movies
        api.getLatestMovies(1).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    latestMovieList.clear();
                    latestMovieList.addAll(response.body().getItems());
                    latestMoviesAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API_LatestMovies", "Không có dữ liệu latest movies");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_LatestMovies", "Lỗi gọi API latest: " + t.getMessage(), t);
            }
        });

        setupMovieSection(recyclerSingleMovies, singleMovieList, singleMovieAdapter, api.getSingleMovies(1));
        setupMovieSection(recyclerSeriesMovies, seriesMoviesList, seriesMovieAdapter, api.getSeriasMovies(1));
        setupMovieSection(recyclerAnimeMovies, animeMoviesList, animeMovieAdapter, api.getAnimeMovies(1));
        setupMovieSection(recyclerTvShowMovies, tvShowMoviesList, tvShowMovieAdapter, api.getTvShowMovies(1));
    }

    private void setupMovieSection(RecyclerView rv, List<Movie> list, MovieAdapter adapter, Call<ApiResponsePhim> apiCall) {
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
                    Log.e("API_MovieSection", "Không có dữ liệu cho section");
                }
            }

            @Override
            public void onFailure(Call<ApiResponsePhim> call, Throwable t) {
                Log.e("API_MovieSection", "Lỗi API section: " + t.getMessage(), t);
            }
        });
    }


    private void openGenreFragment(String genre) {
        Fragment fragment = SearchGenreFragment.newInstance(genre);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
