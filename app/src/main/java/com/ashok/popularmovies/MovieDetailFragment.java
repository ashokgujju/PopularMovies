package com.ashok.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ashok.popularmovies.data.MovieColumns;
import com.ashok.popularmovies.data.MovieProvider;
import com.ashok.popularmovies.databinding.MovieDetailBinding;
import com.ashok.popularmovies.model.Movie;
import com.ashok.popularmovies.model.Review;
import com.ashok.popularmovies.model.ReviewsResponse;
import com.ashok.popularmovies.model.Trailer;
import com.ashok.popularmovies.model.TrailersResponse;
import com.ashok.popularmovies.network.MoviesDbApi;
import com.ashok.popularmovies.network.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import retrofit2.Response;

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        TrailersAdapter.OnTrailerItemClickListener {
    public static final String ARG_ITEM = "movie_obj";
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    public static final String MAX_RATING = "/10";
    public static final String TRAILERS = "trailers";
    public static final String REVIEWS = "reviews";
    public static final String IS_FAVORITE = "isfavorite";
    private final int TRAILERS_LOADER_ID = 11;
    private final int REVIEWS_LOADER_ID = 12;
    private final int IS_FAVORITE_LOADER_ID = 13;

    private MovieDetailBinding binding;
    private Movie movie;

    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;
    private boolean isFavoriteMovie = false;
    private ArrayList<Trailer> trailerArrayList = new ArrayList<>();
    private ArrayList<Review> reviewArrayList = new ArrayList<>();

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM)) {
            movie = getArguments().getParcelable(ARG_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.movie_detail, container, false);
        binding.title.setText(movie.getTitle());
        binding.userRating.setText(String.valueOf(movie.getVoteAverage()).concat(MAX_RATING));
        binding.synopsis.setText(movie.getOverview());
        Picasso.with(getContext()).load(NetworkUtils.buildImgUri(movie.getPosterPath()))
                .placeholder(R.drawable.ic_movie_poster).into(binding.poster);

        StringTokenizer tokenizer = new StringTokenizer(movie.getReleaseDate(), "-");
        binding.releaseDate.setText(tokenizer.nextToken());
        binding.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFavoriteMovie = !isFavoriteMovie;
                if (isFavoriteMovie) {
                    binding.favorite.setImageResource(R.drawable.ic_heart);
                    saveMovieToDatabase();
                } else {
                    binding.favorite.setImageResource(R.drawable.ic_heart_outline);
                    deleteMovieFromDatabase();
                }
            }
        });

        setupRecyclerViews();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        trailersAdapter = new TrailersAdapter(getContext());
        trailersAdapter.setOnTrailerItemListener(this);
        LinearLayoutManager trailerListLayoutMgr = new LinearLayoutManager(getContext());
        trailerListLayoutMgr.setOrientation(LinearLayoutManager.VERTICAL);
        binding.trailersList.setLayoutManager(trailerListLayoutMgr);
        binding.trailersList.setHasFixedSize(true);
        binding.trailersList.setAdapter(trailersAdapter);
        binding.trailersList.setNestedScrollingEnabled(false);


        reviewsAdapter = new ReviewsAdapter(getContext());
        LinearLayoutManager reviewsListLayoutMgr = new LinearLayoutManager(getContext());
        reviewsListLayoutMgr.setOrientation(LinearLayoutManager.VERTICAL);
        binding.reviewsList.setLayoutManager(reviewsListLayoutMgr);
        binding.reviewsList.setHasFixedSize(true);
        binding.reviewsList.setAdapter(reviewsAdapter);
        binding.reviewsList.setNestedScrollingEnabled(false);
    }

    private void deleteMovieFromDatabase() {
        try {
            getActivity().getContentResolver().delete(MovieProvider.Movies.withId(movie.getId()), null, null);
        } catch (Exception e) {
        }
    }

    private void saveMovieToDatabase() {
        ContentValues cv = new ContentValues();
        cv.put(MovieColumns._ID, movie.getId());
        cv.put(MovieColumns.OVERVIEW, movie.getOverview());
        cv.put(MovieColumns.POSTER_PATH, movie.getPosterPath());
        cv.put(MovieColumns.RELEASE_DATE, movie.getReleaseDate());
        cv.put(MovieColumns.TITLE, movie.getTitle());
        cv.put(MovieColumns.VOTE_AVERAGE, movie.getVoteAverage());
        try {
            getActivity().getContentResolver().insert(MovieProvider.Movies.withId(movie.getId()), cv);
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            trailerArrayList = savedInstanceState.getParcelableArrayList(TRAILERS);
            reviewArrayList = savedInstanceState.getParcelableArrayList(REVIEWS);
            isFavoriteMovie = savedInstanceState.getBoolean(IS_FAVORITE);

            trailersAdapter.setTrailers(trailerArrayList);
            reviewsAdapter.setReviews(reviewArrayList);

            invalidateReviews();
            invalidateTrailers();
            invalidateIsFavorite();
        } else {
            getActivity().getSupportLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this).forceLoad();
            getActivity().getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this).forceLoad();
            getActivity().getSupportLoaderManager().initLoader(IS_FAVORITE_LOADER_ID, null, this).forceLoad();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRAILERS, trailerArrayList);
        outState.putParcelableArrayList(REVIEWS, reviewArrayList);
        outState.putBoolean(IS_FAVORITE, isFavoriteMovie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TRAILERS_LOADER_ID:
                return new TrailersAsyncTaskLoader(getContext(), String.valueOf(movie.getId()));
            case REVIEWS_LOADER_ID:
                return new ReviewsAsyncTaskLoader(getActivity(), String.valueOf(movie.getId()));
            case IS_FAVORITE_LOADER_ID:
                return new CursorLoader(getContext(), MovieProvider.Movies.withId(movie.getId()), null,
                        null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                trailerArrayList = (ArrayList<Trailer>) data;
                invalidateTrailers();
                break;
            case REVIEWS_LOADER_ID:
                reviewArrayList = (ArrayList<Review>) data;
                invalidateReviews();
                break;
            case IS_FAVORITE_LOADER_ID:
                Cursor cursor = (Cursor) data;
                if (cursor != null) {
                    if (cursor.getCount() == 1)
                        isFavoriteMovie = true;
                    cursor.close();
                    invalidateIsFavorite();
                }
                break;
        }
    }

    private void invalidateIsFavorite() {
        if (isFavoriteMovie) {
            binding.favorite.setImageResource(R.drawable.ic_heart);
        } else
            binding.favorite.setImageResource(R.drawable.ic_heart_outline);
    }

    private void invalidateReviews() {
        if (reviewArrayList == null || reviewArrayList.size() == 0) {
            binding.reviewsHeader.setVisibility(View.GONE);
            binding.reviewsList.setVisibility(View.GONE);
        } else {
            binding.reviewsHeader.setVisibility(View.VISIBLE);
            binding.reviewsList.setVisibility(View.VISIBLE);
            reviewsAdapter.setReviews(reviewArrayList);
        }
    }

    private void invalidateTrailers() {
        if (trailerArrayList == null || trailerArrayList.size() == 0) {
            binding.trailersHeader.setVisibility(View.GONE);
            binding.trailersList.setVisibility(View.GONE);
            binding.reviewsDiv.setVisibility(View.GONE);
            //disable share action
            setHasOptionsMenu(false);
        } else {
            //enable share action
            setHasOptionsMenu(true);
            binding.reviewsDiv.setVisibility(View.VISIBLE);
            binding.trailersHeader.setVisibility(View.VISIBLE);
            binding.trailersList.setVisibility(View.VISIBLE);
            trailersAdapter.setTrailers(trailerArrayList);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onTrailerItemClicked(Trailer trailer) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(makeYoutubeUrlFromKey(trailer.getKey()))));
    }

    private static class TrailersAsyncTaskLoader extends AsyncTaskLoader {
        private String movieId;

        public TrailersAsyncTaskLoader(Context context, String movieId) {
            super(context);
            this.movieId = movieId;
        }

        @Override
        public Object loadInBackground() {
            try {
                Response<TrailersResponse> trailersResponse = MoviesDbApi.getClient()
                        .getMovieVideos(movieId).execute();
                return trailersResponse.body().getTrailers();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class ReviewsAsyncTaskLoader extends AsyncTaskLoader {
        private String movieId;

        public ReviewsAsyncTaskLoader(Context context, String movieId) {
            super(context);
            this.movieId = movieId;
        }

        @Override
        public Object loadInBackground() {
            try {
                Response<ReviewsResponse> reviewsResponse = MoviesDbApi.getClient()
                        .getMovieReviews(movieId).execute();
                return reviewsResponse.body().getReviews();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.item_share);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        actionProvider.setShareIntent(shareTrailer());
    }

    private Intent shareTrailer() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, makeYoutubeUrlFromKey(trailersAdapter.getTrailer(0).getKey()));
        return shareIntent;
    }

    private String makeYoutubeUrlFromKey(String key) {
        return YOUTUBE_BASE_URL.concat(key);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportLoaderManager().destroyLoader(TRAILERS_LOADER_ID);
        getActivity().getSupportLoaderManager().destroyLoader(REVIEWS_LOADER_ID);
        getActivity().getSupportLoaderManager().destroyLoader(IS_FAVORITE_LOADER_ID);
    }
}