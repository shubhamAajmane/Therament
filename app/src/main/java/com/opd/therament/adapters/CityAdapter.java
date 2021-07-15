package com.opd.therament.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.opd.therament.R;
import com.opd.therament.datamodels.CityDatamodel;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    Context context;
    ArrayList<CityDatamodel> cities;
    onCityClickListener onCityClick;

    public CityAdapter(Context context, ArrayList<CityDatamodel> cities, onCityClickListener onCityClick) {
        this.context = context;
        this.cities = cities;
        this.onCityClick = onCityClick;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_cities, parent, false);
        return new CityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        holder.tvCity.setText(cities.get(position).getName());

        holder.tvCity.setOnClickListener(view -> onCityClick.onCityClick(view, position));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {

        TextView tvCity;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_city);
        }
    }

    public interface onCityClickListener {
        void onCityClick(View view, Integer position);
    }
}
