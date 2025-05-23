package com.example.movieappbyjava.model;

import java.util.List;

public class MovieDetailResponse {
    private boolean status;
    private String msg;
    private Movie movie;
    private List<EpisodeGroup> episodes;

    public boolean isStatus() { return status; }
    public String getMsg() { return msg; }
    public Movie getMovie() { return movie; }
    public List<EpisodeGroup> getEpisodes() { return episodes; }
}
