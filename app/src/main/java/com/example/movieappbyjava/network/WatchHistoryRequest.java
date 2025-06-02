package com.example.movieappbyjava.network;

public class WatchHistoryRequest {
    private String userId;
    private String movieId;
    private String movieTitle;
    private String trailerUrl;

    public WatchHistoryRequest() {

    }

    public WatchHistoryRequest(String userId, String movieId, String movieTitle, String trailerUrl) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.trailerUrl = trailerUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }
}
