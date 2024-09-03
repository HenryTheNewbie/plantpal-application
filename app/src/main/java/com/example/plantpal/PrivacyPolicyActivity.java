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

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ImageView backButton;

    private TextView privacyPolicyTextView;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy_page);

        backButton = findViewById(R.id.privacy_policy_back_button);

        privacyPolicyTextView = findViewById(R.id.privacyPolicyText);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("privacy_policy.txt");

        fetchPrivacyPolicy();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrivacyPolicyActivity.this, AppSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchPrivacyPolicy() {
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String privacyPolicyText = new String(bytes);
                privacyPolicyTextView.setText(privacyPolicyText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("PrivacyPolicyActivity", "Failed to fetch privacy policy", e);
            }
        });
    }
}
