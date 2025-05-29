    package com.example.movieappbyjava.model;

    import java.util.Date;

    public class Comment {
        private String id;           // ID trên server, cần thiết cho update/delete
        private String username;
        private String comment;
        private Date timestamp;     // Thay createdAt để khớp với server
        private double rating;
        private String userId;
        private String slug;        // Dùng slug thay cho movieId
        private String movieTitle;

        // Constructors
        public Comment() {}

        public Comment(String username, String comment, double rating, Date timestamp) {
            this.username = username;
            this.comment = comment;
            this.rating = rating;
            this.timestamp = timestamp;
        }

        public Comment(String username, String comment, double rating, Date timestamp, String userId, String slug, String movieTitle) {
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

        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }

        public String getMovieTitle() { return movieTitle; }
        public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

        // Generate avatar URL based on username
        public String getAvatarUrl() {
            if (username != null && !username.isEmpty()) {
                return "https://api.pravatar.cc/150?u=" + username.hashCode();
            }
            return "https://api.pravatar.cc/150?u=default";
        }
    }
