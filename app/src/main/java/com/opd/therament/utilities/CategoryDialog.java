package com.opd.therament.utilities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.opd.therament.R;

public class CategoryDialog extends Dialog implements View.OnClickListener {

    Button btnCancel, btnDone;
    getCategories getCategories;
    String category;
    Activity activity;
    RadioButton c1, c2, c3, c4, c5, c6;

    public CategoryDialog(@NonNull Activity activity, getCategories getCategories) {
        super(activity);
        this.activity = activity;
        this.getCategories = getCategories;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_layout);
        Window window = getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        btnCancel = findViewById(R.id.btn_cancel);
        btnDone = findViewById(R.id.btn_done);
        c1 = findViewById(R.id.category_children);
        c1.setOnClickListener(this);
        c2 = findViewById(R.id.category_bones);
        c2.setOnClickListener(this);
        c3 = findViewById(R.id.category_surgery);
        c3.setOnClickListener(this);
        c4 = findViewById(R.id.category_dental);
        c4.setOnClickListener(this);
        c5 = findViewById(R.id.category_cancer);
        c5.setOnClickListener(this);
        c6 = findViewById(R.id.category_covid19);
        c6.setOnClickListener(this);

        btnCancel.setOnClickListener(view -> {
            dismiss();
        });

        btnDone.setOnClickListener(view -> {
            getCategories.getCategoryList(category);
            dismiss();
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.category_children: {
                category = "Paediatrics";
            }
            break;

            case R.id.category_bones: {
                category = "Orthopedics";
            }
            break;

            case R.id.category_dental: {
                category = "Dental";
            }
            break;

            case R.id.category_surgery: {
                category = "Surgery";
            }
            break;

            case R.id.category_cancer: {
                category = "Cancer";
            }
            break;

            case R.id.category_covid19: {
                category = "Covid-19";
            }
            break;
        }
    }

    public interface getCategories {

        void getCategoryList(String category);
    }
}
