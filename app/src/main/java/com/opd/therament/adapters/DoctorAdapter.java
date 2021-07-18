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
import com.opd.therament.datamodels.DoctorDatamodel;

import java.util.ArrayList;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorsViewHolder> {

    Context context;
    ArrayList<DoctorDatamodel> doctorsList;

    public DoctorAdapter(Context context, ArrayList<DoctorDatamodel> doctorsList) {
        this.context = context;
        this.doctorsList = doctorsList;
    }

    @NonNull
    @Override
    public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_doctors, parent, false);

        return new DoctorsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorsViewHolder holder, int position) {
        DoctorDatamodel datamodel = doctorsList.get(position);

        Glide.with(context).load(datamodel.getImage()).placeholder(R.drawable.ic_profile).into(holder.ivProfile);
        holder.tvName.setText(datamodel.getName());
        holder.tvDegree.setText(datamodel.getDegree());
    }

    @Override
    public int getItemCount() {
        return doctorsList.size();
    }

    static class DoctorsViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfile;
        TextView tvName, tvDegree;

        DoctorsViewHolder(View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDegree = itemView.findViewById(R.id.tv_degree);
        }
    }
}
