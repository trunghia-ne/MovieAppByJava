package com.example.movieappbyjava.model;

import java.util.Date;

public class Comment {
    private String username;
    private String comment;
    private Date createdAt;
    private double rating;
    private String userId;
    private String movieTitle;
    // Default constructor for Firebase
    public Comment() {
    }

    public Comment(String username, String comment, double rating, Date createdAt) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
    }


    public Comment(String username, String comment, double rating, Date createdAt, String userId, String movieTitle) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
        this.userId = userId;
        this.movieTitle = movieTitle;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
    // Generate avatar URL based on username
    public String getAvatarUrl() {
        if (username != null && !username.isEmpty()) {
            return "https://api.pravatar.cc/150?u=" + username.hashCode();
        }
        return "https://api.pravatar.cc/150?u=default";
    }
}