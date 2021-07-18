package com.opd.therament.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.opd.therament.R;
import com.opd.therament.adapters.ReviewAdapter;
import com.opd.therament.datamodels.ReviewDataModel;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    RecyclerView rvReviews;
    ArrayList<ReviewDataModel> reviewsList = new ArrayList<>();
    ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(view -> {
            onBackPressed();
        });

        ReviewDataModel r1 = new ReviewDataModel();
        r1.setName("Shubham Aajmane");
        r1.setRating("4");
        r1.setReview("Qualified doctors");

        ReviewDataModel r2 = new ReviewDataModel();
        r2.setName("Abhijeet Neje");
        r2.setRating("5");
        r2.setReview("Staff was very good");

        ReviewDataModel r3 = new ReviewDataModel();
        r3.setName("Bahar Khedkar");
        r3.setRating("3");
        r3.setReview("Best service by this hospital");

        ReviewDataModel r4 = new ReviewDataModel();
        r4.setName("Shreya Koulage");
        r4.setRating("4");
        r4.setReview("Hospital environment was very clean and tidy");

        reviewsList.add(r1);
        reviewsList.add(r2);
        reviewsList.add(r3);
        reviewsList.add(r4);

        rvReviews.setAdapter(new ReviewAdapter(this, reviewsList, true));
    }
}