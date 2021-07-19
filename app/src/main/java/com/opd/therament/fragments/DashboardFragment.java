package com.opd.therament.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.opd.therament.R;
import com.opd.therament.activities.CityActivity;
import com.opd.therament.activities.HospitalActivity;
import com.opd.therament.adapters.HospitalAdapter;
import com.opd.therament.datamodels.HospitalDataModel;
import com.opd.therament.utilities.CategoryDialog;

import java.util.ArrayList;
import java.util.Objects;

public class DashboardFragment extends Fragment implements HospitalAdapter.onHospitalClickListener, CategoryDialog.getCategories {

    SharedPreferences sharedPreferences;
    TextView tvCityName;
    FirebaseFirestore firestore;
    RecyclerView rvHospitals;
    String cityName;
    ArrayList<HospitalDataModel> hospitalsList = new ArrayList<>();
    ImageView ivCategory;
    HospitalAdapter hospitalAdapter;
    EditText etSearch;

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
                    HospitalDataModel datamodel = doc.toObject(HospitalDataModel.class);
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
        etSearch = root.findViewById(R.id.et_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null) {
                    searchHospitals(etSearch.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void searchHospitals(String search) {
        ArrayList<HospitalDataModel> sortedList = new ArrayList<>();

        CollectionReference hospitalsColl = firestore.collection(getString(R.string.collection_hospitals));

        hospitalsColl.whereEqualTo("city", cityName).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    HospitalDataModel datamodel = doc.toObject(HospitalDataModel.class);

                    if (datamodel.getName().contains(search)) {
                        sortedList.add(datamodel);
                    }
                }
                hospitalAdapter.updateList(sortedList);

            } else {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onHospitalClick(View view, int position, HospitalDataModel hospitalModel, ImageView ivLogo) {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), ivLogo, "animation");

        String hospitalDetails = new Gson().toJson(hospitalModel);
        Log.d("DETAILS", hospitalDetails);
        Intent intent = new Intent(getActivity(), HospitalActivity.class);
        intent.putExtra("hospitalDetails", hospitalDetails);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void getCategoryList(String category) {

        if (category.equals("All")) {
            getHospitals();
        } else {
            ArrayList<HospitalDataModel> sortedList = new ArrayList<>();

            CollectionReference hospitalsColl = firestore.collection(getString(R.string.collection_hospitals));

            hospitalsColl.whereEqualTo("category", category).whereEqualTo("city", cityName).get().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        HospitalDataModel datamodel = doc.toObject(HospitalDataModel.class);
                        sortedList.add(datamodel);
                    }
                    hospitalAdapter.updateList(sortedList);

                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        sharedPreferences.edit().putInt("clickId", R.id.category_all).apply();
    }
}