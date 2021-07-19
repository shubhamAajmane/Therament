package com.opd.therament.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.opd.therament.R;
import com.opd.therament.datamodels.HospitalDataModel;

import java.util.ArrayList;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    Context context;
    ArrayList<HospitalDataModel> hospitalsList;
    onHospitalClickListener onHospitalClickListener;

    public HospitalAdapter(Context context, ArrayList<HospitalDataModel> hospitalsList, onHospitalClickListener onHospitalClickListener) {
        this.context = context;
        this.hospitalsList = hospitalsList;
        this.onHospitalClickListener = onHospitalClickListener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_hospitals, parent, false);

        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        HospitalDataModel hospitalModel = hospitalsList.get(position);
        holder.tvTitle.setText(hospitalModel.getName());
        holder.tvSubtitle.setText(hospitalModel.getAddress());
        holder.tvRatings.setText(hospitalModel.getRating());
        Glide.with(context).load(hospitalModel.getImageUrl()).into(holder.ivLogo);

        holder.itemView.setOnClickListener(view -> {
            onHospitalClickListener.onHospitalClick(view, position, hospitalModel, holder.ivLogo);
        });
    }

    @Override
    public int getItemCount() {
        return hospitalsList.size();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        TextView tvTitle, tvSubtitle, tvRatings;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvRatings = itemView.findViewById(R.id.tv_ratings);
        }
    }

    public interface onHospitalClickListener {

        void onHospitalClick(View view, int position, HospitalDataModel hospitalModel, ImageView ivLogo);
    }

    public void updateList(ArrayList<HospitalDataModel> hospitalsList) {
        this.hospitalsList = hospitalsList;
        notifyDataSetChanged();
    }
}
