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

       /* DoctorDataModel d1 = new DoctorDataModel();
        d1.setName("Dr. J M Johnson");
        d1.setDegree("M.B.B.S");

        DoctorDataModel d2 = new DoctorDataModel();
        d2.setName("Dr. K D Thomson");
        d2.setDegree("B.A.M.S");

        DoctorDataModel d3 = new DoctorDataModel();
        d3.setName("Dr. R D Potter");
        d3.setDegree("B.D.S");

        doctorsList.add(d1);
        doctorsList.add(d2);
        doctorsList.add(d3);
        rvDoctors.setAdapter(new DoctorAdapter(this, doctorsList));

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
        rvReviews.setAdapter(new ReviewAdapter(this, reviewsList, false));*/

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
                    Log.d("Review",new Gson().toJson(reviewDataModel));
                    reviewsList.add(reviewDataModel);
                }
                rvReviews.setAdapter(new ReviewAdapter(this,reviewsList,false));
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
                intent.putExtra("hospitalId", hospitalDatamodel.getId());
                startActivity(intent);
            }
            break;

            case R.id.tv_view_all: {
                startActivity(new Intent(this, ReviewsActivity.class));
            }
            break;
        }
    }
}