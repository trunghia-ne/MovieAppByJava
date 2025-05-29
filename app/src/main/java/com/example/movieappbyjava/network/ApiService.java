package com.example.movieappbyjava.network;

import com.example.movieappbyjava.model.Comment;
import com.example.movieappbyjava.model.PaymentUrlResponse;

import java.util.List;

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
    @GET("hello")
    Call<String> getHello();

    @POST("/api/pay")
    Call<PaymentUrlResponse> createPayment(@Query("amount") int amount, @Query("userId") String userId);

    // ✅ Lấy danh sách đánh giá của một phim
    @GET("/api/reviews/{movieId}")
    Call<List<Comment>> getReviews(@Path("movieId") String movieId);

    // ✅ Tính điểm trung bình đánh giá phim
    @GET("/api/reviews/{movieId}/average")
    Call<Double> getAverageRating(@Path("movieId") String movieId);

    // ✅ Gửi đánh giá mới hoặc cập nhật (backend xử lý cập nhật nếu userId + movieId đã tồn tại)
    @POST("/api/reviews")
    Call<Comment> submitReview(@Body Comment comment);

    // ✅ Lấy đánh giá của người dùng cho một phim
    @GET("/api/reviews/user/{userId}/movie/{movieId}")
    Call<Comment> getUserReview(@Path("userId") String userId, @Path("movieId") String movieId);

    // ✅ Xoá đánh giá theo reviewId
    @DELETE("/api/reviews/{reviewId}")
    Call<Void> deleteReview(@Path("reviewId") String reviewId);

    // ✅ Khởi tạo Retrofit client
    ApiService api = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Sử dụng localhost của máy thật khi chạy Android Emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService.class);

}
