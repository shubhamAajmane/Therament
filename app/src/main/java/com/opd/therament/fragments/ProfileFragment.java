package com.opd.therament.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.opd.therament.R;
import com.opd.therament.activities.LoginActivity;

public class ProfileFragment extends Fragment {

    Button btnLogout;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    TextView tvName, tvEmail, tvPhone, tvEdit;
    ImageView ivProfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        btnLogout = root.findViewById(R.id.btn_logout);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        tvName = root.findViewById(R.id.tv_name);
        tvEmail = root.findViewById(R.id.tv_email);
        tvPhone = root.findViewById(R.id.tv_phone);
        ivProfile = root.findViewById(R.id.iv_profile);
        tvEdit = root.findViewById(R.id.tv_edit);

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return root;
    }
}