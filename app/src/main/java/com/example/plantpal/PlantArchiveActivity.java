package com.example.plantpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlantArchiveActivity extends AppCompatActivity {

    private EditText searchBar;
    private TextView searchSuggestionText;

    private BottomNavigationView bottomNavigationView;

    private RecyclerView plantRecyclerView;

    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private List<Plant> filteredPlantList;

    private DatabaseReference plantsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_archive_page);

        searchBar = findViewById(R.id.search_bar);

        searchSuggestionText = findViewById(R.id.search_suggestion_text);

        bottomNavigationView = findViewById(R.id.navigation_bar);

        plantRecyclerView = findViewById(R.id.plant_recycler_view);

        plantsRef = FirebaseDatabase.getInstance().getReference().child("plants");

        plantList = new ArrayList<>();
        filteredPlantList = new ArrayList<>();
        plantAdapter = new PlantAdapter(filteredPlantList, this);
        plantRecyclerView.setAdapter(plantAdapter);
        plantRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUI(s.toString());
                filterPlants(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Do nothing
            }
        });

        int selectedItemId = getIntent().getIntExtra("selected_item_id", -1);

        if (selectedItemId != -1) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {@Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_plant_scan) {
                    Intent intent = new Intent(PlantArchiveActivity.this, ScanActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_plant_archive) {
                    return true;
                } else if (itemId == R.id.nav_community_hub) {
                    Intent intent = new Intent(PlantArchiveActivity.this, CommunityForumActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_my_garden) {
                    Intent intent = new Intent(PlantArchiveActivity.this, MyGardenActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        loadPlants();
    }

    private void updateUI(String query) {
        if (query.isEmpty()) {
            searchSuggestionText.setVisibility(View.VISIBLE);
            plantRecyclerView.setVisibility(View.GONE);
        } else {
            searchSuggestionText.setVisibility(View.GONE);
            plantRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filterPlants(String query) {
        filteredPlantList.clear();

        if (query.isEmpty()) {
            filteredPlantList.addAll(plantList);
        } else {
            for (Plant plant : plantList) {
                if (plant.getCommonName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getScientificName().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlantList.add(plant);
                }
            }
        }

        plantAdapter.notifyDataSetChanged();
    }

    private void loadPlants() {
        plantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                plantList.clear();
                filteredPlantList.clear();

                for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    String commonName = plantSnapshot.child("commonName").getValue().toString();
                    String scientificName = plantSnapshot.child("scientificName").getValue().toString();
                    String family = plantSnapshot.child("family").getValue().toString();
                    String description = plantSnapshot.child("description").getValue().toString();
                    String imageURL = plantSnapshot.child("imageURL").getValue().toString();
                    String sunlight = plantSnapshot.child("sunlight").getValue().toString();
                    String water = plantSnapshot.child("water").getValue().toString();
                    String soil = plantSnapshot.child("soil").getValue().toString();
                    String fertilizer = plantSnapshot.child("fertilizer").getValue().toString();
                    String temperature = plantSnapshot.child("temperature").getValue().toString();
                    String humidity = plantSnapshot.child("humidity").getValue().toString();
                    String extraCareInfo = plantSnapshot.child("extraCareInfo").getValue().toString();

                    Plant plant = new Plant(commonName, scientificName, family, description, imageURL, sunlight, water, soil, fertilizer, temperature, humidity, extraCareInfo);
                    plantList.add(plant);
                }

                filterPlants(searchBar.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PlantArchiveActivity", "Failed to load plants", error.toException());
            }
        });
    }

    public void loadPlantImage(String imageUrl, ImageView plantImageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(plantImageView);
        } else {
            plantImageView.setImageResource(R.drawable.ic_plant_placeholder);
        }
    }
}
