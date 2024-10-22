package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanResultsActivity extends AppCompatActivity {

    private ImageView backButton;

    private String imageUri;
    private ImageView userUploadedPlantImage;
    private RecyclerView matchingPlantsRecyclerView;
    private MatchingPlantsAdapter matchingPlantsAdapter;
    private ArrayList<Plant> plantList;
    private ArrayList<String> commonNameList;
    private ArrayList<String> confidenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_results_page);

        backButton = findViewById(R.id.scan_results_back_button);

        userUploadedPlantImage = findViewById(R.id.user_uploaded_plant_image);
        matchingPlantsRecyclerView = findViewById(R.id.matching_plants_recycler_view);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScanResultsActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        commonNameList = intent.getStringArrayListExtra("commonNameList");
        confidenceList = intent.getStringArrayListExtra("confidenceList");
        imageUri = intent.getStringExtra("imageUri");

        Log.d("ScanResultsActivity", "commonNameList: " + commonNameList);
        Log.d("ScanResultsActivity", "confidenceList: " + confidenceList);
        Log.d("ScanResultsActivity", "imageUri: " + imageUri);

        if (imageUri != null) {
            Uri imageUriObj = Uri.parse(imageUri);

            Picasso.get()
                    .load(imageUriObj)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(userUploadedPlantImage);
        } else {
            Picasso.get()
                    .load(R.drawable.ic_plant_placeholder)
                    .into(userUploadedPlantImage);
        }

        if (commonNameList != null && confidenceList != null) {
            fetchPlantData(commonNameList);
        }
    }

    private void fetchPlantData(List<String> commonNameList) {
        int totalPlants = commonNameList.size();
        plantList = new ArrayList<>(Collections.nCopies(totalPlants, null));
        DatabaseReference plantsRef = FirebaseDatabase.getInstance().getReference("plants");

        final int[] plantsFetched = {0};

        for (int i = 0; i < commonNameList.size(); i++) {
            String commonName = commonNameList.get(i);
            int finalI = i;
            plantsRef.orderByChild("commonName").equalTo(commonName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                                Plant plant = plantSnapshot.getValue(Plant.class);
                                if (plant != null) {
                                    plantList.set(finalI, plant);
                                }
                            }
                            plantsFetched[0]++;

                            if (plantsFetched[0] == totalPlants) {
                                updateRecyclerView();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(ScanResultsActivity.this,
                                    "Failed to fetch data: " + databaseError.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateRecyclerView() {
        matchingPlantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        matchingPlantsAdapter = new MatchingPlantsAdapter(this, plantList, confidenceList);
        matchingPlantsRecyclerView.setAdapter(matchingPlantsAdapter);
    }
}
