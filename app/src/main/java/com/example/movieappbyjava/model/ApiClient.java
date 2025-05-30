package com.example.movieappbyjava.model;

import com.example.movieappbyjava.network.ApiService;
import com.example.movieappbyjava.network.UserApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080";


    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static ApiService apiService = retrofit.create(ApiService.class);

    private static UserApi userApi = retrofit.create(UserApi.class);

    public static ApiService getApiService() {
        return apiService;
    }

    public static UserApi getUserApi() {return userApi;}
}
