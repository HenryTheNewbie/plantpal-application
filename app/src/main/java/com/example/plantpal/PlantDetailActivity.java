package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PlantDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView bookmarkButton;

    private ImageView plantImage;
    private TextView plantCommonName, plantScientificName, plantFamily, plantDescription;
    private TextView plantSunlight, plantWater, plantSoil, plantFertilizer, plantTemperature, plantHumidity;
    private TextView plantExtraCareInfo;

    private FirebaseDatabase database;
    private DatabaseReference myGardenRef;

    private SharedPreferences sharedPreferences;

    private boolean isBookmarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_detail_page);

        backButton = findViewById(R.id.plant_detail_back_button);
        bookmarkButton = findViewById(R.id.bookmark_button);

        plantImage = findViewById(R.id.plant_image);
        plantCommonName = findViewById(R.id.plant_common_name_placeholder);
        plantScientificName = findViewById(R.id.plant_scientific_name_placeholder);
        plantFamily = findViewById(R.id.plant_family_text);
        plantDescription = findViewById(R.id.plant_description_text);
        plantSunlight = findViewById(R.id.plant_sunlight);
        plantWater = findViewById(R.id.plant_water);
        plantSoil = findViewById(R.id.plant_soil);
        plantFertilizer = findViewById(R.id.plant_fertilizer);
        plantTemperature = findViewById(R.id.plant_temperature);
        plantHumidity = findViewById(R.id.plant_humidity);
        plantExtraCareInfo = findViewById(R.id.plant_extra_information);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlantArchiveActivity.class);
            startActivity(intent);
        });

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");

        database = FirebaseDatabase.getInstance();
        myGardenRef = database.getReference("myGarden").child(username);

        Intent intent = getIntent();
        String commonName = intent.getStringExtra("commonName");
        String scientificName = intent.getStringExtra("scientificName");
        String family = intent.getStringExtra("family");
        String description = intent.getStringExtra("description");
        String imageURL = intent.getStringExtra("imageURL");
        String sunlight = intent.getStringExtra("sunlight");
        String water = intent.getStringExtra("water");
        String soil = intent.getStringExtra("soil");
        String fertilizer = intent.getStringExtra("fertilizer");
        String temperature = intent.getStringExtra("temperature");
        String humidity = intent.getStringExtra("humidity");
        String extraCareInfo = intent.getStringExtra("extraCareInfo");

        plantCommonName.setText(commonName);
        plantScientificName.setText(scientificName);
        plantFamily.setText(family);
        plantDescription.setText(description);
        plantSunlight.setText(sunlight);
        plantWater.setText(water);
        plantSoil.setText(soil);
        plantFertilizer.setText(fertilizer);
        plantTemperature.setText(temperature);
        plantHumidity.setText(humidity);
        plantExtraCareInfo.setText(extraCareInfo);

        loadImageFromURL(imageURL);

        checkIfBookmarked(commonName);

        bookmarkButton.setOnClickListener(v -> {
            if (isBookmarked) {
                removePlantFromMyGarden(commonName, username);
            } else {
                addPlantToMyGarden(commonName, username);
            }

            isBookmarked = !isBookmarked;
        });
    }

    private void loadImageFromURL(String url) {
        Picasso.get().load(url).into(plantImage);
    }

    private void checkIfBookmarked(String commonName) {
        myGardenRef.child("bookmarkedPlants").orderByValue().equalTo(commonName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            isBookmarked = true;
                            bookmarkButton.setImageResource(R.drawable.marked_bookmark_icon);
                        } else {
                            isBookmarked = false;
                            bookmarkButton.setImageResource(R.drawable.unmarked_bookmark_icon);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PlantDetailActivity", "Failed to check if plant is bookmarked", databaseError.toException());
                    }
                });
    }

    private void addPlantToMyGarden(String plantName, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myGardenRef = databaseReference.child("myGarden").child(username);
        DatabaseReference bookmarkedPlantsRef = myGardenRef.child("bookmarkedPlants");

        bookmarkedPlantsRef.child(plantName).setValue(plantName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    bookmarkButton.setImageResource(R.drawable.marked_bookmark_icon);
                    Toast.makeText(PlantDetailActivity.this, "Added to My Garden.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlantDetailActivity.this, "Failed to add plant. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removePlantFromMyGarden(String plantName, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myGardenRef = databaseReference.child("myGarden").child(username);
        DatabaseReference bookmarkedPlantsRef = myGardenRef.child("bookmarkedPlants");

        bookmarkedPlantsRef.child(plantName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    bookmarkButton.setImageResource(R.drawable.unmarked_bookmark_icon);
                    Toast.makeText(PlantDetailActivity.this, "Removed from My Garden.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlantDetailActivity.this, "Failed to remove plant. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
