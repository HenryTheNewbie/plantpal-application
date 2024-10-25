package com.example.plantpal;

import java.util.List;

public class PlantDisease {
    private String commonName;
    private String scientificName;
    private String description;
    private List<String> visibleSymptoms;
    private String possibleCauses;
    private String prevention;
    private String treatmentSuggestion;

    public PlantDisease() {}

    public PlantDisease(String commonName, String scientificName, String description,
                        List<String> visibleSymptoms, String possibleCauses,
                        String prevention, String treatmentSuggestion) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.description = description;
        this.visibleSymptoms = visibleSymptoms;
        this.possibleCauses = possibleCauses;
        this.prevention = prevention;
        this.treatmentSuggestion = treatmentSuggestion;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getVisibleSymptoms() {
        return visibleSymptoms;
    }

    public String getPossibleCauses() {
        return possibleCauses;
    }

    public String getPrevention() {
        return prevention;
    }

    public String getTreatmentSuggestion() {
        return treatmentSuggestion;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVisibleSymptoms(List<String> visibleSymptoms) {
        this.visibleSymptoms = visibleSymptoms;
    }

    public void setPossibleCauses(String possibleCauses) {
        this.possibleCauses = possibleCauses;
    }

    public void setPrevention(String prevention) {
        this.prevention = prevention;
    }

    public void setTreatmentSuggestion(String treatmentSuggestion) {
        this.treatmentSuggestion = treatmentSuggestion;
    }
}
