package com.example.plantpal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;

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
                if (itemId == R.id.nav_plant_scan) {
                    // Handle Plant Scan action
                    return true;
                } else if (itemId == R.id.nav_plant_archive) {
                    // Handle Plant Archive action
                    return true;
                } else if (itemId == R.id.nav_community_hub) {
                    // Handle Community Hub action
                    return true;
                } else if (itemId == R.id.nav_my_garden) {
                    // Handle My Garden action
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
}
