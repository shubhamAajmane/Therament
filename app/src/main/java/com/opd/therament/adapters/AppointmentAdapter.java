package com.opd.therament.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opd.therament.R;
import com.opd.therament.activities.WriteReviewActivity;
import com.opd.therament.datamodels.AppointmentDataModel;
import com.opd.therament.datamodels.ReviewDataModel;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentHolder> {

    Context context;
    ArrayList<AppointmentDataModel> dataModels;
    onCancelListener onCancelListener;
    ItemViewClick onItemClicked;
    String type;

    public AppointmentAdapter(Context context, ArrayList<AppointmentDataModel> appointmentList, ItemViewClick onItemClicked, String type) {
        this.context = context;
        this.dataModels = appointmentList;
        this.onItemClicked = onItemClicked;
        this.type = type;
    }

    public AppointmentAdapter(Context context, ArrayList<AppointmentDataModel> appointmentList, onCancelListener onCancelListener, ItemViewClick onItemClicked, String type) {
        this.context = context;
        this.dataModels = appointmentList;
        this.onCancelListener = onCancelListener;
        this.onItemClicked = onItemClicked;
        this.type = type;
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
        holder.tvScheduleDate.setText(String.format("%s %s", dataModel.getDate(), dataModel.getTime()));
        holder.btnCancel.setOnClickListener(view -> onCancelListener.onCancelled(dataModel));

        holder.itemView.setOnClickListener(view -> {
            onItemClicked.onItemClicked(dataModel);
        });

        if (type.equals("History")) {

            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.INVISIBLE);

            if (dataModel.getBooked()) {
                holder.tvStatus.setText("Appointment Done");
                holder.ratingBar.setVisibility(View.VISIBLE);
                DocumentReference doc = FirebaseFirestore.getInstance().collection(context.getString(R.string.collection_hospitals)).document(dataModel.getHospitalId()).collection(context.getString(R.string.collection_reviews)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                doc.get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot ratingDoc = task.getResult();

                        if (ratingDoc.exists()) {
                            ReviewDataModel reviewDataModel = ratingDoc.toObject(ReviewDataModel.class);
                            float rating = Float.parseFloat(reviewDataModel.getRating());
                            holder.ratingBar.setRating(rating);
                        } else {
                            holder.ratingBar.setVisibility(View.GONE);
                        }

                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.ratingBar.setOnTouchListener((view, motionEvent) -> {

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        Intent intent = new Intent(context, WriteReviewActivity.class);
                        intent.putExtra("hospitalId", dataModel.getHospitalId());
                        intent.putExtra("appointmentId", dataModel.getId());
                        intent.putExtra("isHistory", true);
                        intent.putExtra("rating", holder.ratingBar.getRating());
                        context.startActivity(intent);
                    }
                    return true;
                });

            } else {
                holder.tvStatus.setText("Cancelled");
                holder.ratingBar.setVisibility(View.GONE);
            }

        } else {

            if (dataModel.getBooked()) {

                DocumentReference doc = FirebaseFirestore.getInstance().collection(context.getString(R.string.collection_hospitals)).document(dataModel.getHospitalId()).collection(context.getString(R.string.collection_reviews)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                doc.get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot review = task.getResult();

                        if (review.exists()) {
                            dataModels.remove(dataModel);
                            notifyDataSetChanged();
                            DocumentReference appointment = FirebaseFirestore.getInstance().collection(context.getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(context.getString(R.string.collection_appointments)).document(dataModel.getId());
                            appointment.delete();
                            DocumentReference historyDoc = FirebaseFirestore.getInstance().collection(context.getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(context.getString(R.string.collection_history)).document(dataModel.getId());
                            historyDoc.set(dataModel);
                        } else {
                            holder.tvStatus.setVisibility(View.VISIBLE);
                            holder.tvStatus.setText("Appointment Done");
                            holder.btnCancel.setVisibility(View.INVISIBLE);
                            holder.ratingBar.setVisibility(View.VISIBLE);

                            holder.ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                                Intent intent = new Intent(context, WriteReviewActivity.class);
                                intent.putExtra("hospitalId", dataModel.getHospitalId());
                                intent.putExtra("appointmentId", dataModel.getId());
                                intent.putExtra("isHistory", false);
                                intent.putExtra("rating", v);
                                context.startActivity(intent);
                            });
                        }
                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.tvStatus.setVisibility(View.INVISIBLE);
                holder.ratingBar.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataModels.size();
    }

    static class AppointmentHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        TextView tvHospitalName, tvAddress, tvTitle, tvDate, tvTime, tvScheduleDate, tvStatus;
        Button btnCancel;
        RatingBar ratingBar;

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
            tvStatus = itemView.findViewById(R.id.tv_status);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }

    public interface onCancelListener {
        void onCancelled(AppointmentDataModel dataModel);
    }

    public interface ItemViewClick {
        void onItemClicked(AppointmentDataModel dataModel);
    }

    public void updateList(ArrayList<AppointmentDataModel> updateList) {
        this.dataModels = updateList;
        notifyDataSetChanged();
    }
}
