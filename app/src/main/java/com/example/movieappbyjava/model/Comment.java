package com.example.movieappbyjava.model;

public class Comment {
    private String id;           // ID trên server, dùng để update/delete
    private String username;
    private String comment;
    private double rating;
    private String userId;
    private String slug;        // Dùng slug thay movieId
    private String movieTitle;

    // timestamp không cần gửi từ Android nếu để server tự sinh
    private transient java.util.Date timestamp;

    // Constructors
    public Comment() {}

    // Dùng khi gửi review từ Android (bỏ timestamp)
    public Comment(String username, String comment, double rating, String userId, String slug, String movieTitle) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.slug = slug;
        this.movieTitle = movieTitle;
    }

    // Dùng khi hiển thị review từ server (có timestamp)
    public Comment(String id, String username, String comment, double rating, java.util.Date timestamp, String userId, String slug, String movieTitle) {
        this.id = id;
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
        this.userId = userId;
        this.slug = slug;
        this.movieTitle = movieTitle;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public java.util.Date getTimestamp() { return timestamp; }
    public void setTimestamp(java.util.Date timestamp) { this.timestamp = timestamp; }

    // Avatar URL (tạo ảnh đại diện tự động)
    public String getAvatarUrl() {
        if (username != null && !username.isEmpty()) {
            return "https://api.pravatar.cc/150?u=" + username.hashCode();
        }
        return "https://api.pravatar.cc/150?u=default";
    }
}
