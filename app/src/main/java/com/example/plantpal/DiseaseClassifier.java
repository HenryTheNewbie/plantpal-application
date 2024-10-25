package com.example.plantpal;

import android.graphics.Bitmap;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.plantpal.ml.Mobilenetv2DiseaseDetectionModel;
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

public class DiseaseClassifier {

    private Mobilenetv2DiseaseDetectionModel model;
    private static final int IMAGE_SIZE = 224;
    private static final float CONFIDENCE_THRESHOLD = 0.05f;

    private List<String> labels;
    private Map<String, String> classNameToCommonNameMap;

    public DiseaseClassifier(Context context) throws IOException {
        this.model = Mobilenetv2DiseaseDetectionModel.newInstance(context);
    }

    /*
    private void loadLabelsFromFirebase(LabelsLoadedCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("disease_labels.txt");

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String fileContent = new String(bytes, StandardCharsets.UTF_8);
            parseLabelsFile(fileContent);
            if (callback != null) {
                callback.onLabelsLoaded();
            }
        }).addOnFailureListener(exception -> {
            Log.e("DiseaseClassifier", "Failed to load labels: " + exception.getMessage());
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
        Log.d("TFLiteModel", "Preprocessed image for disease inference");
        return tensorImage;
    }

    public Pair<String, Float> classifyImage(Bitmap bitmap) {
        TensorImage inputImage = TensorImage.fromBitmap(bitmap);

        List<Pair<String, Float>> allDiseasesWithConfidence = new ArrayList<>();
        try {
            Mobilenetv2DiseaseDetectionModel.Outputs outputs = model.process(inputImage);
            List<Category> diseaseProbabilities = outputs.getPlantProbabilityAsCategoryList();

            for (Category category : diseaseProbabilities) {
                float confidence = category.getScore();
                String[] labelParts = category.getLabel().split("=");

                if (labelParts.length == 2) {
                    String className = labelParts[0].trim();
                    String commonName = labelParts[1].trim();

                    if (confidence > CONFIDENCE_THRESHOLD) {
                        allDiseasesWithConfidence.add(new Pair<>(commonName, confidence));
                    }
                } else {
                    Log.w("DiseaseClassifier", "Unexpected label format: " + category.getLabel());
                }
            }

            Collections.sort(allDiseasesWithConfidence, new Comparator<Pair<String, Float>>() {
                @Override
                public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                    return o2.second.compareTo(o1.second);
                }
            });

            if (!allDiseasesWithConfidence.isEmpty()) {
                return allDiseasesWithConfidence.get(0);
            }

        } finally {
            model.close();
        }

        return null;
    }

    public Pair<String, Float> runInferenceAndGetTop(Bitmap bitmap) {
        return classifyImage(bitmap);
    }

    public void close() {
        if (model != null) {
            model.close();
            model = null;
        }
    }
}