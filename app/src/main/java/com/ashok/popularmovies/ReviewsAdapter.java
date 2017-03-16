package com.ashok.popularmovies;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ashok.popularmovies.databinding.ItemReviewListBinding;
import com.ashok.popularmovies.model.Review;

import java.util.List;

/**
 * Created by ashok on 27/2/17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {
    private Context context;
    private List<Review> reviews;

    public ReviewsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemReviewListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_review_list, parent, false);
        return new ReviewsAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapterViewHolder holder, int position) {
        holder.binding.review.setText(reviews.get(position).getContent());
        holder.binding.author.setText(reviews.get(position).getAuthor());
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (reviews == null)
            return 0;
        return reviews.size();
    }

    public static class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemReviewListBinding binding;

        public ReviewsAdapterViewHolder(ItemReviewListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
