package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.adapters.AppointmentAdapter;
import com.opd.therament.datamodels.AppointmentDataModel;
import com.opd.therament.datamodels.HospitalDataModel;

import java.util.ArrayList;
import java.util.Objects;

public class PreviousAppointmentActivity extends AppCompatActivity implements AppointmentAdapter.ItemViewClick {

    ImageView ivBack;
    RecyclerView rvAppointments;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    AppointmentAdapter appointmentAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        getAppointments();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_appointment);
        ivBack = findViewById(R.id.iv_back);
        rvAppointments = findViewById(R.id.rv_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void getAppointments() {
        ArrayList<AppointmentDataModel> appointmentList = new ArrayList<>();

        CollectionReference appColl = firestore.collection(getString(R.string.collection_users)).document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection(getString(R.string.collection_history));

        appColl.orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : task.getResult()) {
                    AppointmentDataModel dataModel = doc.toObject(AppointmentDataModel.class);
                    appointmentList.add(dataModel);
                }
                appointmentAdapter = new AppointmentAdapter(this, appointmentList, this, "History");
                rvAppointments.setAdapter(appointmentAdapter);

            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClicked(AppointmentDataModel dataModel) {
        DocumentReference hospitalDoc = firestore.collection(getString(R.string.collection_hospitals)).document(dataModel.getHospitalId());

        hospitalDoc.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();

                if (doc.exists()) {
                    HospitalDataModel hospitalDataModel = doc.toObject(HospitalDataModel.class);
                    String hospitalDetails = new Gson().toJson(hospitalDataModel);
                    Intent intent = new Intent(this, HospitalActivity.class);
                    intent.putExtra("hospitalDetails", hospitalDetails);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Hospital Not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}