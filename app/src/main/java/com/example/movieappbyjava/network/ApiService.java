package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.PaymentUrlResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/api/hello")
    Call<String> getHello();
}
