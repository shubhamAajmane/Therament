package com.opd.therament.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.opd.therament.R;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener{

    EditText etPhone;
    Button sendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
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
                startActivity(new Intent(ForgotPassword.this,VerificationActivity.class));
            }
            break;
        }
    }
}