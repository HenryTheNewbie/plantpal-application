package com.example.plantpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlantArchiveActivity extends AppCompatActivity {

    private Button temporaryBackButton;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_archive_page);

        temporaryBackButton = findViewById(R.id.temporary_back_button);
        temporaryBackButton.setOnClickListener(view -> finish());

        bottomNavigationView = findViewById(R.id.navigation_bar);

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
    }
}
