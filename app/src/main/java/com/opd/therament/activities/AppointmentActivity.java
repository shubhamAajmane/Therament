package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.datamodels.AppointmentDataModel;
import com.opd.therament.datamodels.DateDataModel;
import com.opd.therament.datamodels.HospitalDataModel;
import com.opd.therament.datamodels.TimeSlotDataModel;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.LoadingDialog;
import com.opd.therament.utilities.MailSender;

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
    String selectedTime, selectedDate, time, date;
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
    HospitalDataModel hospitalDetails;

    @Override
    protected void onResume() {
        super.onResume();
        LoadingDialog.showDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        Intent intent = getIntent();

        if (intent != null) {
            String hos = intent.getStringExtra("hospitalDetails");
            hospitalDetails = new Gson().fromJson(hos, HospitalDataModel.class);
        }

        spDate = findViewById(R.id.date_spinner);
        spTime = findViewById(R.id.spinner);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(view -> {
            onBackPressed();
        });

        btnSchedule = findViewById(R.id.btn_schedule);
        btnSchedule.setOnClickListener(view -> {
            if (spDate.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            } else if (spTime.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            } else if (etTitle.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()) {
                Toast.makeText(AppointmentActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                LoadingDialog.showDialog(this);
                scheduleAppointment();
            }
        });

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
                        Toast.makeText(AppointmentActivity.this, "This Time Slot is full", Toast.LENGTH_SHORT).show();
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

        CollectionReference timeColl = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDetails.getId()).collection(getString(R.string.collection_timeslots)).document(dateDataModel.getId()).collection(getString(R.string.collection_times));
        timeColl.orderBy("timeSlot").get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot timeDoc : task.getResult()) {
                    TimeSlotDataModel timeSlotDataModel = timeDoc.toObject(TimeSlotDataModel.class);
                    timeList.add(timeSlotDataModel);
                }
                timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, timeList);
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spTime.setAdapter(timeAdapter);

            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDates() {

        ArrayList<DateDataModel> dateList = new ArrayList<>();

        DateDataModel d = new DateDataModel();
        d.setDate("Select Date");
        dateList.add(d);
        d.setId("0");

        CollectionReference collRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDetails.getId()).collection(getString(R.string.collection_timeslots));
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
                LoadingDialog.dismissDialog();
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

        if (dateList.size() != 1) {

            if (!updatedDateList.get(1).equals(dateList.get(1).getDate())) {

                for (int i = 0; i < dateList.size(); i++) {
                    DocumentReference dateDoc = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDetails.getId())
                            .collection(getString(R.string.collection_timeslots)).document(dateList.get(i).getId());

                    dateDoc.update("date", updatedDateList.get(i));
                    dateList.get(i).setDate(updatedDateList.get(i));
                }
            }
        }
        dateAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, dateList);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDate.setAdapter(dateAdapter);
        LoadingDialog.dismissDialog();
    }

    private void scheduleAppointment() {

        DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid());

        checkPreviousAppointment(userDoc);
    }

    private void checkPreviousAppointment(DocumentReference userDoc) {

        CollectionReference appointColl = userDoc.collection(getString(R.string.collection_appointments));
        DocumentReference appointmentDoc = userDoc.collection(getString(R.string.collection_appointments)).document();

        appointColl.whereEqualTo("hospitalId", hospitalDetails.getId()).whereEqualTo("selectedDate", selectedDate).whereEqualTo("selectedTime", selectedTime).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                boolean isScheduled = false;

                for (DocumentSnapshot doc : task.getResult()) {

                    if (doc.exists()) {
                        isScheduled = true;
                    }
                }

                if (isScheduled) {
                    LoadingDialog.dismissDialog();
                    Toast.makeText(AppointmentActivity.this, "Appointment already scheduled for this hospital on this date and time", Toast.LENGTH_SHORT).show();
                } else {
                    getUserDetails(appointmentDoc, userDoc);
                }

            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(AppointmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserDetails(DocumentReference appointmentDoc, DocumentReference userDoc) {

        userDoc.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                DocumentSnapshot doc = task.getResult();

                if (doc.exists()) {
                    UserDataModel userDataModel = doc.toObject(UserDataModel.class);
                    sendEmail(appointmentDoc, userDataModel);
                } else {
                    LoadingDialog.dismissDialog();
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmail(DocumentReference appDoc, UserDataModel userDataModel) {

        String userName = userDataModel.getName();
        String contact;

        if (userDataModel.getPhone() != null) {
            contact = userDataModel.getPhone();
        } else {
            contact = userDataModel.getEmail();
        }

        String msgBody = "Title: " + etTitle.getText().toString() + "\nDescription: " + etDescription.getText().toString() + "\nUser Name: " + userName + "\nContact Details: " + contact + "\nSelected Date: " + selectedDate + "\nSelected Time Slot: " + selectedTime + "\nScheduled On: " + date + " " + time;

        Thread sender = new Thread(() -> {
            boolean mailSent;
            try {
                MailSender sender1 = new MailSender(getString(R.string.app_email), getString(R.string.app_email_pass));
                sender1.sendMail(msgBody,
                        getString(R.string.app_email),
                        "aajmane09@gmail.com");
                mailSent = true;
            } catch (Exception e) {
                mailSent = false;
                Log.e("mylog", "Error: " + e.getMessage());
            }
            if (mailSent) {
                updateDatabase(appDoc);
            }
        });
        sender.start();
    }

    private void updateDatabase(DocumentReference appointmentDoc) {

        AppointmentDataModel appointmentDataModel = new AppointmentDataModel();
        appointmentDataModel.setId(appointmentDoc.getId());
        appointmentDataModel.setHospitalId(hospitalDetails.getId());
        appointmentDataModel.setHospitalImage(hospitalDetails.getImageUrl());
        appointmentDataModel.setHospitalName(hospitalDetails.getName());
        appointmentDataModel.setHospitalAddress(hospitalDetails.getAddress());
        appointmentDataModel.setTime(time);
        appointmentDataModel.setDate(date);
        appointmentDataModel.setSelectedTime(selectedTime);
        appointmentDataModel.setSelectedDate(selectedDate);
        appointmentDataModel.setTitle(etTitle.getText().toString());
        appointmentDataModel.setDescription(etDescription.getText().toString());
        appointmentDoc.set(appointmentDataModel).addOnCompleteListener(task -> {
            DocumentReference timeRef = firestore.collection(getString(R.string.collection_hospitals)).document(hospitalDetails.getId()).collection(getString(R.string.collection_timeslots)).document(selectedDateModel.getId()).collection(getString(R.string.collection_times)).document(selectedTimeModel.getId());
            int increment = ++status;
            timeRef.update("status", String.valueOf(increment));
            LoadingDialog.dismissDialog();
            Toast.makeText(AppointmentActivity.this, "Appointment scheduled successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}