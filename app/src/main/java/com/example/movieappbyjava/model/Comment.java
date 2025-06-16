package com.example.movieappbyjava.model;

import java.util.Date;

public class Comment {
    private String id;           // ID trên server, dùng để update/delete
    private String username;
    private String comment;
    private Double rating;      // Thay double bằng Double để hỗ trợ null
    private String userId;
    private String slug;        // Dùng slug thay movieId
    private String movieTitle;
    private String parentId;    // ID của comment cha (nếu là reply)
    private Date timestamp; // Không gửi lên server, server tự sinh
    private boolean hidden = false; // Thêm trường để đánh dấu bình luận bị ẩn

    // Constructors
    public Comment() {}

    // Dùng khi gửi review từ Android (bỏ timestamp và id)
    public Comment(String username, String comment, Double rating, String userId, String slug, String movieTitle) {
        this.username = username;
        this.comment = comment;
        this.rating = rating != null ? rating : 0.0; // Giá trị mặc định nếu null
        this.userId = userId;
        this.slug = slug;
        this.movieTitle = movieTitle;
    }

    // Dùng khi hiển thị review từ server (có timestamp và id)
    public Comment(String id, String username, String comment, Double rating, Date timestamp, String userId, String slug, String movieTitle, boolean hidden) {
        this.id = id;
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
        this.userId = userId;
        this.slug = slug;
        this.movieTitle = movieTitle;
        this.hidden = hidden;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    // Generate avatar URL based on username
    public String getAvatarUrl() {
        if (username != null && !username.trim().isEmpty()) {
            try {
                return "https://api.pravatar.cc/150?u=" + username.hashCode();
            } catch (Exception e) {
                return "https://api.pravatar.cc/150?u=default";
            }
        }
        return "https://api.pravatar.cc/150?u=default";
    }

    // Kiểm tra có phải là reply không
    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", userId='" + userId + '\'' +
                ", slug='" + slug + '\'' +
                ", movieTitle='" + movieTitle + '\'' +
                ", parentId='" + parentId + '\'' +
                ", timestamp=" + timestamp +
                ", hidden=" + hidden +
                '}';
    }
}