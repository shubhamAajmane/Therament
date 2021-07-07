package com.opd.therament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;

public class NewPasswordActivity extends AppCompatActivity {

    EditText etPass, etConfirm;
    Button btnLogin;
    String name, phone;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        etPass = findViewById(R.id.et_pass);
        etConfirm = findViewById(R.id.et_confirm_pass);
        btnLogin = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        if (intent != null) {
            name = intent.getStringExtra("name");
            phone = intent.getStringExtra("phone");
        }

        btnLogin.setOnClickListener(view -> {
            String pass = etPass.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(NewPasswordActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                Toast.makeText(NewPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                addToDatabase(phone);
            }
        });
    }

    private void addToDatabase(String password) {

        if(mAuth!=null) {

            FirebaseUser user = mAuth.getCurrentUser();
            String userId = user.getUid();
            Log.d("USERID",userId);
            DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(userId);
            userDoc.update("password", password);

            startActivity(new Intent(NewPasswordActivity.this, LoginActivity.class));
            Toast.makeText(NewPasswordActivity.this, "Password changed successfully " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            finish();
        } else {
            Toast.makeText(NewPasswordActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.signOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }
}