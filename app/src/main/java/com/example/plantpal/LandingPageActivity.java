package com.example.plantpal;

import android.os.Bundle;
import android.widget.Button;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class LandingPageActivity extends AppCompatActivity {

    private Button temporaryBackButton;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        temporaryBackButton = findViewById(R.id.temporary_back_button);
        temporaryBackButton.setOnClickListener(view -> finish());

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
