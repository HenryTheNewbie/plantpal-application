package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AboutTheAppActivity extends AppCompatActivity {

    private ImageView backButton;

    private TextView aboutTheAppTextView;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_the_app_page);

        backButton = findViewById(R.id.about_the_app_back_button);

        aboutTheAppTextView = findViewById(R.id.aboutTheAppText);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("about_the_app.txt");

        fetchAboutTheApp();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AboutTheAppActivity.this, AppSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchAboutTheApp() {
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String aboutTheAppText = new String(bytes);
                aboutTheAppTextView.setText(aboutTheAppText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("AboutTheAppActivity", "Failed to fetch about the app", e);
            }
        });
    }
}
