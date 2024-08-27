package com.example.plantpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView backButton;

    private ImageView editProfilePicture;
    private ImageView tintOverlay;
    private TextView changeProfilePictureText;

    private EditText editUsername;
    private EditText editBio;
    private EditText editNewPassword;
    private EditText editConfirmNewPassword;
    private Button saveChangesButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_page);

        backButton = findViewById(R.id.edit_profile_back_button);

        editProfilePicture = findViewById(R.id.edit_profile_picture);
        tintOverlay = findViewById(R.id.tint_overlay);
        changeProfilePictureText = findViewById(R.id.edit_text);

        editUsername = findViewById(R.id.edit_username);
        editBio = findViewById(R.id.edit_bio);
        editNewPassword = findViewById(R.id.edit_new_password);
        editConfirmNewPassword = findViewById(R.id.edit_confirm_new_password);
        saveChangesButton = findViewById(R.id.save_changes_button);

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", "User");

        tintOverlay.setBackgroundColor(Color.parseColor("#80000000"));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, MyGardenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        if (userEmail != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");

            usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            editUsername.setText(user.getUsername());
                            editBio.setText(user.getBio());

                            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePictureUrl()).into(editProfilePicture);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}