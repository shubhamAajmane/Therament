package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.datamodels.ReviewDataModel;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.LoadingDialog;

public class WriteReviewActivity extends AppCompatActivity {

    RatingBar ratingBar;
    EditText etReview;
    Button btnSubmit;
    String hospitalId, appointmentId;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    boolean isHistory;
    ReviewDataModel reviewDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        ratingBar = findViewById(R.id.rating_bar);
        etReview = findViewById(R.id.et_review);
        btnSubmit = findViewById(R.id.btn_submit);
        reviewDataModel = new ReviewDataModel();

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        if (intent != null) {
            hospitalId = intent.getStringExtra("hospitalId");
            appointmentId = intent.getStringExtra("appointmentId");
            float rating = intent.getFloatExtra("rating", 0);
            reviewDataModel.setRating(String.valueOf(rating));
            ratingBar.setRating(rating);
            isHistory = intent.getBooleanExtra("isHistory", false);
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
            reviewDataModel.setRating(String.valueOf(v));
        });

        btnSubmit.setOnClickListener(view -> {

            if (etReview.getText().toString().isEmpty()) {
                etReview.setError("Please Enter Review");
            } else if (ratingBar.getRating() == 0) {
                Toast.makeText(this, "Please Rate", Toast.LENGTH_SHORT).show();
            } else {
                LoadingDialog.showDialog(this);
                DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid());
                userDoc.get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        DocumentSnapshot user = task.getResult();

                        if (user.exists()) {
                            UserDataModel userDataModel = user.toObject(UserDataModel.class);
                            reviewDataModel.setId(userDataModel.getUserId());
                            reviewDataModel.setName(userDataModel.getName());
                            reviewDataModel.setReview(etReview.getText().toString());
                            rateHospital(reviewDataModel);
                        } else {
                            LoadingDialog.dismissDialog();
                            Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        LoadingDialog.dismissDialog();
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void rateHospital(ReviewDataModel reviewDataModel) {

        DocumentReference hosDoc = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_reviews)).document(reviewDataModel.getId());

        if (isHistory) {
            hosDoc.update("rating", reviewDataModel.getRating(), "review", reviewDataModel.getReview()).addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                    LoadingDialog.dismissDialog();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            hosDoc.set(reviewDataModel).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    DocumentReference appointment = FirebaseFirestore.getInstance().collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(getString(R.string.collection_appointments)).document(appointmentId);
                    appointment.delete();
                } else {
                    LoadingDialog.dismissDialog();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
        LoadingDialog.dismissDialog();
        Toast.makeText(this, "Rated Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}