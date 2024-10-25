package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;

public class PlantDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView bookmarkButton;

    private ImageView plantImage;
    private TextView plantCommonName, plantScientificName, plantFamily, plantDescription, plantCategory;
    private TextView plantSunlight, plantWater, plantSoil, plantFertilizer, plantTemperature, plantHumidity;
    private TextView plantExtraCareInfo;
    private RelativeLayout plantHerbalPropertiesContent;
    private TextView plantHerbalProperties;
    private RelativeLayout diseaseContent;
    private TextView diseaseCommonName, diseaseScientificName, diseaseDescription, diseasePossibleCauses, diseasePrevention, diseaseTreatmentSuggestion, diseaseVisibleSymptoms;

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
        plantCategory = findViewById(R.id.plant_category_text);
        plantHerbalProperties = findViewById(R.id.plant_herbal_properties);

        plantHerbalPropertiesContent = findViewById(R.id.plant_herbal_properties_content);

        diseaseContent = findViewById(R.id.plant_possible_disease_content);
        diseaseCommonName = findViewById(R.id.detected_disease_common_name);
        diseaseScientificName = findViewById(R.id.detected_disease_scientific_name);
        diseaseDescription = findViewById(R.id.detected_disease_description);
        diseasePossibleCauses = findViewById(R.id.disease_possible_causes_text);
        diseasePrevention = findViewById(R.id.disease_prevention_method_text);
        diseaseTreatmentSuggestion = findViewById(R.id.disease_treatment_suggestion_text);
        diseaseVisibleSymptoms = findViewById(R.id.disease_visible_symptoms_text);

        backButton.setOnClickListener(v -> {
            finish();
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
        String category = intent.getStringExtra("category");
        String herbalProperties = intent.getStringExtra("herbalProperties");

        String diseaseCommonNameText = intent.getStringExtra("diseaseCommonName");
        String diseaseScientificNameText = intent.getStringExtra("diseaseScientificName");
        String diseaseDescriptionText = intent.getStringExtra("diseaseDescription");
        ArrayList<String> visibleSymptoms = intent.getStringArrayListExtra("visibleSymptoms");
        String diseasePossibleCausesText = intent.getStringExtra("diseasePossibleCauses");
        String diseasePreventionText = intent.getStringExtra("diseasePrevention");
        String diseaseTreatmentSuggestionText = intent.getStringExtra("diseaseTreatmentSuggestion");

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
        plantCategory.setText(category);

        if (herbalProperties == null || herbalProperties.trim().isEmpty()) {
            plantHerbalPropertiesContent.setVisibility(View.GONE);
        } else {
            plantHerbalPropertiesContent.setVisibility(View.VISIBLE);
            plantHerbalProperties.setText(herbalProperties);
        }

        if (diseaseCommonNameText != null) {
            diseaseContent.setVisibility(View.VISIBLE);

            diseaseCommonName.setText(diseaseCommonNameText);
            diseaseScientificName.setText(diseaseScientificNameText);
            diseaseDescription.setText(diseaseDescriptionText);
            diseasePossibleCauses.setText(diseasePossibleCausesText);
            diseasePrevention.setText(diseasePreventionText);
            diseaseTreatmentSuggestion.setText(diseaseTreatmentSuggestionText);

            if (visibleSymptoms != null && !visibleSymptoms.isEmpty()) {
                StringBuilder visibleSymptomsText = new StringBuilder();
                for (int i = 0; i < visibleSymptoms.size(); i++) {
                    String symptom = visibleSymptoms.get(i);
                    visibleSymptomsText.append(symptom);
                    if (i < visibleSymptoms.size() - 1) {
                        visibleSymptomsText.append("\n");
                    }
                }
                diseaseVisibleSymptoms.setText(visibleSymptomsText.toString());
            } else {
                diseaseVisibleSymptoms.setText("No visible symptoms.");
            }
        } else {
            diseaseContent.setVisibility(View.GONE);
        }

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
