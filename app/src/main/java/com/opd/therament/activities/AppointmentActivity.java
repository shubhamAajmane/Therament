package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.datamodels.AppointmentDataModel;
import com.opd.therament.datamodels.DateDataModel;
import com.opd.therament.datamodels.TimeSlotDataModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentActivity extends AppCompatActivity {

    Spinner spTime, spDate;
    ImageView ivBack;
    EditText etTitle, etDescription;
    Button btnSchedule;
    String hospitalId, selectedTime, selectedDate, time, date;
    int totalCount, status;
    ArrayAdapter<TimeSlotDataModel> timeAdapter;
    ArrayAdapter<DateDataModel> dateAdapter;
    FirebaseFirestore firestore;
    TimeSlotDataModel selectedTimeModel;
    DateDataModel selectedDateModel;
    FirebaseAuth mAuth;
    Calendar myCalendar;
    SimpleDateFormat dateFormat, timeFormat;
    TimeSlotDataModel t = new TimeSlotDataModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(view -> {
            onBackPressed();
        });

        spDate = findViewById(R.id.date_spinner);
        spTime = findViewById(R.id.spinner);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnSchedule = findViewById(R.id.btn_schedule);
        btnSchedule.setOnClickListener(view -> {
            if (spDate.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            } else if (spTime.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            } else if (etTitle.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()) {
                Toast.makeText(AppointmentActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                scheduleAppointment();
            }
        });

        Intent intent = getIntent();

        if (intent != null) {
            hospitalId = intent.getStringExtra("hospitalId");
        }

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        getDates();

        myCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        time = timeFormat.format(new Date());
        date = dateFormat.format(new Date());

        spDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDateModel = (DateDataModel) adapterView.getItemAtPosition(i);
                selectedDate = selectedDateModel.getDate();

                if (!selectedDate.equals("Select Date")) {
                    totalCount = Integer.parseInt(selectedDateModel.getTotal());
                    getTimeSlots(selectedDateModel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeModel = (TimeSlotDataModel) adapterView.getItemAtPosition(i);
                selectedTime = selectedTimeModel.getTimeSlot();

                if (!selectedTime.equals("Select Time Slot")) {
                    status = Integer.parseInt(selectedTimeModel.getStatus());

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
    }

    private void getTimeSlots(DateDataModel dateDataModel) {

        ArrayList<TimeSlotDataModel> timeList = new ArrayList<>();
        t.setTimeSlot("Select Time Slot");
        timeList.add(t);

        CollectionReference timeColl = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_timeslots)).document(dateDataModel.getId()).collection(getString(R.string.collection_times));
        timeColl.get().addOnCompleteListener(task -> {

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

   /* @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        selectedDate = dateFormat.format(myCalendar.getTime());
        etDate.setText(selectedDate);
    }*/

    private void getDates() {

        ArrayList<DateDataModel> dateList = new ArrayList<>();

        DateDataModel d = new DateDataModel();
        d.setDate("Select Date");
        dateList.add(d);
        d.setId("0");

        CollectionReference collRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_timeslots));
        collRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot dateDoc : task.getResult()) {

                    if (dateDoc.exists()) {
                        DateDataModel dateDataModel = dateDoc.toObject(DateDataModel.class);
                        dateList.add(dateDataModel);
                    }
                }
                updateDates(dateList);
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDates(ArrayList<DateDataModel> dateList) {

        ArrayList<String> updatedDateList = new ArrayList<>();
        updatedDateList.add("Select Date");

        int post = 7;

        for (int i = 0; i < post; i++) {

            if (i == 0) {
                myCalendar.set(Calendar.DAY_OF_YEAR, myCalendar.get(Calendar.DAY_OF_YEAR));
            } else if (i > 0) {
                myCalendar.set(Calendar.DAY_OF_YEAR, myCalendar.get(Calendar.DAY_OF_YEAR) + 1);
            }
            Date today = myCalendar.getTime();
            String result = dateFormat.format(today);
            updatedDateList.add(result);
        }

        if (!updatedDateList.get(1).equals(dateList.get(1).getDate())) {

            for (int i = 0; i < dateList.size(); i++) {
                DocumentReference dateDoc = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId)
                        .collection(getString(R.string.collection_timeslots)).document(dateList.get(i).getId());

                dateDoc.update("date", updatedDateList.get(i));
                dateList.get(i).setDate(updatedDateList.get(i));
            }
        }
        dateAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, dateList);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDate.setAdapter(dateAdapter);
    }

    private void scheduleAppointment() {

        DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid());

        checkPreviousAppointment(userDoc);
    }

    private void checkPreviousAppointment(DocumentReference userDoc) {

        CollectionReference appointColl = userDoc.collection(getString(R.string.collection_appointments));
        DocumentReference appointmentDoc = userDoc.collection(getString(R.string.collection_appointments)).document();

        appointColl.whereEqualTo("hospitalId", hospitalId).whereEqualTo("selectedDate", selectedDate).whereEqualTo("selectedTime", selectedTime).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                boolean isScheduled = false;

                for (DocumentSnapshot doc : task.getResult()) {

                    if (doc.exists()) {
                        isScheduled = true;
                    }
                }

                if (isScheduled) {
                    Toast.makeText(AppointmentActivity.this, "Appointment already scheduled for this hospital on this date and time", Toast.LENGTH_SHORT).show();
                } else {
                    updateDatabase(appointmentDoc);
                }

            } else {
                Toast.makeText(AppointmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDatabase(DocumentReference appointmentDoc) {

        AppointmentDataModel appointmentDataModel = new AppointmentDataModel();
        appointmentDataModel.setId(appointmentDoc.getId());
        appointmentDataModel.setHospitalId(hospitalId);
        appointmentDataModel.setTime(time);
        appointmentDataModel.setDate(date);
        appointmentDataModel.setSelectedTime(selectedTime);
        appointmentDataModel.setSelectedDate(selectedDate);
        appointmentDataModel.setTitle(etTitle.getText().toString());
        appointmentDataModel.setDescription(etDescription.getText().toString());

        appointmentDoc.set(appointmentDataModel).addOnCompleteListener(taskDoc -> {

            if (taskDoc.isSuccessful()) {
                Toast.makeText(AppointmentActivity.this, "Appointment scheduled successfully", Toast.LENGTH_SHORT).show();
                DocumentReference timeRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalId).collection(getString(R.string.collection_timeslots)).document(selectedDateModel.getId()).collection(getString(R.string.collection_times)).document(selectedTimeModel.getId());

                int increment = ++status;
                timeRef.update("status", String.valueOf(increment));
                finish();
            }
        });
    }
}