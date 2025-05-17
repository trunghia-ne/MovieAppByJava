package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.ApiResponsePhim;
import com.example.movieappbyjava.model.ApiResponse;
import com.example.movieappbyjava.model.MovieDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KKPhimApi {
    @GET("danh-sach/phim-moi-cap-nhat")
    Call<ApiResponse> getLatestMovies(@Query("page") int page);

    @GET("v1/api/danh-sach/phim-le")
    Call<ApiResponsePhim> getSingleMovies();

    @GET("v1/api/danh-sach/phim-bo")
    Call<ApiResponsePhim> getSeriasMovies();

    @GET("v1/api/danh-sach/hoat-hinh")
    Call<ApiResponsePhim> getAnimeMovies();


    @GET("v1/api/danh-sach/tv-shows")
    Call<ApiResponsePhim> getTvShowMovies();

    @GET("phim/{slug}")
    Call<MovieDetailResponse> getMovieDetail(@Path("slug") String slug);
}
