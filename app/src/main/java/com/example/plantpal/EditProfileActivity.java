package com.example.plantpal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_PICK = 102;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private boolean isDialogShowing = false;

    private ImageView backButton;
    private ImageView editProfilePicture;
    private ImageView tintOverlay;
    private TextView changeProfilePictureText;

    private EditText editUsername;
    private EditText editBio;
    private EditText editNewPassword;
    private EditText editConfirmNewPassword;
    private Button saveChangesButton;

    private String currentPhotoPath;

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

        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerOptions();
            }
        });

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editUsername.getText().toString();
                String newBio = editBio.getText().toString();
                String newPassword = editNewPassword.getText().toString();
                String confirmNewPassword = editConfirmNewPassword.getText().toString();

                if (newUsername.isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.isEmpty() && confirmNewPassword.isEmpty()) {
                    updateProfileInfo(newUsername, newBio);
                } else {
                    if (newPassword.length() < 8) {
                        Toast.makeText(EditProfileActivity.this, "Password should be at least 8 characters.", Toast.LENGTH_SHORT).show();
                    } else if (!newPassword.equals(confirmNewPassword)) {
                        Toast.makeText(EditProfileActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    } else {
                        promptForCurrentPassword(newUsername, newBio, newPassword);
                    }
                }
            }
        });
    }

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Cancel"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    Log.d("ImagePicker", "Take Photo selected");
                    checkCameraPermission();
                    break;
                case 1:
                    Log.d("ImagePicker", "Choose from Gallery selected");
                    checkGalleryPermission();
                    break;
            }
            dialog.dismiss();
        });
        builder.show();
        Log.d("ImagePicker", "ImagePicker dialog shown");
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.e("Permissions", "Camera permission denied.");
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Log.e("Permissions", "Storage permission denied.");
                Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Log.d("ImagePicker", "openCamera called");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.e("ImagePicker", "Error creating image file: " + e.getMessage());
                    Toast.makeText(this, "Error creating image.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.example.plantpal.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    Log.d("ImagePicker", "Camera intent started successfully");
                } else {
                    Log.e("ImagePicker", "Photo file is null");
                    Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ImagePicker", "No camera app found");
                Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".png", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Log.d("ImagePicker", "openGallery called");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openGalleryIntent();
        }
    }

    private void openGalleryIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Log.d("ImagePicker", "Starting gallery activity");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ImagePicker", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("ImagePicker", "Image captured successfully");

                Uri photoURI = Uri.parse(currentPhotoPath);
                if (photoURI != null) {
                    editProfilePicture.setImageURI(photoURI);
                    uploadImageToFirebase(photoURI);
                } else {
                    Log.e("ImagePicker", "Photo URI is null");
                    Toast.makeText(this, "Failed to retrieve captured image.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    Log.d("ImagePicker", "Image selected successfully");
                    editProfilePicture.setImageURI(selectedImageUri);
                    uploadImageToFirebase(selectedImageUri);
                } else {
                    Log.e("ImagePicker", "Selected image URI is null");
                    Toast.makeText(this, "Failed to retrieve selected image.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.d("ImagePicker", "Result not OK, requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://plantpal-application.appspot.com");

        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("profile_pictures/" + imageUri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Image uploaded successfully");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d("Firebase", "Download URL: " + imageUrl);
                    updateProfilePictureUrl(imageUrl);
                }).addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to retrieve download URL: " + e.getMessage());
                    Toast.makeText(this, "Failed to retrieve image URL.", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to upload image: " + e.getMessage());
                Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e("ImagePicker", "Image URI is null, cannot upload.");
            Toast.makeText(this, "Image URI is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfilePictureUrl(String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        String userEmail = sharedPreferences.getString("email", "User");

        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        Log.d("Firebase", "Updating profile picture URL for userId: " + userId);

                        usersRef.child(userId).child("profilePictureUrl").setValue(imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Profile picture URL updated successfully");
                                    Toast.makeText(EditProfileActivity.this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Failed to update profile picture URL: " + e.getMessage());
                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Log.e("Firebase", "User not found");
                    Toast.makeText(EditProfileActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
                Toast.makeText(EditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileInfo(String newUsername, String newBio) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        String userEmail = sharedPreferences.getString("email", "User");
        String currentUsername = sharedPreferences.getString("username", "User");

        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserId = null;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    currentUserId = userSnapshot.getKey();
                    break;
                }

                if (currentUserId != null) {
                    String finalCurrentUserId = currentUserId;
                    usersRef.orderByChild("username").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean usernameTakenByAnotherUser = false;
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                if (!userId.equals(finalCurrentUserId)) {
                                    usernameTakenByAnotherUser = true;
                                    break;
                                }
                            }

                            if (usernameTakenByAnotherUser) {
                                Toast.makeText(EditProfileActivity.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
                            } else {
                                usersRef.child(finalCurrentUserId).child("username").setValue(newUsername);
                                usersRef.child(finalCurrentUserId).child("bio").setValue(newBio);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", newUsername);
                                editor.apply();

                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(EditProfileActivity.this, "Failed to check username availability.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to retrieve current user information.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to retrieve current user information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptForCurrentPassword(String newUsername, String newBio, String newPassword) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        String userEmail = sharedPreferences.getString("email", "User");

        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserId = null;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    currentUserId = userSnapshot.getKey();
                    break;
                }

                if (currentUserId != null) {
                    String finalCurrentUserId = currentUserId;
                    usersRef.orderByChild("username").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isUsernameTaken = false;

                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                if (!userId.equals(finalCurrentUserId)) {
                                    isUsernameTaken = true;
                                    break;
                                }
                            }

                            if (isUsernameTaken) {
                                Toast.makeText(EditProfileActivity.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
                            } else {
                                LayoutInflater inflater = LayoutInflater.from(EditProfileActivity.this);
                                View dialogView = inflater.inflate(R.layout.dialog_current_password, null);

                                EditText currentPasswordEditText = dialogView.findViewById(R.id.enter_current_password);
                                Button changePasswordButton = dialogView.findViewById(R.id.dialog_save_changes_button);

                                AlertDialog dialog = new AlertDialog.Builder(EditProfileActivity.this)
                                        .setTitle(null)
                                        .setView(dialogView)
                                        .setNegativeButton("Cancel", null)
                                        .create();

                                changePasswordButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String currentPassword = currentPasswordEditText.getText().toString().trim();

                                        if (!currentPassword.isEmpty()) {
                                            verifyCurrentPassword(currentPassword, newUsername, newBio, newPassword);
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(EditProfileActivity.this, "Please enter your current password.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                dialog.getWindow().setGravity(Gravity.CENTER);
                                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(EditProfileActivity.this, "Failed to check username availability.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to retrieve current user information.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to retrieve current user information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyCurrentPassword(String currentPassword, String newUsername, String newBio, String newPassword) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        String userEmail = sharedPreferences.getString("email", "User");

        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String currentUserId = userSnapshot.getKey();

                        if (storedPassword != null && storedPassword.equals(currentPassword)) {
                            updateUsernameBioAndPassword(newUsername, newBio, newPassword);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to retrieve user information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUsernameBioAndPassword(String newUsername, String newBio, String newPassword) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        String userEmail = sharedPreferences.getString("email", "User");

        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    if (!newUsername.isEmpty()) {
                        usersRef.child(userId).child("username").setValue(newUsername);
                    }

                    if (!newBio.isEmpty()) {
                        usersRef.child(userId).child("bio").setValue(newBio);
                    }

                    if (!newPassword.isEmpty()) {
                        usersRef.child(userId).child("password").setValue(newPassword);
                    }

                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}