package com.opd.therament.utilities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.opd.therament.R;

public class ImagePickerDialog extends Dialog implements View.OnClickListener {

    ImageView ivCamera, ivGallery, ivCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker);
        Window window = getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        ivCamera = findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(this);
        ivGallery = findViewById(R.id.iv_gallery);
        ivGallery.setOnClickListener(this);
        ivCancel = findViewById(R.id.iv_cancel);
        ivCancel.setOnClickListener(this);
    }

    public ImagePickerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.iv_camera: {

            }
            break;

            case R.id.iv_gallery: {

            }
            break;

            case R.id.iv_cancel: {

            }
            break;
        }
    }
}
