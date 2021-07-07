package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.OtpWatcher;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String verificationId, name, phone, password;
    boolean isLogin;
    Button btnVerify;
    TextView tvResendCode;
    boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        init();

        EditText[] otpText = {otp1, otp2, otp3, otp4, otp5, otp6};
        otp1.addTextChangedListener(new OtpWatcher(otp1, otpText));
        otp2.addTextChangedListener(new OtpWatcher(otp2, otpText));
        otp3.addTextChangedListener(new OtpWatcher(otp3, otpText));
        otp4.addTextChangedListener(new OtpWatcher(otp4, otpText));
        otp5.addTextChangedListener(new OtpWatcher(otp5, otpText));
        otp6.addTextChangedListener(new OtpWatcher(otp6, otpText));

        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        if (intent != null) {

            isLogin = intent.getBooleanExtra("isLogin", false);

            verificationId = intent.getStringExtra("auth");
        }

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(VerificationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                resendingToken = forceResendingToken;
            }
        };

        btnVerify.setOnClickListener(view -> {
            String verificationCode = otp1.getText().toString() + otp2.getText().toString() + otp3.getText().toString() + otp4.getText().toString() + otp5.getText().toString() + otp6.getText().toString();

            if (!verificationCode.isEmpty()) {
                verifyCode(verificationCode);
            } else {
                Toast.makeText(VerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });

        tvResendCode.setOnClickListener(view -> {
            resendVerification(phone, resendingToken);
        });
    }

    void init() {
        otp1 = findViewById(R.id.otp_edit_box1);
        otp2 = findViewById(R.id.otp_edit_box2);
        otp3 = findViewById(R.id.otp_edit_box3);
        otp4 = findViewById(R.id.otp_edit_box4);
        otp5 = findViewById(R.id.otp_edit_box5);
        otp6 = findViewById(R.id.otp_edit_box6);
        btnVerify = findViewById(R.id.btn_verify);
        tvResendCode = findViewById(R.id.tv_resend);
    }

    public void resendVerification(String phone, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder().setPhoneNumber(phone).setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallbacks).setForceResendingToken(token).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhone(credential);
    }

    private void signInWithPhone(PhoneAuthCredential phoneAuthCredential) {

        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                isLogged = true;
                if (!isLogin) {
                    addToDatabase(name, phone);
                } else {
                    Intent newPassword = new Intent(VerificationActivity.this, MainActivity.class);
                    startActivity(newPassword);
                }

            } else {
                Toast.makeText(VerificationActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToDatabase(String name, String phone) {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(userId);

        UserDataModel newUser = new UserDataModel();
        newUser.setName(name);
        newUser.setPhone(phone);
        newUser.setUserId(userId);
        userDoc.set(newUser);

        startActivity(new Intent(VerificationActivity.this, MainActivity.class));
        Toast.makeText(VerificationActivity.this, "Welcome " + newUser.getName(), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!isLogged) {
            mAuth.signOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }
}