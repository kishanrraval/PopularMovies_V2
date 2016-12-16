package com.example.kishan.popularmovies;

import android.provider.BaseColumns;

/**
 * Created by kisha on 16/12/2016.
 */

public final class FavouritesContract
{
    private FavouritesContract(){}

    public static class Favourites implements BaseColumns
    {
        public static final String TABLE_NAME = "favourite";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_POSTER = "poster_path";
        public static final String COLUMN_NAME_RELEASE = "release";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_VOTE = "vote";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
    }
}
