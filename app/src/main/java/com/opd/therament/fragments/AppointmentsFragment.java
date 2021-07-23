package com.opd.therament.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.opd.therament.R;
import com.opd.therament.adapters.AppointmentAdapter;
import com.opd.therament.datamodels.AppointmentDataModel;

import java.util.ArrayList;

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.onCancelListener {

    ImageView ivBack;
    RecyclerView rvAppointments;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ArrayList<AppointmentDataModel> appointmentList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_appointments, container, false);
        ivBack = root.findViewById(R.id.iv_back);
        rvAppointments = root.findViewById(R.id.rv_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        getAppointments();
        return root;
    }

    private void getAppointments() {
        CollectionReference appColl = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid()).collection(getString(R.string.collection_appointments));

        appColl.orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : task.getResult()) {

                    AppointmentDataModel dataModel = doc.toObject(AppointmentDataModel.class);
                    appointmentList.add(dataModel);
                }
                rvAppointments.setAdapter(new AppointmentAdapter(getContext(), appointmentList, this));

            } else {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCancelled(AppointmentDataModel dataModel, int pos) {

        new AlertDialog.Builder(getContext()).setTitle("Cancel Appointment").setMessage("Are you sure you want to cancel your appointment?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).create().show();
    }
}