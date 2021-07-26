package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.adapters.ReviewAdapter;
import com.opd.therament.datamodels.ReviewDataModel;
import com.opd.therament.utilities.LoadingDialog;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    RecyclerView rvReviews;
    ImageView ivBack;
    FirebaseFirestore firestore;
    String hospitalId;

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        if(intent!=null) {
            hospitalId = intent.getStringExtra("hospitalId");
        }
        LoadingDialog.showDialog(this);
        getReviews();
    }

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
        firestore = FirebaseFirestore.getInstance();
    }

    private void getReviews() {

        ArrayList<ReviewDataModel> reviewsList = new ArrayList<>();

        CollectionReference doctorsColl = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_reviews));

        doctorsColl.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    ReviewDataModel reviewDataModel = doc.toObject(ReviewDataModel.class);
                    Log.d("Review",new Gson().toJson(reviewDataModel));
                    reviewsList.add(reviewDataModel);
                }
                rvReviews.setAdapter(new ReviewAdapter(this,reviewsList,false));
                LoadingDialog.dismissDialog();
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}