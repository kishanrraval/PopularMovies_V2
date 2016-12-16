package com.example.kishan.popularmovies;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.kishan.popularmovies.FavouritesContract.Favourites.COLUMN_NAME_ID;
import static com.example.kishan.popularmovies.FavouritesContract.Favourites.TABLE_NAME;


public class DetailActivity extends AppCompatActivity {

    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String title, releaseDate, poster, vote, synopsis;

        final String POSTER = "poster_path";
        final String RELEASE = "release_date";
        final String TITLE = "title";
        final String VOTE = "vote_average";
        final String SYNOPSIS = "overview";
        final String ID = "id";


        Bundle bundle = getIntent().getExtras();

        title = bundle.getString(TITLE);
        releaseDate = bundle.getString(RELEASE);
        poster = bundle.getString(POSTER);
        vote = bundle.getString(VOTE);
        synopsis = bundle.getString(SYNOPSIS);
        id = bundle.getString(ID);

        Log.e("id of sel",id);
        ImageView img_poster = (ImageView)findViewById(R.id.mvi_poster);
        Picasso.with(getBaseContext()).load("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + poster).into(img_poster);

        TextView textView_rating = (TextView) findViewById(R.id.mvi_rating);
        textView_rating.setText(getString(R.string.rating) + ": " + vote + "/10");

        TextView textView_releaseDate = (TextView) findViewById(R.id.mvi_releaseDate);
        textView_releaseDate.setText(getString(R.string.release_date) + ": " + releaseDate);

        TextView textView_description = (TextView) findViewById(R.id.mvi_description);
        textView_description.setText(synopsis);

        Button fav = (Button) findViewById(R.id.fav_btn);

        SQLiteDatabase db = new FavouritesDBHelper(getApplicationContext()).getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_NAME_ID+"="+id, null);

