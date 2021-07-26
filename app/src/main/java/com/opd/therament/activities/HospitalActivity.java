package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.adapters.DoctorAdapter;
import com.opd.therament.adapters.ReviewAdapter;
import com.opd.therament.datamodels.DoctorDataModel;
import com.opd.therament.datamodels.HospitalDataModel;
import com.opd.therament.datamodels.ReviewDataModel;
import com.opd.therament.utilities.LoadingDialog;

import java.util.ArrayList;

public class HospitalActivity extends AppCompatActivity implements View.OnClickListener {

    HospitalDataModel hospitalDatamodel;
    TextView tvName, tvAddress, tvDescription, tvRatings, tvViewall, tvTime;
    ImageView ivLogo, ivBack;
    RatingBar ratingBar;
    RecyclerView rvDoctors, rvReviews;
    FirebaseFirestore firestore;
    Button btnSchedule;
    ArrayList<DoctorDataModel> doctorsList = new ArrayList<>();
    ArrayList<ReviewDataModel> reviewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);
        init();
        firestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();

        if (intent != null) {
            String hospitalDetails = getIntent().getStringExtra("hospitalDetails");
            hospitalDatamodel = new Gson().fromJson(hospitalDetails, HospitalDataModel.class);
        }
        setData();
    }

    private void init() {
        tvName = findViewById(R.id.tv_hospital_name);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        ivLogo = findViewById(R.id.iv_logo);
        ratingBar = findViewById(R.id.rating_bar);
        ivBack = findViewById(R.id.iv_back);
        rvDoctors = findViewById(R.id.rv_doctors);
        rvReviews = findViewById(R.id.rv_reviews);
        tvRatings = findViewById(R.id.tv_ratings);
        tvTime = findViewById(R.id.tv_time);
        tvViewall = findViewById(R.id.tv_view_all);
        tvViewall.setOnClickListener(this);
        btnSchedule = findViewById(R.id.btn_appointment);
        btnSchedule.setOnClickListener(this);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        ivBack.setOnClickListener(this);
    }

    private void setData() {
        tvName.setText(hospitalDatamodel.getName());
        tvAddress.setText(hospitalDatamodel.getAddress());
        tvDescription.setText(hospitalDatamodel.getDescription());
        tvRatings.setText(hospitalDatamodel.getRating());
        tvTime.setText(String.format("%s %s", getString(R.string.time_label), hospitalDatamodel.getTime()));
        Glide.with(this).load(hospitalDatamodel.getImageUrl()).into(ivLogo);
        ratingBar.setRating(Float.parseFloat(hospitalDatamodel.getRating()));

        LoadingDialog.showDialog(this);
        getDoctors();
        getReviews();
    }

    private void getDoctors() {

        CollectionReference doctorsColl = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDatamodel.getId()).collection(getString(R.string.collection_doctors));

        doctorsColl.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    DoctorDataModel doctorDatamodel = snapshot.toObject(DoctorDataModel.class);
                    doctorsList.add(doctorDatamodel);
                }
                rvDoctors.setAdapter(new DoctorAdapter(HospitalActivity.this, doctorsList));
                LoadingDialog.dismissDialog();
            } else {
                Toast.makeText(HospitalActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getReviews() {
        CollectionReference doctorsColl = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDatamodel.getId()).collection(getString(R.string.collection_reviews));

        doctorsColl.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    ReviewDataModel reviewDataModel = doc.toObject(ReviewDataModel.class);
                    Log.d("Review", new Gson().toJson(reviewDataModel));
                    reviewsList.add(reviewDataModel);
                }
                rvReviews.setAdapter(new ReviewAdapter(this, reviewsList, false));
                LoadingDialog.dismissDialog();
            } else {
                Toast.makeText(HospitalActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.iv_back: {
                onBackPressed();
            }
            break;

            case R.id.btn_appointment: {
                Intent intent = new Intent(this, AppointmentActivity.class);
                String hospitalDetails = new Gson().toJson(hospitalDatamodel);
                intent.putExtra("hospitalDetails", hospitalDetails);
                startActivity(intent);
            }
            break;

            case R.id.tv_view_all: {
                Intent intent = new Intent(this, ReviewsActivity.class);
                intent.putExtra("hospitalId", hospitalDatamodel.getId());
                startActivity(intent);
            }
            break;
        }
    }
}