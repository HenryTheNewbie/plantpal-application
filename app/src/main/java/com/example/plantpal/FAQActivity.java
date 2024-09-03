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

public class FAQActivity extends AppCompatActivity {

    private ImageView backButton;

    private TextView faqTextView;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faq_page);

        backButton = findViewById(R.id.faq_back_button);

        faqTextView = findViewById(R.id.frequentlyAskedQuestionsText);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("faq.txt");

        fetchFAQ();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FAQActivity.this, AppSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchFAQ() {
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String faqText = new String(bytes);
                faqTextView.setText(faqText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("FAQActivity", "Failed to fetch FAQ", e);
            }
        });
    }
}
