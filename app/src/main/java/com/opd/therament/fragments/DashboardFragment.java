package com.opd.therament.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.activities.CityActivity;
import com.opd.therament.adapters.HospitalAdapter;
import com.opd.therament.datamodels.HospitalDatamodel;
import com.opd.therament.utilities.CategoryDialog;

import java.util.ArrayList;
import java.util.Objects;

public class DashboardFragment extends Fragment implements HospitalAdapter.onHospitalClickListener, CategoryDialog.getCategories {

    SharedPreferences sharedPreferences;
    TextView tvCityName;
    FirebaseFirestore firestore;
    RecyclerView rvHospitals;
    String cityName;
    ArrayList<HospitalDatamodel> hospitalsList = new ArrayList<>();
    ImageView ivCategory;
    HospitalAdapter hospitalAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        init(root);

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        cityName = sharedPreferences.getString("city", "");
        tvCityName.setText(cityName);
        getHospitals();

        tvCityName.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), CityActivity.class));
        });

        ivCategory.setOnClickListener(view -> {
            CategoryDialog categoryDialog = new CategoryDialog(getActivity(), this);
            categoryDialog.show();
        });
        return root;
    }

    private void getHospitals() {
        CollectionReference hospitalsColl = firestore.collection(getString(R.string.collection_hospitals));

        hospitalsColl.whereEqualTo("city", cityName).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    HospitalDatamodel datamodel = doc.toObject(HospitalDatamodel.class);
                    hospitalsList.add(datamodel);
                }
                hospitalAdapter = new HospitalAdapter(getContext(), hospitalsList, this);
                rvHospitals.setAdapter(hospitalAdapter);

            } else {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(View root) {
        tvCityName = root.findViewById(R.id.tv_city);
        firestore = FirebaseFirestore.getInstance();
        rvHospitals = root.findViewById(R.id.rv_hospitals);
        rvHospitals.setLayoutManager(new LinearLayoutManager(getContext()));
        ivCategory = root.findViewById(R.id.iv_category);
    }

    @Override
    public void onHospitalClick(View view, int position) {

    }

    @Override
    public void getCategoryList(String category) {
        ArrayList<HospitalDatamodel> sortedList = new ArrayList<>();

        CollectionReference hospitalsColl = firestore.collection(getString(R.string.collection_hospitals));

        hospitalsColl.whereEqualTo("category", category).whereEqualTo("city", cityName).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    HospitalDatamodel datamodel = doc.toObject(HospitalDatamodel.class);
                    sortedList.add(datamodel);
                }
                hospitalAdapter.updateList(sortedList);

            } else {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}