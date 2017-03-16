package com.ashok.popularmovies;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashok.popularmovies.databinding.ItemMovieListBinding;
import com.ashok.popularmovies.model.Movie;
import com.ashok.popularmovies.network.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ashok on 24/2/17.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {
    private Context context;
    private List<Movie> movies;
    private OnMovieItemClickListener movieItemClickListener;

    public MoviesAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }


    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemMovieListBinding itemMoviesBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.item_movie_list, parent, false);
        return new MoviesAdapterViewHolder(itemMoviesBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, int position) {
        Uri posterUri = NetworkUtils.buildImgUri(movies.get(position).getPosterPath());
        Picasso.with(context).load(posterUri).placeholder(R.drawable.ic_movie_poster).fit()
                .into(holder.itemMoviesBinding.moviePoster);

    }

    @Override
    public int getItemCount() {
        if (movies == null)
            return 0;
        return movies.size();
    }

    public interface OnMovieItemClickListener {
        void onMovieItemClicked(Movie movie);
    }

    public void setOnMovieItemClickListener(OnMovieItemClickListener itemClickListener) {
        this.movieItemClickListener = itemClickListener;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemMovieListBinding itemMoviesBinding;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);

            itemMoviesBinding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            movieItemClickListener.onMovieItemClicked(movies.get(getAdapterPosition()));
        }
    }
}
