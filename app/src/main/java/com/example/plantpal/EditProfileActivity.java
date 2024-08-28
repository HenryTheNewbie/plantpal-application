package com.example.plantpal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
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

    private SharedPreferences sharedPreferences;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

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
        if (isFinishing() || isDestroyed() || isDialogShowing) {
            return;
        }

        isDialogShowing = true;

        CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Your Profile Picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    openCamera();
                } else if (options[item].equals("Choose from Gallery")) {
                    openGallery();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.setOnDismissListener(dialog -> isDialogShowing = false);
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                editProfilePicture.setImageURI(selectedImageUri);

                StorageReference imageRef = storageRef.child("profile_pictures/" + selectedImageUri.getLastPathSegment());

                UploadTask uploadTask = imageRef.putFile(selectedImageUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference usersRef = database.getReference("users");

                                String userEmail = sharedPreferences.getString("email", "User");

                                usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                            String userId = userSnapshot.getKey();
                                            usersRef.child(userId).child("profilePictureUrl").setValue(imageUrl);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(EditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });

            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                editProfilePicture.setImageBitmap(imageBitmap);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                StorageReference imageRef = storageRef.child("profile_pictures/" + System.currentTimeMillis() + ".png");

                UploadTask uploadTask = imageRef.putBytes(imageData);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference usersRef = database.getReference("users");

                                String userEmail = sharedPreferences.getString("email", "User");

                                usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                            String userId = userSnapshot.getKey();
                                            usersRef.child(userId).child("profilePictureUrl").setValue(imageUrl);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(EditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
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