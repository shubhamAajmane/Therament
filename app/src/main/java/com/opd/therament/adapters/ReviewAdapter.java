package com.opd.therament.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.opd.therament.R;
import com.opd.therament.datamodels.ReviewDataModel;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    Context context;
    ArrayList<ReviewDataModel> reviewList;
    boolean viewAll;

    public ReviewAdapter(Context context, ArrayList<ReviewDataModel> reviewList, boolean viewAll) {
        this.context = context;
        this.reviewList = reviewList;
        this.viewAll = viewAll;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
        return new ReviewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
        ReviewDataModel dataModel = reviewList.get(position);
        holder.tvName.setText(dataModel.getName());
        holder.tvReview.setText(dataModel.getReview());
        holder.ratingBar.setRating(Float.parseFloat(dataModel.getRating()));
    }

    @Override
    public int getItemCount() {

        if (viewAll) {
            return reviewList.size();
        } else return Math.min(reviewList.size(), 3);
    }

    static class ReviewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        RatingBar ratingBar;
        TextView tvName, tvReview;

        public ReviewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.iv_image);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvReview = itemView.findViewById(R.id.tv_review);
        }
    }
}
