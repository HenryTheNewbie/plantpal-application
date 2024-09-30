package com.example.plantpal;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ForumDetailActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private ImageView backButton, forumAuthorAvatar;

    private TextView titleTextView, contextTextView, authorTextView, createdAtTextView;

    private LinearLayout forumImagesSection;
    private ImageView forumImage1, forumImage2, forumImage3;

    private RecyclerView repliesRecyclerView;
    private ReplyAdapter replyAdapter;
    private List<Reply> replyList;

    private EditText replyInput;
    private ImageView replyImageButton;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private HashMap<Uri, Integer> imagePositionsMap = new HashMap<>();

    private Uri selectedImageUri;
    private RelativeLayout replyPreviewImageSection;
    private RecyclerView replyPreviewImageRecyclerView;
    private ReplyPreviewImagesAdapter replyPreviewImagesAdapter;
    private int imageCount = 0;
    private ImageView submitReplyButton;

    private DatabaseReference databaseReference;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_detail_page);

        backButton = findViewById(R.id.forum_detail_back_button);
        forumAuthorAvatar = findViewById(R.id.forum_author_avatar);

        titleTextView = findViewById(R.id.forum_title_placeholder);
        contextTextView = findViewById(R.id.forum_context);
        authorTextView = findViewById(R.id.forum_author_username);
        createdAtTextView = findViewById(R.id.forum_date_placeholder);

        forumImagesSection = findViewById(R.id.forum_images_section);
        forumImage1 = findViewById(R.id.forum_image_1);
        forumImage2 = findViewById(R.id.forum_image_2);
        forumImage3 = findViewById(R.id.forum_image_3);

        repliesRecyclerView = findViewById(R.id.replies_recycler_view);
        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        replyList = new ArrayList<>();
        replyAdapter = new ReplyAdapter(replyList, this);
        repliesRecyclerView.setAdapter(replyAdapter);

        replyInput = findViewById(R.id.reply_input_field);

        replyImageButton = findViewById(R.id.reply_image_button);

        replyPreviewImageSection = findViewById(R.id.reply_preview_image_section);

        replyPreviewImageRecyclerView = findViewById(R.id.reply_preview_image_recycler_view);
        replyPreviewImagesAdapter = new ReplyPreviewImagesAdapter(this, selectedImageUris, imagePositionsMap, this::removeImage);

        replyPreviewImageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        replyPreviewImageRecyclerView.setAdapter(replyPreviewImagesAdapter);

        submitReplyButton = findViewById(R.id.reply_submit_button);

        sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (getIntent() != null) {
            String title = getIntent().getStringExtra("title");
            String context = getIntent().getStringExtra("context");
            String author = getIntent().getStringExtra("author");
            String createdAt = getIntent().getStringExtra("createdAt");

            String forumImageUrl1 = null;
            String forumImageUrl2 = null;
            String forumImageUrl3 = null;

            ArrayList<String> forumImageUrls = getIntent().getStringArrayListExtra("forumImageUrls");

            if (forumImageUrls != null && !forumImageUrls.isEmpty()) {
                forumImageUrl1 = forumImageUrls.get(0);
                forumImageUrl2 = forumImageUrls.size() > 1 ? forumImageUrls.get(1) : null;
                forumImageUrl3 = forumImageUrls.size() > 2 ? forumImageUrls.get(2) : null;
            }

            if (forumImageUrl1 != null && !forumImageUrl1.isEmpty()) {
                Picasso.get().load(forumImageUrl1).into(forumImage1);
                forumImagesSection.setVisibility(View.VISIBLE);
                forumImage1.setVisibility(View.VISIBLE);
            } else {
                forumImagesSection.setVisibility(View.GONE);
                forumImage1.setVisibility(View.GONE);
            }

            if (forumImageUrl2 != null && !forumImageUrl2.isEmpty()) {
                Picasso.get().load(forumImageUrl2).into(forumImage2);
                forumImage2.setVisibility(View.VISIBLE);
            } else {
                forumImage2.setVisibility(View.GONE);
            }

            if (forumImageUrl3 != null && !forumImageUrl3.isEmpty()) {
                Picasso.get().load(forumImageUrl3).into(forumImage3);
                forumImage3.setVisibility(View.VISIBLE);
            } else {
                forumImage3.setVisibility(View.GONE);
            }

            titleTextView.setText(title);
            contextTextView.setText(context);
            authorTextView.setText(author);
            createdAtTextView.setText("Created on: " + createdAt);

            loadAuthorAvatar(author);
            loadReplies();
        }

        submitReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postReply();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForumDetailActivity.this, CommunityForumActivity.class);
                startActivity(intent);
            }
        });

        replyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGalleryPermission();
            }
        });
    }

    private void loadAuthorAvatar(String authorUsername) {
        databaseReference.child("users").child(authorUsername).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profilePictureUrl = dataSnapshot.getValue(String.class);
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Picasso.get()
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(forumAuthorAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load author avatar: " + databaseError.getMessage());
            }
        });
    }

    private void loadReplies() {
        String forumId = getIntent().getStringExtra("forumId");
        if (forumId == null) {
            Log.e("ForumDetailActivity", "Forum ID is null.");
            return;
        }

        databaseReference.child("forums").child(forumId).child("replies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot repliesSnapshot) {
                if (repliesSnapshot.exists()) {
                    for (DataSnapshot replySnapshot : repliesSnapshot.getChildren()) {
                        String replyAuthor = replySnapshot.child("author").getValue(String.class);
                        String replyContent = replySnapshot.child("content").getValue(String.class);
                        String replyCreatedAt = replySnapshot.child("createdAt").getValue(String.class);

                        List<String> replyImageUrls = new ArrayList<>();
                        DataSnapshot imageUrlsSnapshot = replySnapshot.child("replyImageUrls");
                        if (imageUrlsSnapshot.exists()) {
                            for (DataSnapshot imageUrlSnapshot : imageUrlsSnapshot.getChildren()) {
                                String imageUrl = imageUrlSnapshot.getValue(String.class);
                                if (imageUrl != null) {
                                    replyImageUrls.add(imageUrl);
                                }
                            }
                        }

                        Log.d("Replies", "replyImageUrls: " + replyImageUrls);

                        if (replyAuthor != null && replyContent != null && replyCreatedAt != null && replyImageUrls != null) {
                            Reply reply = new Reply(replyAuthor, replyContent, replyCreatedAt, replyImageUrls);
                            replyList.add(reply);
                        }
                    }

                    replyAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ForumDetailActivity", "No replies found for forum ID: " + forumId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load replies: " + databaseError.getMessage());
            }
        });
    }

    private void postReply() {
        String author = sharedPreferences.getString("username", "User");
        String content = replyInput.getText().toString();
        String createdAt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

        if (content.trim().isEmpty()) {
            Toast.makeText(this, "Reply cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        Reply reply = new Reply(author, content, createdAt, uploadedImageUrls);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String forumId = getIntent().getStringExtra("forumId");
        DatabaseReference repliesRef = databaseReference.child("forums").child(forumId).child("replies");
        DatabaseReference repliesCountRef = databaseReference.child("forums").child(forumId).child("repliesCount");

        repliesCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(currentCount + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                if (databaseError != null) {
                    Toast.makeText(ForumDetailActivity.this, "Failed to update reply count.", Toast.LENGTH_SHORT).show();
                } else {
                    Integer newCount = currentData.getValue(Integer.class);
                    String replyKey = "reply" + newCount;

                    if (!selectedImageUris.isEmpty()) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                                .child("reply_images")
                                .child(forumId)
                                .child(replyKey);

                        uploadImages(storageRef, uploadedImageUrls, reply, repliesRef, replyKey);
                    } else {
                        saveReplyToDatabase(repliesRef, reply, replyKey);
                    }
                }
            }
        });
    }

    private void uploadImages(StorageReference storageRef, List<String> uploadedImageUrls, Reply reply, DatabaseReference repliesRef, String replyKey) {
        if (selectedImageUris.isEmpty()) {
            saveReplyToDatabase(repliesRef, reply, replyKey);
            return;
        }

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            StorageReference imageRef = storageRef.child("replyImage" + (i + 1) + ".png");

            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrls.add(uri.toString());

                    if (uploadedImageUrls.size() == imageCount) {
                        reply.setReplyImageUrls(uploadedImageUrls);
                        saveReplyToDatabase(repliesRef, reply, replyKey);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(ForumDetailActivity.this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(ForumDetailActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveReplyToDatabase(DatabaseReference repliesRef, Reply reply, String replyKey) {
        repliesRef.child(replyKey).setValue(reply).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DatabaseReference replyImagesRef = repliesRef.child(replyKey).child("replyImageUrls");
                for (int i = 0; i < reply.getReplyImageUrls().size(); i++) {
                    replyImagesRef.child(String.valueOf(i)).setValue(reply.getReplyImageUrls().get(i));
                }

                replyInput.setText("");
                selectedImageUris.clear();
                imagePositionsMap.clear();
                imageCount = 0;
                replyPreviewImagesAdapter.notifyDataSetChanged();
                updateAddImageButtonAvailability();
                updatePreviewImageSectionVisibility();

                Toast.makeText(ForumDetailActivity.this, "Reply posted successfully.", Toast.LENGTH_SHORT).show();
                updateReplies();
            } else {
                Toast.makeText(ForumDetailActivity.this, "Failed to post reply. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReplies() {
        replyList.clear();
        loadReplies();
    }

    public void loadRepliesAuthorAvatar(String replyAuthorUsername, ImageView replyAuthorAvatar) {
        databaseReference.child("users").child(replyAuthorUsername).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profilePictureUrl = dataSnapshot.getValue(String.class);
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Picasso.get()
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(replyAuthorAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load author avatar: " + databaseError.getMessage());
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

                    replyPreviewImagesAdapter.notifyDataSetChanged();

                }

                Log.d("ImagePicker", "Image count after addition: " + imageCount);

                updateAddImageButtonAvailability();
                updatePreviewImageSectionVisibility();
            } else {
                Log.e("ImagePicker", "Selected image URI is null");
                Toast.makeText(this, "Failed to retrieve selected image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("ImagePicker", "Result not OK, requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }

    private void updateAddImageButtonAvailability() {
        if (imageCount >= 3) {
            replyImageButton.setEnabled(false);
            replyImageButton.setAlpha(0.5f);
        } else {
            replyImageButton.setEnabled(true);
            replyImageButton.setAlpha(1.0f);
        }
    }

    private void updatePreviewImageSectionVisibility() {
        if (selectedImageUris.size() > 0) {
            replyPreviewImageSection.setVisibility(View.VISIBLE);
        } else {
            replyPreviewImageSection.setVisibility(View.GONE);
        }
    }

    private void removeImage(int position) {
        if (position >= 0 && position < selectedImageUris.size()) {
            selectedImageUris.remove(position);

            imagePositionsMap.remove(position);

            Log.d("ImagePicker", "Image count: " + imageCount);
            Log.d("ImagePicker", "Image removed at position: " + position);

            replyPreviewImagesAdapter.notifyItemRemoved(position);

            if (position < selectedImageUris.size()) {
                replyPreviewImagesAdapter.notifyItemRangeChanged(position, selectedImageUris.size());
            }

            imageCount = selectedImageUris.size();

            Log.d("ImagePicker", "Image count after removal: " + imageCount);

            updateAddImageButtonAvailability();
            updatePreviewImageSectionVisibility();
        } else {
            Log.e("ImagePicker", "Attempted to remove image at invalid position: " + position);
        }
    }
}
