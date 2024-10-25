package com.example.plantpal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.plantpal.ml.Mobilenetv2PlantIdentificationModel;
import com.example.plantpal.ml.Mobilenetv2DiseaseDetectionModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;
    private static final int REQUEST_IMAGE_CAPTURE = 1003;
    private static final int REQUEST_IMAGE_PICK = 1004;

    private PlantClassifier plantClassifier;
    private DiseaseClassifier diseaseClassifier;

    int imageSize = 224;

    private String currentPhotoPath;
    private MaterialCardView cameraButton;
    private MaterialCardView galleryButton;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_page);

        try {
            plantClassifier = new PlantClassifier(this);
            Log.d("TFLite", "Model initialized successfully.");
        } catch (IOException e) {
            Log.e("TFLite", "Error initializing TFLite model: " + e.getMessage());
        }

        try {
            diseaseClassifier = new DiseaseClassifier(this);
            Log.d("TFLite", "Model initialized successfully.");
        } catch (IOException e) {
            Log.e("TFLite", "Error initializing TFLite model: " + e.getMessage());
        }

        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);

        bottomNavigationView = findViewById(R.id.navigation_bar);

        int selectedItemId = getIntent().getIntExtra("selected_item_id", -1);

        if (selectedItemId != -1) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        cameraButton.setOnClickListener(view -> checkCameraPermission());
        galleryButton.setOnClickListener(view -> checkGalleryPermission());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_plant_scan) {
                    return true;
                } else if (itemId == R.id.nav_plant_archive) {
                    Intent intent = new Intent(ScanActivity.this, PlantArchiveActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_community_hub) {
                    Intent intent = new Intent(ScanActivity.this, CommunityForumActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_my_garden) {
                    Intent intent = new Intent(ScanActivity.this, MyGardenActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Log.e("Permissions", "Camera permission denied.");
                    Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Log.e("Permissions", "Storage permission denied.");
                    Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show();
                }
            });

    private void openCamera() {
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
        Log.d("ImagePicker", "Image path: " + currentPhotoPath);
        return image;
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri imageUri = null;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    imageUri = Uri.fromFile(imgFile);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                imageUri = data.getData();
            }

            if (imageUri != null) {
                Bitmap bitmap = getBitmapFromUri(imageUri);
                runInference(bitmap, imageUri);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            Log.e("getBitmapFromUri", "Error loading image from URI: " + e.getMessage());
            return null;
        }
    }

    private void runInference(Bitmap bitmap, Uri photoUri) {
        if (plantClassifier != null && diseaseClassifier != null) {
            List<Pair<String, Float>> resultsWithConfidence = plantClassifier.runInferenceAndGetAll(bitmap);
            Pair<String, Float> diseaseResult = diseaseClassifier.runInferenceAndGetTop(bitmap);

            List<String> commonNameList = new ArrayList<>();
            List<String> confidenceList = new ArrayList<>();

            for (Pair<String, Float> result : resultsWithConfidence) {
                commonNameList.add(result.first);

                String confidence = String.format(Locale.getDefault(), "%.2f", result.second * 100);
                confidenceList.add(confidence);
            }

            String diseaseCommonName = diseaseResult.first;
            String diseaseConfidence = String.format(Locale.getDefault(), "%.2f", diseaseResult.second * 100);

            Log.d("InferenceResults", "Common names: " + commonNameList);
            Log.d("InferenceResults", "Confidence: " + confidenceList);
            Log.d("InferenceResults", "Image path: " + photoUri.toString());

            Log.d("InferenceResults", "Disease common name: " + diseaseCommonName);
            Log.d("InferenceResults", "Confidence: " + diseaseConfidence);

            Intent intent = new Intent(ScanActivity.this, ScanResultsActivity.class);

            intent.putStringArrayListExtra("commonNameList", (ArrayList<String>) commonNameList);
            intent.putStringArrayListExtra("confidenceList", (ArrayList<String>) confidenceList);
            intent.putExtra("imageUri", photoUri.toString());

            intent.putExtra("diseaseCommonName", diseaseCommonName);
            intent.putExtra("diseaseConfidence", diseaseConfidence);

            startActivity(intent);
        } else {
            Log.e("TFLite", "TFLite classifier is not initialized.");
            Toast.makeText(this, "Error running inference.", Toast.LENGTH_SHORT).show();
        }
    }
}