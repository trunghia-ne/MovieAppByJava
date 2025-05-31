    package com.example.movieappbyjava.network;

    import com.example.movieappbyjava.model.ApiResponseMessage;
    import com.example.movieappbyjava.model.CollectionFilm;
    import com.example.movieappbyjava.model.Comment;
    import com.example.movieappbyjava.model.Movie;
    import com.example.movieappbyjava.model.PaymentUrlResponse;

    import java.util.List;
    import java.util.Map;

    import retrofit2.Call;
    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;
    import retrofit2.http.Body;
    import retrofit2.http.DELETE;
    import retrofit2.http.GET;
    import retrofit2.http.POST;
    import retrofit2.http.Path;
    import retrofit2.http.Query;

    public interface ApiService {
        @POST("/api/pay")
        Call<PaymentUrlResponse> createPayment(@Query("amount") int amount, @Query("userId") String userId);
        @GET("api/reviews/{slug}")
        Call<List<Comment>> getReviews(@Path("slug") String slug);

        @GET("api/reviews/{slug}/average")
        Call<Map<String, Object>> getAverageRating(@Path("slug") String slug);

        @POST("api/reviews")
        Call<Comment> submitReview(@Body Comment comment);

        @GET("api/reviews/user/{userId}/movie/{slug}")
        Call<Comment> getUserReview(@Path("userId") String userId, @Path("slug") String slug);

        @DELETE("api/reviews/{reviewId}")
        Call<Void> deleteReview(@Path("reviewId") String reviewId);

        @GET("api/getCollectionsByUser")
        Call<List<CollectionFilm>> getCollectionsByUser(@Query("userId") String userId);

        @POST("api/addFilmToCollection")
        Call<ApiResponseMessage> addFilmToCollection(
                @Query("collectionId") String collectionId,
                @Body Movie movie
        );

        @GET("api/checkFilmInUserCollections")
        Call<Boolean> isFilmInUserCollections(@Query("userId") String userId, @Query("slug") String slug);
    }