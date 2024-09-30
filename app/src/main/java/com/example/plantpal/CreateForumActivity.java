package com.example.plantpal;

import android.Manifest;
import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateForumActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private ImageView backButton;

    private EditText titleEditText;
    private EditText contextEditText;
    private Button createForumButton;
    private View addImageButton;

    private Uri selectedImageUri;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private HashMap<Uri, Integer> imagePositionsMap = new HashMap<>();

    private RecyclerView previewImagesRecyclerView;
    private ForumPreviewImagesAdapter forumPreviewImagesAdapter;

    private int imageCount = 0;

    private SharedPreferences sharedPreferences;

    public interface ImageUploadCallback {
        void onImagesUploaded(List<String> imageUrls);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_forum_page);

        backButton = findViewById(R.id.create_forum_back_button);

        titleEditText = findViewById(R.id.forum_title_edit_text);
        contextEditText = findViewById(R.id.forum_context_edit_text);
        createForumButton = findViewById(R.id.create_forum_button);
        addImageButton = findViewById(R.id.add_image_button);

        previewImagesRecyclerView = findViewById(R.id.forum_image_preview_recycler_view);
        forumPreviewImagesAdapter = new ForumPreviewImagesAdapter(this, selectedImageUris, imagePositionsMap, this::removeImage);

        previewImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        previewImagesRecyclerView.setAdapter(forumPreviewImagesAdapter);

        sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateForumActivity.this, CommunityForumActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGalleryPermission();
            }
        });

        createForumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference forumsRef = database.getReference("forums");
                DatabaseReference forumCounterRef = database.getReference("counters").child("forumCount");

                String title = titleEditText.getText().toString();
                String context = contextEditText.getText().toString();
                String username = sharedPreferences.getString("username", "");

                if (title.isEmpty()) {
                    Toast.makeText(CreateForumActivity.this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (context.isEmpty()) {
                    Toast.makeText(CreateForumActivity.this, "Context cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

                forumCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long forumCount = 0;
                        if (dataSnapshot.exists()) {
                            forumCount = (long) dataSnapshot.getValue();
                        }

                        long newForumId = forumCount + 1;

                        String forumId = "forum" + newForumId;

                        uploadImagesToFirebase(forumId, new ImageUploadCallback() {
                            @Override
                            public void onImagesUploaded(List<String> imageUrls) {
                                List<String> forumImageUrls = new ArrayList<>(imageUrls);
                                Map<String, String> replies = new HashMap<>();

                                Forum forum = new Forum(forumId, title, context, username, currentDate, replies, 0, forumImageUrls);

                                forumsRef.child(forumId).setValue(forum).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        forumCounterRef.setValue(newForumId);

                                        Toast.makeText(CreateForumActivity.this, "Forum created successfully.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(CreateForumActivity.this, CommunityForumActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(CreateForumActivity.this, "Failed to create forum.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(CreateForumActivity.this, "Failed to create forum.", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CreateForumActivity.this, "Failed to create forum.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Log.e("Permissions", "Storage permission denied.");
                    Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show();
                }
            });

    private void openGallery() {
        Log.d("ImagePicker", "openGallery called");

        openGalleryIntent();
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

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Log.d("ImagePicker", "Image selected successfully");

                Log.d("ImagePicker", "Image count: " + imageCount);

                if (!selectedImageUris.contains(selectedImageUri)) {
                    selectedImageUris.add(selectedImageUri);
                    imagePositionsMap.put(selectedImageUri, imageCount);
                    imageCount++;

                    forumPreviewImagesAdapter.notifyDataSetChanged();
                }

                Log.d("ImagePicker", "Image count after addition: " + imageCount);

                updateAddImageButtonVisibility();
                updateRecyclerViewVisibility();
            } else {
                Log.e("ImagePicker", "Selected image URI is null");
                Toast.makeText(this, "Failed to retrieve selected image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("ImagePicker", "Result not OK, requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }

    private void updateAddImageButtonVisibility() {
        if (imageCount >= 3) {
            addImageButton.setVisibility(View.GONE);
        } else {
            addImageButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateRecyclerViewVisibility() {
        if (selectedImageUris.size() > 0) {
            previewImagesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            previewImagesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void uploadImagesToFirebase(String forumId, ImageUploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("forum_images").child(forumId);

        List<Uri> imagesUris = selectedImageUris;
        List<String> uploadedImagesUrls = new ArrayList<>();
        int[] uploadCount = {0};
        int totalImages = imagesUris.size();

        if (totalImages == 0) {
            callback.onImagesUploaded(uploadedImagesUrls);
            return;
        }

        for (int i = 0; i < totalImages; i++) {
            final int index = i;
            Uri imageUri = imagesUris.get(i);
            if (imageUri != null) {
                StorageReference imageRef = storageRef.child("forumImage" + (index + 1) + ".png");
                UploadTask uploadTask = imageRef.putFile(imageUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d("Firebase", "Image " + (index + 1) + " uploaded successfully");
                        uploadedImagesUrls.add(downloadUri.toString());
                        uploadCount[0]++;

                        if (uploadCount[0] == totalImages) {
                            callback.onImagesUploaded(uploadedImagesUrls);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to get download URL for image " + (index + 1) + ": " + e.getMessage());
                        uploadCount[0]++;
                        if (uploadCount[0] == totalImages) {
                            callback.onImagesUploaded(uploadedImagesUrls);
                        }
                    });
                }).addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to upload image " + (index + 1) + ": " + e.getMessage());
                    uploadCount[0]++;
                    if (uploadCount[0] == totalImages) {
                        callback.onImagesUploaded(uploadedImagesUrls);
                    }
                });
            }
        }
    }

    private void removeImage(int position) {
        if (position >= 0 && position < selectedImageUris.size()) {
            selectedImageUris.remove(position);

            imagePositionsMap.remove(position);

            Log.d("ImagePicker", "Image count: " + imageCount);
            Log.d("ImagePicker", "Image removed at position: " + position);

            forumPreviewImagesAdapter.notifyItemRemoved(position);

            if (position < selectedImageUris.size()) {
                forumPreviewImagesAdapter.notifyItemRangeChanged(position, selectedImageUris.size());
            }

            imageCount = selectedImageUris.size();

            Log.d("ImagePicker", "Image count after removal: " + imageCount);

            updateAddImageButtonVisibility();
            updateRecyclerViewVisibility();
        } else {
            Log.e("ImagePicker", "Attempted to remove image at invalid position: " + position);
        }
    }
}