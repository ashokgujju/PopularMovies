package com.ashok.popularmovies;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashok.popularmovies.databinding.ItemTrailerListBinding;
import com.ashok.popularmovies.model.Trailer;

import java.util.List;

/**
 * Created by ashok on 27/2/17.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {
    private Context context;
    private List<Trailer> trailers;
    private OnTrailerItemClickListener trailerItemListener;

    public interface OnTrailerItemClickListener {
        void onTrailerItemClicked(Trailer trailer);
    }

    public TrailersAdapter(Context context) {
        this.context = context;
    }

    public void setTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    public void setOnTrailerItemListener(OnTrailerItemClickListener trailerItemListener) {
        this.trailerItemListener = trailerItemListener;
    }

    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemTrailerListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_trailer_list, parent, false);
        return new TrailersAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TrailersAdapterViewHolder holder, int position) {
        holder.binding.title.setText(trailers.get(position).getName());
    }

    public Trailer getTrailer(int position) {
        if (trailers == null)
            return null;
        return trailers.get(position);
    }

    @Override
    public int getItemCount() {
        if (trailers == null)
            return 0;
        return trailers.size();
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemTrailerListBinding binding;

        public TrailersAdapterViewHolder(ItemTrailerListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trailerItemListener.onTrailerItemClicked(trailers.get(getAdapterPosition()));
                }
            });
        }
    }
}
