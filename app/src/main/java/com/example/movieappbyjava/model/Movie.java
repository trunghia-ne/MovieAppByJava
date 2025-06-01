package com.example.movieappbyjava.model;

import java.util.List;

public class Movie {
    private String _id;
    private String name;
    private String slug;
    private String origin_name;
    private String content;
    private String type;
    private String status;
    private String poster_url;
    private String thumb_url;
    private String trailer_url;
    private String time;
    private String episode_current;
    private String episode_total;
    private int year;
    private int view;
    private List<String> actor;
    private List<String> director;
    private List<Category> category;
    private List<Country> country;

    public Movie() {

    }
    public Movie(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    public void setPoster_url(String poster_url) {this.poster_url = poster_url; }
    public String getId() { return _id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getOrigin_name() { return origin_name; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getPoster_url() { return poster_url; }
    public String getThumb_url() { return thumb_url; }
    public String getTrailer_url() { return trailer_url; }
    public String getTime() { return time; }
    public String getEpisode_current() { return episode_current; }
    public String getEpisode_total() { return episode_total; }
    public int getYear() { return year; }
    public int getView() { return view; }

    public List<String> getActor() { return actor; }
    public List<String> getDirector() { return director; }
    public List<Category> getCategory() { return category; }
    public List<Country> getCountry() { return country; }
}
