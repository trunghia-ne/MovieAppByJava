package com.example.movieappbyjava.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/api/hello")
    Call<String> getHello();
}
