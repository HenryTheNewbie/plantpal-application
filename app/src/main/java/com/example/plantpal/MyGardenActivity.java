package com.example.plantpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyGardenActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private ImageView appSettingsButton;

    private ImageView profilePicture;
    private TextView username;
    private TextView userBio;
    private Button editProfileButton;

    private RecyclerView myGardenRecyclerView;
    private MyGardenAdapter myGardenAdapter;
    private List<Plant> myGardenList;
    private TextView noPlantsTextView;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_garden_page);

        profilePicture = findViewById(R.id.profile_picture);
        username = findViewById(R.id.my_garden_username);
        userBio = findViewById(R.id.my_garden_bio);
        editProfileButton = findViewById(R.id.edit_profile_button);

        bottomNavigationView = findViewById(R.id.navigation_bar);

        myGardenRecyclerView = findViewById(R.id.my_garden_recycler_view);

        myGardenList = new ArrayList<>();
        myGardenAdapter = new MyGardenAdapter(myGardenList, this);
        myGardenRecyclerView.setAdapter(myGardenAdapter);
        myGardenRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        noPlantsTextView = findViewById(R.id.no_plants_text);

        appSettingsButton = findViewById(R.id.settings_icon);

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        String userEmail = sharedPreferences.getString("email", "User");

        if (userEmail != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");

            usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            username.setText(user.getUsername());
                            userBio.setText(user.getBio());

                            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePictureUrl()).into(profilePicture);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MyGardenActivity.this, "Error loading user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyGardenActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        int selectedItemId = getIntent().getIntExtra("selected_item_id", -1);

        if (selectedItemId != -1) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        appSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyGardenActivity.this, AppSettingsActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {@Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_plant_scan) {
                    Intent intent = new Intent(MyGardenActivity.this, ScanActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_plant_archive) {
                    Intent intent = new Intent(MyGardenActivity.this, PlantArchiveActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_community_hub) {
                    Intent intent = new Intent(MyGardenActivity.this, CommunityForumActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_my_garden) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        loadMyGardenPlants();
    }

    private void loadMyGardenPlants() {
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference bookmarkedPlantsRef = databaseReference.child("myGarden").child(username).child("bookmarkedPlants");
        bookmarkedPlantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myGardenList.clear();

                    for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                        String plantName = plantSnapshot.getKey();

                        DatabaseReference plantsRef = databaseReference.child("plants");
                        plantsRef.orderByChild("commonName").equalTo(plantName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot plantDataSnapshot : dataSnapshot.getChildren()) {
                                    Plant plant = plantDataSnapshot.getValue(Plant.class);
                                    if (plant != null) {
                                        myGardenList.add(plant);
                                    }
                                }

                                myGardenAdapter.notifyDataSetChanged();

                                if (myGardenList.isEmpty()) {
                                    myGardenRecyclerView.setVisibility(View.GONE);
                                    noPlantsTextView.setVisibility(View.VISIBLE);
                                } else {
                                    myGardenRecyclerView.setVisibility(View.VISIBLE);
                                    noPlantsTextView.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MyGardenActivity.this, "Error loading plant data.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    myGardenRecyclerView.setVisibility(View.GONE);
                    noPlantsTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyGardenActivity.this, "Error loading bookmarked plants.", Toast.LENGTH_SHORT).show();
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
