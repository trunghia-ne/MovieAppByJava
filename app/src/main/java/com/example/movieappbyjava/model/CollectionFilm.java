package com.example.movieappbyjava.model;
import com.google.firebase.Timestamp;

public class CollectionFilm {
    private String id;              // document ID
    private String collection_name;
    private Timestamp createdAt;
    private String userId;

    public CollectionFilm() {} // Constructor rỗng bắt buộc

    public CollectionFilm(String id, String collection_name, Timestamp createdAt, String userId) {
        this.id = id;
        this.collection_name = collection_name;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollection_name() {
        return collection_name;
    }

    public void setCollection_name(String collection_name) {
        this.collection_name = collection_name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

