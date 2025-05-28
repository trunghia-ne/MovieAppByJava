package com.example.movieappbyjava.model;

import java.util.Date;

public class Comment {
    public String username;
    public String comment;
    public Date createdAt;
    private double rating;
    public Comment(String username, String comment, double rating, Date createdAt) {
        this.username = username;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and setters for better encapsulation
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

}
