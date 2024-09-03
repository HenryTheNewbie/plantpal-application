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

public class TermsOfUseActivity extends AppCompatActivity {

    private ImageView backButton;

    private TextView termsOfUseTextView;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_of_use_page);

        backButton = findViewById(R.id.terms_of_use_back_button);

        termsOfUseTextView = findViewById(R.id.termsOfUseText);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("terms_of_use.txt");

        fetchTermsOfUse();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TermsOfUseActivity.this, AppSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchTermsOfUse() {
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String termsOfUseText = new String(bytes);
                termsOfUseTextView.setText(termsOfUseText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("TermsOfUseActivity", "Failed to fetch terms of use", e);
            }
        });
    }
}
