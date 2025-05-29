package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.PaymentUrlResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("hello")
    Call<String> getHello();

    @POST("/api/pay")
    Call<PaymentUrlResponse> createPayment(@Query("amount") int amount, @Query("userId") String userId);


}
