package com.opd.therament.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.ConnectivityManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvSignup;
    Button btnLogin;
    SignInButton googleSignInButton;
    GoogleSignInClient googleSignInClient;
    private static final int SIGN_IN = 100;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    EditText etPhone;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    boolean isRegistered = false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseApp.initializeApp(this);

        TextView googleButtonLabel = (TextView) googleSignInButton.getChildAt(0);
        googleButtonLabel.setText("Continue with Google");

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(LoginActivity.this, "OTP has been sent to your phone no", Toast.LENGTH_SHORT).show();

                Intent login = new Intent(LoginActivity.this, VerificationActivity.class);
                login.putExtra("auth", s);
                login.putExtra("isLogin", true);
                startActivity(login);
            }
        };
    }

    void init() {
        tvSignup = findViewById(R.id.tv_signup);
        btnLogin = findViewById(R.id.btn_login);
        googleSignInButton = findViewById(R.id.btn_google);
        etPhone = findViewById(R.id.et_phone);

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
            addToDataBase(currentUser);
        }
    }

    private void addToDataBase(FirebaseUser currentUser) {

        CollectionReference userCollection = firestore.collection(getString(R.string.collection_users));

        userCollection.whereEqualTo("userId", currentUser.getUid()).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot document : task.getResult()) {

                    if (document.exists()) {
                        isRegistered = true;
                    }
                }

                if (!isRegistered) {
                    DocumentReference userDoc = userCollection.document(currentUser.getUid());

                    UserDataModel newUser = new UserDataModel();
                    newUser.setName(currentUser.getDisplayName());
                    newUser.setPhone(currentUser.getPhoneNumber());
                    newUser.setEmail(currentUser.getEmail());
                    newUser.setUserId(currentUser.getUid());
                    userDoc.set(newUser);
                    startActivity(new Intent(LoginActivity.this, CityActivity.class));
                } else {
                    String city = sharedPreferences.getString("city", "");

                    if (city.isEmpty()) {
                        startActivity(new Intent(LoginActivity.this, CityActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                }
                finish();

            } else {
                Log.d("FIRESTORE", task.getException().toString());
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_login: {
                if (etPhone.getText().toString().isEmpty() || etPhone.getText().toString().length() != 10) {
                    etPhone.setError("Invalid phone no");
                } else {
                    if (new ConnectivityManager().checkConnectivity(LoginActivity.this)) {
                        checkPhoneNo(etPhone.getText().toString());
                    } else {
                        new AlertDialog.Builder(LoginActivity.this).setTitle("No Internet").setMessage("Please check your internet connection").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).create().show();
                    }
                }
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
                    }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create().show();
                }
            }
        }
    }

    private void checkPhoneNo(String phone) {
        CollectionReference userCollection = firestore.collection(getString(R.string.collection_users));

        userCollection.whereEqualTo("phone", phone).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                boolean isRegistered = false;

                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                    if (documentSnapshot.exists()) {
                        isRegistered = true;
                    }
                }

                if (!isRegistered) {
                    Toast.makeText(LoginActivity.this, "Phone no not registered", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, SignupActivity.class);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                } else {
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                            .setPhoneNumber("+91" + phone).setTimeout(60L, TimeUnit.SECONDS)
                            .setCallbacks(mCallbacks)
                            .setActivity(LoginActivity.this)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
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

                    assert user != null;
                    Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    updateUI(user);
                } else {
                    updateUI(null);
                }
            }
        });
    }
}