package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.datamodels.TimeSlotDataModel;

import java.util.ArrayList;

public class AppointmentActivity extends AppCompatActivity {

    Spinner spTime;
    EditText etTitle, etDescription;
    Button btnSchedule;
    String hospitalId, selectedTime;
    int totalCount, status;
    ArrayList<TimeSlotDataModel> timeList = new ArrayList<>();
    ArrayAdapter<TimeSlotDataModel> timeAdapter;
    FirebaseFirestore firestore;
    TimeSlotDataModel timeSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        spTime = findViewById(R.id.spinner);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnSchedule = findViewById(R.id.btn_schedule);
        TimeSlotDataModel t = new TimeSlotDataModel();
        t.setTime("Select Time Slot");
        timeList.add(t);

        Intent intent = getIntent();

        if (intent != null) {
            hospitalId = intent.getStringExtra("hospitalId");
            totalCount = Integer.parseInt(intent.getStringExtra("totalCount"));
        }

        firestore = FirebaseFirestore.getInstance();
        getTimeSlots();

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeSlot = (TimeSlotDataModel) adapterView.getItemAtPosition(i);
                selectedTime = timeSlot.getTime();

                if (!selectedTime.equals("Select Time Slot")) {
                    status = Integer.parseInt(timeSlot.getStatus());

                    if (status == totalCount) {
                        Toast.makeText(AppointmentActivity.this, "This slot is full", Toast.LENGTH_SHORT).show();
                        int pos = timeAdapter.getPosition(t);
                        spTime.setSelection(pos);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnSchedule.setOnClickListener(view -> {

            if (spTime.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            } else if (etTitle.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()) {
                Toast.makeText(AppointmentActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                scheduleAppointment();
            }
        });
    }

    private void getTimeSlots() {
        CollectionReference collRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_timeslots));
        collRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot timeDoc : task.getResult()) {
                    TimeSlotDataModel timeSlotDataModel = timeDoc.toObject(TimeSlotDataModel.class);
                    timeList.add(timeSlotDataModel);
                }
                timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, timeList);
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spTime.setAdapter(timeAdapter);
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleAppointment() {
        if (status <= totalCount) {
            DocumentReference timeRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_timeslots)).document(timeSlot.getId());
            int increment = status++;
            timeRef.update("status", String.valueOf(increment));
        }
    }
}