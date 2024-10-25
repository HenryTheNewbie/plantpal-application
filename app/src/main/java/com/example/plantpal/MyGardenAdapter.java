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

public class MyGardenAdapter extends RecyclerView.Adapter<MyGardenAdapter.MyGardenViewHolder> {

    private List<Plant> myGardenList;
    public Context context;

    public MyGardenAdapter(List<Plant> myGardenList, Context context) {
        this.myGardenList = myGardenList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyGardenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_garden_item, parent, false);
        return new MyGardenViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGardenViewHolder holder, int position) {
        Plant plant = myGardenList.get(position);

        holder.commonNameTextView.setText(plant.getCommonName());
        holder.scientificNameTextView.setText(plant.getScientificName());

        ((MyGardenActivity) context).loadPlantImage(plant.getImageURL(), holder.plantImageView);

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
        return myGardenList.size();
    }

public static class MyGardenViewHolder extends RecyclerView.ViewHolder {
        TextView commonNameTextView;
        TextView scientificNameTextView;
        ImageView plantImageView;

        public MyGardenViewHolder(@NonNull View itemView) {
            super(itemView);
            commonNameTextView = itemView.findViewById(R.id.my_garden_plant_common_name);
            scientificNameTextView = itemView.findViewById(R.id.my_garden_plant_scientific_name);
            plantImageView = itemView.findViewById(R.id.my_garden_plant_image);
        }
    }
}
