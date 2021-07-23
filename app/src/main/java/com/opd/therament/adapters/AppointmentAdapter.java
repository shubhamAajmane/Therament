package com.opd.therament.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.opd.therament.R;
import com.opd.therament.datamodels.AppointmentDataModel;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentHolder> {

    Context context;
    ArrayList<AppointmentDataModel> dataModels;
    onCancelListener onCancelListener;

    public AppointmentAdapter(Context context, ArrayList<AppointmentDataModel> appointmentList, onCancelListener onCancelListener) {
        this.context = context;
        this.dataModels = appointmentList;
        this.onCancelListener = onCancelListener;
    }

    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_appointment, parent, false);

        return new AppointmentHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {
        AppointmentDataModel dataModel = dataModels.get(position);
        holder.tvTitle.setText(dataModel.getTitle());
        holder.tvDate.setText(dataModel.getSelectedDate());
        holder.tvTime.setText(dataModel.getSelectedTime());
        Glide.with(context).load(dataModel.getHospitalImage()).into(holder.ivLogo);
        holder.tvHospitalName.setText(dataModel.getHospitalName());
        holder.tvAddress.setText(dataModel.getHospitalAddress());
        holder.tvScheduleDate.setText(dataModel.getDate());
        holder.tvScheduleTime.setText(dataModel.getTime());

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelListener.onCancelled(dataModel, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataModels.size();
    }

    static class AppointmentHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        TextView tvHospitalName, tvAddress, tvTitle, tvDate, tvTime, tvScheduleDate, tvScheduleTime;
        Button btnCancel;

        public AppointmentHolder(View itemView) {
            super(itemView);

            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvHospitalName = itemView.findViewById(R.id.tv_hospital_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time_slot);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            tvScheduleDate = itemView.findViewById(R.id.tv_schedule_date);
            tvScheduleTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface onCancelListener {
        void onCancelled(AppointmentDataModel dataModel, int pos);
    }
}
