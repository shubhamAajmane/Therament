package com.opd.therament.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.opd.therament.R;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.ConnectivityManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvForgot, tvSignup;
    Button btnLogin;
    SignInButton googleSignInButton;
    GoogleSignInClient googleSignInClient;
    private static final int SIGN_IN = 100;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    UserDataModel userDataModel = new UserDataModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        mAuth = FirebaseAuth.getInstance();

        TextView googleButtonLabel = (TextView) googleSignInButton.getChildAt(0);
        googleButtonLabel.setText("Continue with Google");

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        firestore = FirebaseFirestore.getInstance();
    }

    void init() {
        tvForgot = findViewById(R.id.tv_forgot);
        tvSignup = findViewById(R.id.tv_signup);
        btnLogin = findViewById(R.id.btn_login);
        googleSignInButton = findViewById(R.id.btn_google);

        tvForgot.setOnClickListener(this);
        tvSignup.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        googleSignInButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void addToDataBase(FirebaseUser currentUser) {

        CollectionReference userCollection = firestore.collection(getString(R.string.collection_users));

        userCollection.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot document : task.getResult()) {

                    UserDataModel oldUser = document.toObject(UserDataModel.class);

                    if (!oldUser.getUserId().equals(currentUser.getUid())) {

                        DocumentReference userDoc = userCollection.document(currentUser.getUid());

                        UserDataModel newUser = new UserDataModel();
                        newUser.setName(currentUser.getDisplayName());
                        newUser.setPhone(currentUser.getPhoneNumber());
                        newUser.setEmail(currentUser.getEmail());
                        newUser.setUserId(currentUser.getUid());
                        userDoc.set(newUser);
                        Toast.makeText(LoginActivity.this, "Welcome " + newUser.getName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                }
            } else {
                Log.d("FIRESTORE", task.getException().toString());
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tv_forgot: {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
            break;

            case R.id.btn_login: {

            }
            break;

            case R.id.tv_signup: {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
            break;

            case R.id.btn_google: {

                if (new ConnectivityManager().checkConnectivity(LoginActivity.this)) {
                    signIn();
                } else {
                    new AlertDialog.Builder(LoginActivity.this).setTitle("No Internet").setMessage("Please check your internet connection").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.finishAffinity(LoginActivity.this);
                        }
                    }).create().show();
                }
            }
        }
    }

    public void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException ex) {
                Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    updateUI(null);
                }
            }
        });
    }
}