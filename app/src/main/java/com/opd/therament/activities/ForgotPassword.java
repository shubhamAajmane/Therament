package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.opd.therament.R;
import com.opd.therament.datamodels.UserDataModel;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    EditText etPhone;
    Button sendOtp;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(ForgotPassword.this, "Verification Unsuccessful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(ForgotPassword.this, "OTP has been sent to your phone no", Toast.LENGTH_SHORT).show();

                Intent verify = new Intent(ForgotPassword.this, VerificationActivity.class);
                verify.putExtra("auth", s);
                verify.putExtra("isPassword", true);
                startActivity(verify);
            }
        };
    }

    void init() {
        etPhone = findViewById(R.id.et_phone);
        sendOtp = findViewById(R.id.btn_send_otp);
        sendOtp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_send_otp: {
                if (etPhone.getText().toString().isEmpty() || etPhone.getText().toString().length() != 10) {
                    etPhone.setError("Invalid phone no");
                } else {
                    checkPhoneNo(etPhone.getText().toString());
                }
            }
            break;
        }
    }

    private void checkPhoneNo(String phone) {
        CollectionReference userCollection = firestore.collection(getString(R.string.collection_users));

        userCollection.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                boolean isRegistered = false;

                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                    UserDataModel userDataModel = documentSnapshot.toObject(UserDataModel.class);

                    if (userDataModel.getPhone() != null) {
                        if (userDataModel.getPhone().equals(phone)) {
                            isRegistered = true;
                        }
                    }
                }

                if(isRegistered) {
                    sendVerificationCode(phone);
                } else {
                    Toast.makeText(ForgotPassword.this,"Phone no not registered",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder().setPhoneNumber("+91" + phone).setActivity(ForgotPassword.this).setCallbacks(mCallbacks).setTimeout(60L, TimeUnit.SECONDS).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}