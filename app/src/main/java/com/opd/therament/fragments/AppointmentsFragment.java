package com.opd.therament.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.activities.HospitalActivity;
import com.opd.therament.activities.PreviousAppointmentActivity;
import com.opd.therament.adapters.AppointmentAdapter;
import com.opd.therament.datamodels.AppointmentDataModel;
import com.opd.therament.datamodels.DateDataModel;
import com.opd.therament.datamodels.HospitalDataModel;
import com.opd.therament.datamodels.TimeSlotDataModel;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.LoadingDialog;
import com.opd.therament.utilities.MailSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.onCancelListener, AppointmentAdapter.ItemViewClick {

    ImageView ivBack;
    RecyclerView rvAppointments;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ArrayList<AppointmentDataModel> appointmentList;
    CollectionReference appColl;
    AppointmentAdapter appointmentAdapter;
    SimpleDateFormat dateFormat;
    String date;
    TextView tvPreviousAppointments, tvNoAppointments;
    LottieAnimationView emptyAnimation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_appointments, container, false);
        ivBack = root.findViewById(R.id.iv_back);
        rvAppointments = root.findViewById(R.id.rv_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        tvNoAppointments = root.findViewById(R.id.tv_no_appointments);
        emptyAnimation = root.findViewById(R.id.empty_animation);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        tvPreviousAppointments = root.findViewById(R.id.tv_previous_appointments);
        LoadingDialog.showDialog(getActivity());
        getAppointments();
        tvPreviousAppointments.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), PreviousAppointmentActivity.class));
        });

        dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
        date = dateFormat.format(new Date());

        return root;
    }

    private void automaticDone() throws ParseException {

        Date currentDate = dateFormat.parse(date);

        for (int i = 0; i < appointmentList.size(); i++) {

            String selectedTime = appointmentList.get(i).getSelectedTime().substring(11);

            Date appDate = dateFormat.parse(appointmentList.get(i).getSelectedDate() + " " + selectedTime);

            assert currentDate != null;

            if (currentDate.compareTo(appDate) >= 0) {
                appointmentList.get(i).setBooked(true);
                updateStatus(appointmentList.get(i));
            }
        }
    }

    private void getAppointments() {
        appointmentList = new ArrayList<>();

        appColl = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid()).collection(getString(R.string.collection_appointments));

        appColl.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : task.getResult()) {
                    AppointmentDataModel dataModel = doc.toObject(AppointmentDataModel.class);
                    appointmentList.add(dataModel);
                }
                if (appointmentList.isEmpty()) {
                    emptyAnimation.setVisibility(View.VISIBLE);
                    tvNoAppointments.setVisibility(View.VISIBLE);
                    rvAppointments.setVisibility(View.INVISIBLE);
                } else {
                    emptyAnimation.setVisibility(View.INVISIBLE);
                    tvNoAppointments.setVisibility(View.INVISIBLE);
                    rvAppointments.setVisibility(View.VISIBLE);
                    try {
                        sortList(appointmentList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        automaticDone();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                LoadingDialog.dismissDialog();
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortList(ArrayList<AppointmentDataModel> appointmentList) throws ParseException {
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Date temp;

        for (int i = 0; i < appointmentList.size(); i++) {

            for (int j = i + 1; j < appointmentList.size(); j++) {
                Date first = formatDate.parse(appointmentList.get(i).getSelectedDate());
                Date second = formatDate.parse(appointmentList.get(j).getSelectedDate());

                assert first != null;
                if (first.compareTo(second) > 0) {
                    temp = first;
                    appointmentList.get(i).setSelectedDate(appointmentList.get(j).getSelectedDate());
                    appointmentList.get(j).setSelectedDate(formatDate.format(temp));
                }
            }
        }
        appointmentAdapter = new AppointmentAdapter(getContext(), appointmentList, this, this, "Other");
        rvAppointments.setAdapter(appointmentAdapter);
    }

    @Override
    public void onCancelled(AppointmentDataModel dataModel) {

        new AlertDialog.Builder(getContext()).setTitle("Cancel Appointment").setMessage("Are you sure you want to cancel your appointment?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DocumentReference appDoc = appColl.document(dataModel.getId());
                DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid());

                userDoc.get().addOnCompleteListener(task1 -> {

                    if (task1.isSuccessful()) {
                        UserDataModel userDataModel = Objects.requireNonNull(task1.getResult()).toObject(UserDataModel.class);
                        assert userDataModel != null;
                        sendEmail(dataModel, userDataModel);

                        appDoc.delete().addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                LoadingDialog.showDialog(getContext());
                                dataModel.setBooked(false);
                                updateStatus(dataModel);
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).setNegativeButton("No", (dialogInterface, i) -> {
        }).create().show();
    }

    private void updateStatus(AppointmentDataModel dataModel) {

        CollectionReference dateColl = firestore.collection(getString(R.string.collection_hospitals)).document(dataModel.getHospitalId()).collection(getString(R.string.collection_timeslots));

        dateColl.whereEqualTo("date", dataModel.getSelectedDate()).get().addOnCompleteListener(task -> {
            CollectionReference timeColl = null;
            DateDataModel date = null;

            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {

                    if (doc.exists()) {
                        date = doc.toObject(DateDataModel.class);
                        timeColl = dateColl.document(date.getId()).collection(getString(R.string.collection_times));
                    }
                }

                if (timeColl != null) {

                    DateDataModel finalDate = date;
                    timeColl.whereEqualTo("timeSlot", dataModel.getSelectedTime()).get().addOnCompleteListener(task1 -> {
                        TimeSlotDataModel time = null;

                        if (task1.isSuccessful()) {
                            for (DocumentSnapshot doc : task1.getResult()) {
                                if (doc.exists()) {
                                    time = doc.toObject(TimeSlotDataModel.class);
                                }
                            }

                            int status = Integer.parseInt(time.getStatus());
                            if (status != 0) {

                                int decrement = --status;
                                DocumentReference timeRef = firestore.collection(getString(R.string.collection_hospitals)).document(dataModel.getHospitalId()).collection(getString(R.string.collection_timeslots)).document(finalDate.getId()).collection(getString(R.string.collection_times)).document(time.getId());
                                timeRef.update("status", String.valueOf(decrement)).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        updateHistory(dataModel);
                                    }
                                });
                            }
                        } else {
                            LoadingDialog.dismissDialog();
                            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHistory(AppointmentDataModel dataModel) {

        if (!dataModel.getBooked()) {
            DocumentReference historyDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid()).collection(getString(R.string.collection_history)).document(dataModel.getId());
            historyDoc.set(dataModel).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    appointmentList.remove(dataModel);
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
        getAppointments();
    }

    private void sendEmail(AppointmentDataModel appDatamodel, UserDataModel userDataModel) {
        LoadingDialog.showDialog(getContext());
        String userName = userDataModel.getName();
        String contact;

        if (userDataModel.getPhone() != null) {
            contact = userDataModel.getPhone();
        } else {
            contact = userDataModel.getEmail();
        }
        DocumentReference hosRef = firestore.collection(getString(R.string.collection_hospitals)).document(appDatamodel.getHospitalId());
        hosRef.get().addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                HospitalDataModel hospitalDataModel = Objects.requireNonNull(task1.getResult()).toObject(HospitalDataModel.class);
                String msgBody = "Title: Cancel Appointment " + "\nUser Name: " + userName + "\nContact Details: " + contact + "\nSelected Date: " + appDatamodel.getSelectedDate() + "\nSelected Time Slot: " + appDatamodel.getSelectedTime() + "\nScheduled On: " + appDatamodel.getDate() + appDatamodel.getTime();

                Thread sender = new Thread(() -> {
                    try {
                        MailSender sender1 = new MailSender(getString(R.string.app_email), getString(R.string.app_email_pass));
                        sender1.sendMail(msgBody,
                                getString(R.string.app_email),
                                hospitalDataModel.getEmail());
                    } catch (Exception e) {
                        Log.e("mylog", "Error: " + e.getMessage());
                    }
                });
                sender.start();
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Appointment Cancelled Successfully", Toast.LENGTH_SHORT).show();

            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
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
                    Intent intent = new Intent(getActivity(), HospitalActivity.class);
                    intent.putExtra("hospitalDetails", hospitalDetails);
                    startActivity(intent);
                } else {
                    LoadingDialog.dismissDialog();
                    Toast.makeText(getContext(), "Hospital Not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}