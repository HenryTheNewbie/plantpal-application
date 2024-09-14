package com.example.plantpal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private Context context;

    public PlantAdapter(List<Plant> plantList, Context context) {
        this.plantList = plantList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        holder.commonNameTextView.setText(plant.getCommonName());
        holder.scientificNameTextView.setText(plant.getScientificName());

        ((PlantArchiveActivity) context).loadPlantImage(plant.getImageURL(), holder.plantImageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra("commonName", plant.getCommonName());
            intent.putExtra("scientificName", plant.getScientificName());
            intent.putExtra("family", plant.getFamily());
            intent.putExtra("description", plant.getDescription());
            intent.putExtra("imageURL", plant.getImageURL());
            intent.putExtra("sunlight", plant.getSunlight());
            intent.putExtra("water", plant.getWater());
            intent.putExtra("soil", plant.getSoil());
            intent.putExtra("fertilizer", plant.getFertilizer());
            intent.putExtra("temperature", plant.getTemperature());
            intent.putExtra("humidity", plant.getHumidity());
            intent.putExtra("extraCareInfo", plant.getExtraCareInfo());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public class PlantViewHolder extends RecyclerView.ViewHolder {

        public TextView commonNameTextView;
        public TextView scientificNameTextView;
        public ImageView plantImageView;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            commonNameTextView = itemView.findViewById(R.id.plant_common_name);
            scientificNameTextView = itemView.findViewById(R.id.plant_scientific_name);
            plantImageView = itemView.findViewById(R.id.plant_image);
        }
    }
}
