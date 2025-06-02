package com.example.movieappbyjava.model;


import java.util.Date;

public class WatchHistory {
    private String userId;
    private String movieId;
    private String name;
    private String posterUrl;
    private long watchedDuration;
    private long totalDuration;
    private String status;
    private Date watchedAt;
    private String device;

    public WatchHistory() {
    }

    public WatchHistory(String userId, String movieId, String name, String posterUrl,
                        long watchedDuration, long totalDuration, String status,
                        Date watchedAt, String device) {
        this.userId = userId;
        this.movieId = movieId;
        this.name = name;
        this.posterUrl = posterUrl;
        this.watchedDuration = watchedDuration;
        this.totalDuration = totalDuration;
        this.status = status;
        this.watchedAt = watchedAt;
        this.device = device;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setWatchedDuration(long watchedDuration) {
        this.watchedDuration = watchedDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWatchedAt(Date watchedAt) {
        this.watchedAt = watchedAt;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getUserId() {
        return userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getName() {
        return name;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public long getWatchedDuration() {
        return watchedDuration;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public String getStatus() {
        return status;
    }

    public Date getWatchedAt() {
        return watchedAt;
    }

    public String getDevice() {
        return device;
    }
}
