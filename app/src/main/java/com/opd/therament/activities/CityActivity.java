package com.opd.therament.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.adapters.CityAdapter;
import com.opd.therament.datamodels.CityDataModel;
import com.opd.therament.utilities.LoadingDialog;

import java.util.ArrayList;
import java.util.Objects;

public class CityActivity extends AppCompatActivity implements CityAdapter.onCityClickListener {

    RecyclerView rvCities;
    CityAdapter cityAdapter;
    FirebaseFirestore firestore;
    ArrayList<CityDataModel> cityList;
    SharedPreferences sharedPreferences;
    EditText etSearch;

    @Override
    protected void onResume() {
        super.onResume();
        LoadingDialog.showDialog(this);
        getCities();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        init();
        sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();
    }

    public void init() {
        rvCities = findViewById(R.id.rv_cities);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.et_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null) {

                    if (etSearch.getText().toString().isEmpty()) {
                        getCities();
                    } else {
                        searchCity(etSearch.getText().toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void searchCity(String cityName) {
        ArrayList<CityDataModel> sortedList = new ArrayList<>();

        CollectionReference hospitalsColl = firestore.collection(getString(R.string.collection_cities));

        hospitalsColl.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    CityDataModel datamodel = doc.toObject(CityDataModel.class);

                    if (datamodel.getName().contains(cityName)) {
                        sortedList.add(datamodel);
                    }
                }
                rvCities.setVisibility(View.VISIBLE);
                cityAdapter.updateList(sortedList);
                LoadingDialog.dismissDialog();
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCities() {
        cityList = new ArrayList<>();
        CollectionReference collCity = firestore.collection(getString(R.string.collection_cities));

        collCity.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {

                    CityDataModel cityDatamodel = doc.toObject(CityDataModel.class);

                    cityList.add(cityDatamodel);
                }

                if (cityList != null) {
                    cityAdapter = new CityAdapter(CityActivity.this, cityList, this);
                    rvCities.setAdapter(cityAdapter);
                    LoadingDialog.dismissDialog();
                }
            } else {
                LoadingDialog.dismissDialog();
                Toast.makeText(CityActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCityClick(View view, Integer position) {
        sharedPreferences.edit().putString("city", cityList.get(position).getName()).apply();
        startActivity(new Intent(CityActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(CityActivity.this, "Please select a city", Toast.LENGTH_SHORT).show();
    }
}