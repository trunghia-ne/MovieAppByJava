package com.example.movieappbyjava.model;

import java.util.List;

public class ApiResponsePhim {
    private String status;
    private String msg;
    private DataWrapper data;

    public DataWrapper getData() {
        return data;
    }

    public static class DataWrapper {
        private List<Movie> items;

        public List<Movie> getItems() {
            return items;
        }
    }
}
