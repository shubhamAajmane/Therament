package com.opd.therament.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.activities.LoginActivity;
import com.opd.therament.datamodels.UserDataModel;
import com.opd.therament.utilities.ImagePickerDialog;
import com.opd.therament.utilities.LoadingDialog;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    Button btnLogout;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    SharedPreferences sharedPreferences;
    TextView tvName, tvEmail, tvPhone, tvEdit;
    EditText etName, etEmail, etPhone;
    CardView imageLayout;
    ImageView ivProfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        btnLogout = root.findViewById(R.id.btn_logout);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        tvName = root.findViewById(R.id.tv_name);
        tvEmail = root.findViewById(R.id.tv_email);
        tvPhone = root.findViewById(R.id.tv_phone);
        ivProfile = root.findViewById(R.id.iv_profile);
        tvEdit = root.findViewById(R.id.tv_edit);
        imageLayout = root.findViewById(R.id.profile_layout);
        etName = root.findViewById(R.id.et_name);
        etEmail = root.findViewById(R.id.et_email);
        etPhone = root.findViewById(R.id.et_phone);

        LoadingDialog.showDialog(getActivity());
        getUserData();

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        imageLayout.setOnClickListener(view -> {
            ImagePickerDialog pickerDialog = new ImagePickerDialog(requireContext());
            pickerDialog.show();
        });

        tvEdit.setOnClickListener(view -> {

            if (tvEdit.getText().toString().equals("Edit")) {
                tvEdit.setText("Done");

                tvName.setVisibility(View.INVISIBLE);
                tvPhone.setVisibility(View.INVISIBLE);
                tvEmail.setVisibility(View.INVISIBLE);

                etName.setVisibility(View.VISIBLE);
                etPhone.setVisibility(View.VISIBLE);
                etEmail.setVisibility(View.VISIBLE);
            } else {

                if (etName.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPhone.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    LoadingDialog.showDialog(getContext());
                    tvEdit.setText("Edit");
                    UserDataModel newUser = new UserDataModel();
                    newUser.setName(etName.getText().toString());
                    newUser.setPhone(etPhone.getText().toString());
                    newUser.setEmail(etEmail.getText().toString());
                    setData(newUser);
                    tvName.setVisibility(View.VISIBLE);
                    tvPhone.setVisibility(View.VISIBLE);
                    tvEmail.setVisibility(View.VISIBLE);

                    etName.setVisibility(View.INVISIBLE);
                    etPhone.setVisibility(View.INVISIBLE);
                    etEmail.setVisibility(View.INVISIBLE);
                    updateProfile(newUser);
                }
            }
        });

        return root;
    }

    private void getUserData() {
        DocumentReference userRef = firestore.collection(getString(R.string.collection_users)).document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        userRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                DocumentSnapshot userDoc = task.getResult();

                assert userDoc != null;
                if (userDoc.exists()) {
                    UserDataModel userDataModel = userDoc.toObject(UserDataModel.class);
                    setData(userDataModel);
                    LoadingDialog.dismissDialog();
                } else {
                    LoadingDialog.dismissDialog();
                    //Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setData(UserDataModel userDataModel) {
        etName.setVisibility(View.INVISIBLE);
        etEmail.setVisibility(View.INVISIBLE);
        etPhone.setVisibility(View.INVISIBLE);

        tvName.setVisibility(View.VISIBLE);
        tvPhone.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.VISIBLE);

        tvName.setText(userDataModel.getName());
        etName.setText(userDataModel.getName());
        tvPhone.setText(userDataModel.getPhone());
        etPhone.setText(userDataModel.getPhone());
        tvEmail.setText(userDataModel.getEmail());
        etEmail.setText(userDataModel.getEmail());
    }

    private void updateProfile(UserDataModel newUser) {

        DocumentReference userDoc = firestore.collection(getString(R.string.collection_users)).document(mAuth.getCurrentUser().getUid());
        userDoc.update("name", newUser.getName(), "phone", newUser.getPhone(), "email", newUser.getEmail()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Updated Profile Successfully", Toast.LENGTH_SHORT).show();
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}