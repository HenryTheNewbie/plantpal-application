package com.example.plantpal;

import android.graphics.Bitmap;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.plantpal.ml.Mobilenetv2PlantIdentificationModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.image.TensorImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

public class PlantClassifier {

    private Mobilenetv2PlantIdentificationModel model;
    private static final int IMAGE_SIZE = 224;
    private static final float CONFIDENCE_THRESHOLD = 0.05f;

    private List<String> labels;
    private Map<String, String> classNameToCommonNameMap;

    public PlantClassifier(Context context) throws IOException {
        this.model = Mobilenetv2PlantIdentificationModel.newInstance(context);
    }

    /*
    private void loadLabelsFromFirebase(LabelsLoadedCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("labels.txt");

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String fileContent = new String(bytes, StandardCharsets.UTF_8);
            parseLabelsFile(fileContent);
            if (callback != null) {
                callback.onLabelsLoaded();
            }
        }).addOnFailureListener(exception -> {
            Log.e("PlantClassifier", "Failed to load labels: " + exception.getMessage());
        });
    }

    public interface LabelsLoadedCallback {
        void onLabelsLoaded();
    }

    private void parseLabelsFile(String fileContent) {
        classNameToCommonNameMap = new HashMap<>();
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String className = parts[0].trim();
                String commonName = parts[1].trim();
                classNameToCommonNameMap.put(className, commonName);
            }
        }
        labels = new ArrayList<>(classNameToCommonNameMap.keySet());
    }
    */

    public TensorImage preprocessImage(Bitmap bitmap) {
        TensorImage tensorImage = TensorImage.fromBitmap(bitmap);
        Log.d("TFLiteModel", "Preprocessed image for inference");
        return tensorImage;
    }

    public List<Pair<String, Float>> classifyImage(Bitmap bitmap) {
        TensorImage inputImage = TensorImage.fromBitmap(bitmap);

        List<Pair<String, Float>> allSpeciesWithConfidence = new ArrayList<>();
        try {
            Mobilenetv2PlantIdentificationModel.Outputs outputs = model.process(inputImage);
            List<Category> plantProbabilities = outputs.getPlantProbabilityAsCategoryList();

            Log.d("PlantClassifier", "Plant probabilities: " + plantProbabilities);

            for (Category category : plantProbabilities) {
                float confidence = category.getScore();
                String[] labelParts = category.getLabel().split("=");

                if (labelParts.length == 2) {
                    String className = labelParts[0].trim();
                    String commonName = labelParts[1].trim();

                    if (confidence > CONFIDENCE_THRESHOLD) {
                        Log.d("PlantClassifier", "Confidence for " + commonName + ": " + confidence);
                        allSpeciesWithConfidence.add(new Pair<>(commonName, confidence));
                    } else {
                        Log.d("PlantClassifier", "Low confidence for " + className + ": " + confidence);
                    }
                } else {
                    Log.w("PlantClassifier", "Unexpected label format: " + category.getLabel());
                }
            }

            Collections.sort(allSpeciesWithConfidence, new Comparator<Pair<String, Float>>() {
                @Override
                public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                    return o2.second.compareTo(o1.second);
                }
            });

        } finally {
            model.close();
        }

        return allSpeciesWithConfidence;
    }

    public List<Pair<String, Float>> runInferenceAndGetAll(Bitmap bitmap) {
        return classifyImage(bitmap);
    }

    public void close() {
        if (model != null) {
            model.close();
            model = null;
        }
    }
}