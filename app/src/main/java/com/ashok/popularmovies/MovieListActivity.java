package com.ashok.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ashok.popularmovies.data.MovieColumns;
import com.ashok.popularmovies.data.MovieProvider;
import com.ashok.popularmovies.databinding.ActivityMovieListBinding;
import com.ashok.popularmovies.model.Movie;
import com.ashok.popularmovies.model.MoviesResponse;
import com.ashok.popularmovies.network.MoviesDbApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>>,
        MoviesAdapter.OnMovieItemClickListener {

    private static final int MOVIES_LOADER_ID = 10;
    public static final String MOVIES_KEY = "movies";
    public static final int LOAD_FIRST_MOVIE = 21;
    private boolean DATABASE_UPDATED = false;
    private ActivityMovieListBinding movieListBinding;
    private MoviesAdapter moviesAdapter;
    private MyObserver myObserver;
    private SharedPreferences sharedPreferences;
    private boolean mTwoPane;
    private ArrayList<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieListBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_list);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        }

        moviesAdapter = new MoviesAdapter(this);
        moviesAdapter.setOnMovieItemClickListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, getNumColumns());
        movieListBinding.includeList.movieList.setLayoutManager(layoutManager);
        movieListBinding.includeList.movieList.setHasFixedSize(true);
        movieListBinding.includeList.movieList.setAdapter(moviesAdapter);
        movieListBinding.includeList.movieList.addOnScrollListener(
                new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        if (!isShowingFavoritesList()) {
                            getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null,
                                    MovieListActivity.this).forceLoad();
                        }
                    }
                });

        if (savedInstanceState != null) {
            movieList = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
            moviesAdapter.setData(movieList);
            changeListVisibility();
        } else {
            getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, this).forceLoad();
        }

        myObserver = new MyObserver(new Handler());
        getContentResolver().registerContentObserver(MovieProvider.Movies.CONTENT_URI, true, myObserver);
    }

    private boolean isShowingFavoritesList() {
        return getCurrentListType().equals(getString(R.string.pref_favorites_value));
    }

    private int getNumColumns() {
        if (!mTwoPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 4;
        return 2;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DATABASE_UPDATED) {
            if (isShowingFavoritesList()) {
                reloadMovies();
            }
            DATABASE_UPDATED = false;
        }
    }

    private void reloadMovies() {
        moviesAdapter.setData(null);
        movieList.clear();
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this).forceLoad();
    }

    public String getCurrentListType() {
        return sharedPreferences.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_most_popular_value));
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        final String currentListType = getCurrentListType();
        final int pageNo = (movieList.size() / 20) + 1;  //20 results per query
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            public List<Movie> loadInBackground() {
                try {
                    if (currentListType.equals(getString(R.string.pref_favorites_value))) {
                        //load from database
                        Cursor cursor = getContentResolver().query(MovieProvider.Movies.CONTENT_URI,
                                null, null, null, null);
                        if (cursor == null)
                            return null;
                        if (cursor.getCount() != 0) {
                            return getMovies(cursor);
                        }
                        cursor.close();
                    } else {
                        Response<MoviesResponse> response = MoviesDbApi.getClient()
                                .getMovies(currentListType, String.valueOf(pageNo)).execute();
                        return response.body().getMovies();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @NonNull
            private List<Movie> getMovies(Cursor cursor) {
                List<Movie> movies = new ArrayList<>();
                while (cursor.moveToNext()) {
                    Movie movie = new Movie();
                    movie.setId(cursor.getInt(cursor.getColumnIndex(MovieColumns._ID)));
                    movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieColumns.OVERVIEW)));
                    movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieColumns.POSTER_PATH)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieColumns.RELEASE_DATE)));
                    movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieColumns.TITLE)));
                    movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MovieColumns.VOTE_AVERAGE)));
                    movies.add(movie);
                }
                return movies;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        if (data != null) {
            movieList.addAll(data);
            moviesAdapter.setData(movieList);

            //load first movie
            if (mTwoPane) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
                if (fragment == null && movieList.size() > 0) {
                    handler.sendEmptyMessage(LOAD_FIRST_MOVIE);
                }
            }
        }

        changeListVisibility();
    }

    private void changeListVisibility() {
        if (movieList.isEmpty()) {
            movieListBinding.includeList.movieList.setVisibility(View.INVISIBLE);
            movieListBinding.includeList.sadMinion.setVisibility(View.VISIBLE);
        } else {
            movieListBinding.includeList.movieList.setVisibility(View.VISIBLE);
            movieListBinding.includeList.sadMinion.setVisibility(View.GONE);
        }
    }

    // http://stackoverflow.com/questions/12276243/commit-fragment-from-onloadfinished-within-activity
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOAD_FIRST_MOVIE) {
                onMovieItemClicked(movieList.get(0));
            }
        }
    };

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIES_KEY, movieList);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onMovieItemClicked(Movie movie) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_ITEM, movie);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.ARG_ITEM, movie);
            startActivity(intent);
        }
    }

    private class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (mTwoPane && isShowingFavoritesList()) {
                reloadMovies();
            } else {
                DATABASE_UPDATED = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String listType = getCurrentListType();
        if (listType.equals(getString(R.string.pref_most_popular_value))) {
            menu.findItem(R.id.menu_popular).setChecked(true);
        } else if (listType.equals(getString(R.string.pref_highest_rated_value))) {
            menu.findItem(R.id.menu_top_rated).setChecked(true);
        } else if (listType.equals(getString(R.string.pref_favorites_value))) {
            menu.findItem(R.id.menu_favorites).setChecked(true);
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!item.isChecked()) {
            String listType = null;
            switch (item.getItemId()) {
                case R.id.menu_popular:
                    listType = getString(R.string.pref_most_popular_value);
                    break;
                case R.id.menu_top_rated:
                    listType = getString(R.string.pref_highest_rated_value);
                    break;
                case R.id.menu_favorites:
                    listType = getString(R.string.pref_favorites_value);
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
            item.setChecked(true);

            sharedPreferences.edit().putString(getString(R.string.pref_sort_order_key),
                    listType).commit();

            reloadMovies();
            if (mTwoPane) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(myObserver);
    }
}