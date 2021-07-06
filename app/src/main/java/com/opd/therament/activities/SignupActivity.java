package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
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

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etName, etPhone, etPass, etConfirmPass;
    Button btnSignup;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("FIREBASE",e.getLocalizedMessage());
                Toast.makeText(SignupActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(SignupActivity.this, "OTP has been sent to your phone no", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent otpIntent = new Intent(SignupActivity.this, VerificationActivity.class);
                        otpIntent.putExtra("auth", s);
                        otpIntent.putExtra("name", etName.getText().toString());
                        otpIntent.putExtra("phone", etPhone.getText().toString());
                        otpIntent.putExtra("password", etPass.getText().toString());
                        startActivity(otpIntent);
                    }
                }, 2000);
            }
        };
    }

    private void signInWithPhone(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendToDashboard();
                }
            }
        });
    }

    private void sendToDashboard() {
        Intent dashIntent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(dashIntent);
        finish();
    }

    public void init() {
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etPass = findViewById(R.id.et_pass);
        etConfirmPass = findViewById(R.id.et_confirm_pass);
        btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_signup: {
                if (etName.getText().toString().isEmpty()) {
                    etName.setError("Enter name");
                } else if (etPhone.getText().toString().isEmpty()) {
                    etPhone.setError("Enter phone no");
                } else if (etPass.getText().toString().isEmpty()) {
                    etPass.setError("Enter password");
                } else if (etConfirmPass.getText().toString().isEmpty()) {
                    etConfirmPass.setError("Enter confirm password");
                } else if (etPhone.getText().length() != 10) {
                    etPhone.setError("Enter valid phone no");
                } else if (!etPass.getText().toString().equals(etConfirmPass.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "Password and Confirm Password must be same", Toast.LENGTH_SHORT).show();
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

                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                    UserDataModel userDataModel = documentSnapshot.toObject(UserDataModel.class);

                    if (userDataModel.getPhone() != null) {
                        if (!userDataModel.getPhone().equals(phone)) {
                            sendVerificationCode(phone);

                        } else {
                            Toast.makeText(SignupActivity.this, "Phone no already registered", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        sendVerificationCode(phone);
                    }
                }
            }
        });
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder().setPhoneNumber("+91" + phone).setActivity(SignupActivity.this).setCallbacks(mCallbacks).setTimeout(60L, TimeUnit.SECONDS).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}