package com.example.plantpal;

public class Plant {
    private String commonName;
    private String scientificName;
    private String family;
    private String description;
    private String imageURL;
    private String sunlight;
    private String water;
    private String soil;
    private String fertilizer;
    private String temperature;
    private String humidity;
    private String extraCareInfo;
    private String category;
    private String herbalProperties;

    public Plant() {
    }

    public Plant(String commonName, String scientificName, String family, String description, String imageURL,
                 String sunlight, String water, String soil, String fertilizer, String temperature, String humidity,
                 String extraCareInfo, String category, String herbalProperties) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.family = family;
        this.description = description;
        this.imageURL = imageURL;
        this.sunlight = sunlight;
        this.water = water;
        this.soil = soil;
        this.fertilizer = fertilizer;
        this.temperature = temperature;
        this.humidity = humidity;
        this.extraCareInfo = extraCareInfo;
        this.category = category;
        this.herbalProperties = herbalProperties;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSunlight() {
        return sunlight;
    }

    public void setSunlight(String sunlight) {
        this.sunlight = sunlight;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public String getFertilizer() {
        return fertilizer;
    }

    public void setFertilizer(String fertilizer) {
        this.fertilizer = fertilizer;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getExtraCareInfo() {
        return extraCareInfo;
    }

    public void setExtraCareInfo(String extraCareInfo) {
        this.extraCareInfo = extraCareInfo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHerbalProperties() {
        return herbalProperties;
    }

    public void setHerbalProperties(String herbalProperties) {
        this.herbalProperties = herbalProperties;
    }
}
