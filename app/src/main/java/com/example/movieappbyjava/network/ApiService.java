package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.ApiResponseMessage;
import com.example.movieappbyjava.model.CollectionFilm;
import com.example.movieappbyjava.model.Comment;
import com.example.movieappbyjava.model.Movie;
import com.example.movieappbyjava.model.Payment;
import com.example.movieappbyjava.model.PaymentUrlResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/pay")
    Call<PaymentUrlResponse> createPayment(@Query("amount") int amount, @Query("userId") String userId);

    @GET("api/revenue")
    Call<List<Payment>> getPayments();

    @GET("api/revenue/total")
    Call<Long> getTotalRevenue();

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

    @POST("api/reviews/reply")
    Call<Comment> addReply(@Body Comment reply);

    @GET("api/reviews/{commentId}/replies")
    Call<List<Comment>> getReplies(@Path("commentId") String commentId);

    @PUT("api/reviews/{reviewId}/reply")
    Call<Comment> updateReply(@Path("reviewId") String reviewId, @Body Comment reply);

    @GET("api/getCollectionsByUser")
    Call<List<CollectionFilm>> getCollectionsByUser(@Query("userId") String userId);

    @POST("api/addFilmToCollection")
    Call<ApiResponseMessage> addFilmToCollection(
            @Query("collectionId") String collectionId,
            @Body Movie movie
    );

    @GET("api/checkFilmInUserCollections")
    Call<Boolean> isFilmInUserCollections(@Query("userId") String userId, @Query("slug") String slug);

    @POST("api/addCollection")
    Call<ApiResponseMessage> addCollection(@Query("userId") String userId, @Body CollectionFilm collectionFilm);

    @GET("api/getFilmsByCollectionId")
    Call<List<Movie>> getFilmsByCollectionId(@Query("collectionId") String collectionId);

    @DELETE("api/deleteCollection")
    Call<ApiResponseMessage> deleteCollection(@Query("collectionId") String collectionId);

    @DELETE("api/deleteFilmFromCollection")
    Call<ApiResponseMessage> deleteFilmFromCollection(
            @Query("collectionId") String collectionId,
            @Query("slug") String slug
    );
    @PUT("api/reviews/{reviewId}/hide")
    Call<Void> hideComment(@Path("reviewId") String reviewId);
    @PUT("api/reviews/{reviewId}/show")
    Call<Void> showComment(@Path("reviewId") String reviewId);
    @GET("api/reviews/all")
    Call<List<Comment>> getAllComments();
}