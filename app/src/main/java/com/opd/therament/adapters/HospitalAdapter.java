package com.opd.therament.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.opd.therament.R;
import com.opd.therament.datamodels.HospitalDatamodel;

import java.util.ArrayList;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    Context context;
    ArrayList<HospitalDatamodel> hospitalsList;
    onHospitalClickListener onHospitalClickListener;

    public HospitalAdapter(Context context, ArrayList<HospitalDatamodel> hospitalsList,onHospitalClickListener onHospitalClickListener) {
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
        HospitalDatamodel hospitalModel = hospitalsList.get(position);
        holder.tvTitle.setText(hospitalModel.getName());
        holder.tvSubtitle.setText(hospitalModel.getAddress());
        holder.tvRatings.setText(hospitalModel.getRating());

        holder.itemView.setOnClickListener(view -> {
            onHospitalClickListener.onHospitalClick(view,position);
        });
    }

    @Override
    public int getItemCount() {
        return hospitalsList.size();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        TextView tvTitle, tvSubtitle,tvRatings;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvRatings = itemView.findViewById(R.id.tv_ratings);
        }
    }

    public interface onHospitalClickListener {

         void onHospitalClick(View view,int position);
    }

    public void updateList(ArrayList<HospitalDatamodel> hospitalsList) {
        this.hospitalsList = hospitalsList;
        notifyDataSetChanged();
    }
}
