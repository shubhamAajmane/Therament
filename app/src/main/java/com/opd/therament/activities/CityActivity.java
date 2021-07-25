package com.opd.therament.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Objects;

public class CityActivity extends AppCompatActivity implements CityAdapter.onCityClickListener {

    RecyclerView rvCities;
    CityAdapter cityAdapter;
    FirebaseFirestore firestore;
    ArrayList<CityDataModel> cityList = new ArrayList<>();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        init();
        sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();
        getCities();
    }

    public void init() {
        rvCities = findViewById(R.id.rv_cities);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getCities() {
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
                }
            } else {
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