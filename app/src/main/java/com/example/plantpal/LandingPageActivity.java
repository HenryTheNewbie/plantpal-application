package com.example.plantpal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LandingPageActivity extends AppCompatActivity {

    private RelativeLayout scanCard;
    private RelativeLayout plantArchiveCard;
    private RelativeLayout communityHubCard;
    private RelativeLayout myGardenCard;
    private BottomNavigationView bottomNavigationView;
    private TextView headerUsername;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        scanCard = findViewById(R.id.scan_card);
        plantArchiveCard = findViewById(R.id.plant_archive_card);
        communityHubCard = findViewById(R.id.community_hub_card);
        myGardenCard = findViewById(R.id.my_garden_card);

        bottomNavigationView = findViewById(R.id.navigation_bar);

        headerUsername = findViewById(R.id.header_username);
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        String username = sharedPreferences.getString("username", "User");
        headerUsername.setText(username + ".");

        scanCard.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPageActivity.this, ScanActivity.class);
            intent.putExtra("selected_item_id", R.id.nav_plant_scan);
            startActivity(intent);
        });

        plantArchiveCard.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPageActivity.this, PlantArchiveActivity.class);
            intent.putExtra("selected_item_id", R.id.nav_plant_archive);
            startActivity(intent);
        });

        communityHubCard.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPageActivity.this, CommunityForumActivity.class);
            intent.putExtra("selected_item_id", R.id.nav_community_hub);
            startActivity(intent);
        });

        myGardenCard.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPageActivity.this, MyGardenActivity.class);
            intent.putExtra("selected_item_id", R.id.nav_my_garden);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setSelectedItemId(R.id.dummy_item);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Intent intent;

                if (itemId == R.id.nav_plant_scan) {
                    intent = new Intent(LandingPageActivity.this, ScanActivity.class);
                } else if (itemId == R.id.nav_plant_archive) {
                    intent = new Intent(LandingPageActivity.this, PlantArchiveActivity.class);
                } else if (itemId == R.id.nav_community_hub) {
                    intent = new Intent(LandingPageActivity.this, CommunityForumActivity.class);
                } else if (itemId == R.id.nav_my_garden) {
                    intent = new Intent(LandingPageActivity.this, MyGardenActivity.class);
                } else {
                    return false;
                }

                intent.putExtra("selected_item_id", itemId);
                startActivity(intent);
                return true;
            }
        });
    }
}
