package com.example.movieappbyjava.model;

import com.example.movieappbyjava.network.ApiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // timeout kết nối
            .readTimeout(30, TimeUnit.SECONDS)     // timeout đọc dữ liệu
            .writeTimeout(30, TimeUnit.SECONDS)    // timeout ghi dữ liệu
            .build();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)   // gán OkHttpClient với timeout vào Retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static ApiService apiService = retrofit.create(ApiService.class);

    public static ApiService getApiService() {
        return apiService;
    }
}
