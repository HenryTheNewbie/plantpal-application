package com.example.plantpal;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingPageActivity extends AppCompatActivity {

    private Button temporaryBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        temporaryBackButton = findViewById(R.id.temporary_back_button);
        temporaryBackButton.setOnClickListener(view -> finish());
    }
}
