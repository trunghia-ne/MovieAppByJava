package com.example.movieappbyjava.model;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KKPhimClient {
    private static final String BASE_URL = "https://phimapi.com/";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
