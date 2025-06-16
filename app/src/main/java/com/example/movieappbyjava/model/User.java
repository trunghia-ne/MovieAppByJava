package com.example.movieappbyjava.model;


public class User {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String image;

    public User(String username, String email, String phone, String image, String password) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.password = password;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImage(String image) {
        this.image = image;
    }
}