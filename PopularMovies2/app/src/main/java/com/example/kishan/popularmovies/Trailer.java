package com.example.kishan.popularmovies;

/**
 * Created by kishan on 15/12/2016.
 */

public class Trailer
{
    private String name;
    private String key;

    public Trailer(String name, String key)
    {
        this.name = name;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }
}
