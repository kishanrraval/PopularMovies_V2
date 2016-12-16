package com.example.kishan.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kisha on 16/12/2016.
 */

public class FavouritesDBHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Favourites.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavouritesContract.Favourites.TABLE_NAME + " (" +
                    FavouritesContract.Favourites._ID + " INTEGER PRIMARY KEY," +
                    FavouritesContract.Favourites.COLUMN_NAME_ID + TEXT_TYPE + COMMA_SEP +
                    FavouritesContract.Favourites.COLUMN_NAME_POSTER + TEXT_TYPE + COMMA_SEP +
                    FavouritesContract.Favourites.COLUMN_NAME_RELEASE + TEXT_TYPE + COMMA_SEP +
                    FavouritesContract.Favourites.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    FavouritesContract.Favourites.COLUMN_NAME_VOTE + TEXT_TYPE + COMMA_SEP +
                    FavouritesContract.Favourites.COLUMN_NAME_SYNOPSIS + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FavouritesContract.Favourites.TABLE_NAME;

    public FavouritesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
