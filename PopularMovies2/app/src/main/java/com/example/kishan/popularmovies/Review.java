package com.example.kishan.popularmovies;

/**
 * Created by kishan on 15/12/2016.
 */

public class Review {

    private String author;
    private String review;

    public Review(String author, String review)
    {
        this.author = author;
        this.review = review;
    }

    public String getAuthor() {
        return author;
    }

    public String getReview() {
        return review;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
