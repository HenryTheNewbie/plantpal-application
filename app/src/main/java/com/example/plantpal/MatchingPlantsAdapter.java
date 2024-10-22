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

import com.squareup.picasso.Picasso;
import com.example.plantpal.R;

import java.util.List;

public class MatchingPlantsAdapter extends RecyclerView.Adapter<MatchingPlantsAdapter.PlantViewHolder> {

    private Context context;
    private List<Plant> plantList;
    private List<String> confidenceList;

    public MatchingPlantsAdapter(Context context, List<Plant> plantList, List<String> confidenceList) {
        this.context = context;
        this.plantList = plantList;
        this.confidenceList = confidenceList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scan_results_item, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        String confidence = confidenceList.get(position);

        holder.commonNameTextView.setText(plant.getCommonName());
        holder.scientificNameTextView.setText(plant.getScientificName());
        holder.confidenceTextView.setText(confidence + "%" + " Confidence");

        Picasso.get()
                .load(plant.getImageURL())
                .placeholder(R.drawable.ic_plant_placeholder)
                .error(R.drawable.ic_plant_placeholder)
                .into(holder.plantImageView);

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
            intent.putExtra("category", plant.getCategory());
            intent.putExtra("herbalProperties", plant.getHerbalProperties());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImageView;
        TextView commonNameTextView;
        TextView scientificNameTextView;
        TextView confidenceTextView;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);

            plantImageView = itemView.findViewById(R.id.scan_results_plant_image);
            commonNameTextView = itemView.findViewById(R.id.scan_results_plant_common_name);
            scientificNameTextView = itemView.findViewById(R.id.scan_results_plant_scientific_name);
            confidenceTextView = itemView.findViewById(R.id.scan_results_confidence);
        }
    }
}