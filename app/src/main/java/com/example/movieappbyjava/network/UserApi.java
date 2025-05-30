package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/api/user/{id}")
    Call<User> getUserById(@Path("id") String id);

    @PUT("/api/user/{id}")
    Call<Void> updateUser(@Path("id") String id, @Body User user);
}