        if(c.getCount() == 0)
        {
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                FavouritesDBHelper mDbHelper = new FavouritesDBHelper(getApplicationContext());
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    // Create a new map of values, where column names are the keys
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_NAME_ID, id);
                    values.put(FavouritesContract.Favourites.COLUMN_NAME_POSTER, poster);
                    values.put(FavouritesContract.Favourites.COLUMN_NAME_RELEASE, releaseDate);
                    values.put(FavouritesContract.Favourites.COLUMN_NAME_SYNOPSIS, synopsis);
                    values.put(FavouritesContract.Favourites.COLUMN_NAME_TITLE, title);
                    values.put(FavouritesContract.Favourites.COLUMN_NAME_VOTE, vote);
                    long newRowId = db.insert(TABLE_NAME, null, values);
                    Button fav = (Button) findViewById(R.id.fav_btn);
                    fav.setText("Added to Favourites");
                }
            });
        }
        else
        {
            fav.setText("Added to Favourites");

            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FavouritesDBHelper mDbHelper = new FavouritesDBHelper(getApplicationContext());
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    if(db.delete(TABLE_NAME, COLUMN_NAME_ID+ "=" + id, null) <= 0)
                        Log.e("Error Deleting row", id);

                    Button fav = (Button) findViewById(R.id.fav_btn);
                    fav.setText("Add to Favourites");
                }
            });
        }




        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver  , new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            boolean isConnected = ni != null &&
                    ni.isConnectedOrConnecting();


            if (isConnected) {
                updateVideos();
                updateReviews();
            } else {
                Snackbar.make(findViewById(R.id.parent_View), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Start Wifi", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.holo_blue_light)).show();
            }
        }
    };

    private void updateReviews()
    {
        FetchReviews fetchReview = new FetchReviews();
        fetchReview.execute(id);
    }

    private void updateVideos()
    {
        FetchTrailers fetchTrailer = new FetchTrailers();
        fetchTrailer.execute(id);
    }


    class FetchTrailers extends AsyncTask<String, Void, Trailer[]>
    {

        @Override
        protected void onPostExecute(final Trailer[] trailers) {


            if(trailers != null)
            {
                View.OnClickListener buttonListener1 = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v="+trailers[0])));
                    }
                };
                View.OnClickListener buttonListener2 = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v="+trailers[1])));
                    }
                };
                View.OnClickListener buttonListener3 = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v="+trailers[2])));
                    }
                };

                LinearLayout trailerLayout = (LinearLayout) findViewById(R.id.mvi_trailerBTNs);
                Button b1 = (Button) findViewById(R.id.youtube_btn1);
                Button b2 = (Button) findViewById(R.id.youtube_btn2);
                Button b3 = (Button) findViewById(R.id.youtube_btn3);

                b1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[0].getKey())));
                    }
                });
                b2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[1].getKey())));
                    }
                });
                b3.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[2].getKey())));
                    }
                });

                if(trailers.length > 2)
                {
                    b3.setText(trailers[2].getName());
                    b3.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[2].getKey())));
                        }
                    });
                }

                if(trailers.length > 1)
                {
                    b2.setText(trailers[1].getName());
                    b2.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[1].getKey())));
                        }
                    });
                }

                if(trailers.length > 0)
                {
                    b1.setText(trailers[0].getName());
                    b1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+ trailers[0].getKey())));
                        }
                    });
                }

                if(trailers.length == 2)
                    trailerLayout.removeView(b3); // this row will remove the view
                else if(trailers.length == 1)
                {
                    trailerLayout.removeView(b2);
                    trailerLayout.removeView(b3);
                }


            }
        }

        private JSONArray JSONArray_vid;
        private JSONObject vid_JSON;
        @Override
        protected Trailer[] doInBackground(String... params) {

            Log.e("DoINBackgeournd"," Started");
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String dataIn = null;

            if(params.length == 0)
            {
                return null;
            }

            try
            {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] +"/videos?";
                final String api = "api_key";

                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(api, getResources().getString(R.string.api_key))
                        .build();


                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line=bufferedReader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0)
                {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                dataIn = buffer.toString(); //Storing Data coming into String

                Log.v("LOG TAG","API Data : "+dataIn);

                vid_JSON = new JSONObject(dataIn);
                JSONArray_vid = vid_JSON.getJSONArray("results");

            }
            catch (MalformedURLException e)
            {
                Log.e("URL Connection error", "Error Closing Stream", e);
            }
            catch (IOException e)
            {
                Log.e("IOException", "Error", e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("PlaceHolderFragment","Error Closing Stream", e);
                    }
                }

            }

            try {
                return getJSONTrailerData();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }

        private Trailer[] getJSONTrailerData() throws JSONException
        {
            final String VID_KEY = "key";
            final String VID_NAME = "name";

            int length = 3;
            if(JSONArray_vid.length() < length)
                length = JSONArray_vid.length();

            Trailer[] trList = new Trailer[length];
            for(int i = 0; i < length; i++)
            {
                String key, name;

                JSONObject mvi = JSONArray_vid.getJSONObject(i);

                key = mvi.getString(VID_KEY);
                name = mvi.getString(VID_NAME);

                trList[i] = new Trailer(name, key);
            }
            return trList;
        }
    }


    class FetchReviews extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Data",s);
            TextView b1 = (TextView) findViewById(R.id.detail_reviewText);
            b1.setText(s);
        }

        private JSONArray JSONArray_rev;
        private JSONObject JSON_rev;
        @Override
        protected String doInBackground(String... params) {
            Log.e("DoINBackgeournd review"," Started");
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String dataIn = null;

            if(params.length == 0)
            {
                return null;
            }

            try
            {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] +"/reviews?";
                final String api = "api_key";

                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(api, getResources().getString(R.string.api_key))
                        .build();


                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line=bufferedReader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0)
                {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                dataIn = buffer.toString(); //Storing Data coming into String

                Log.e("LOG TAG","API Data : "+dataIn);

                JSON_rev = new JSONObject(dataIn);
                JSONArray_rev = JSON_rev.getJSONArray("results");

            }
            catch (MalformedURLException e)
            {
                Log.e("URL Connection error", "Error Closing Stream", e);
            }
            catch (IOException e)
            {
                Log.e("IOException", "Error", e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("PlaceHolderFragment","Error Closing Stream", e);
                    }
                }
            }

            try {
                return getJSONReviewData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getJSONReviewData() throws JSONException
        {
            final String REV_AUTHOR = "author";
            final String REV_CONTENT = "content";
            final String REV_RESULTS = "total_results";
            int REV_NO;
            String out = "";
            REV_NO = JSON_rev.getInt(REV_RESULTS);
            if(REV_NO == 0)
                return "No Reviews Yet.";
            for(int i=0 ; i < REV_NO ; i++)
            {
                JSONObject rev = JSONArray_rev.getJSONObject(i);
                out += "Author: " + rev.getString(REV_AUTHOR) + "\n";
                out += "Review: " + rev.getString(REV_CONTENT) + "\n\n";
                out += "---\n";
            }
            return out;
        }
    }


}
