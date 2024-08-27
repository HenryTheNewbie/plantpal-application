package com.example.plantpal;

public class User {
    private String username;
    private String email;
    private String password;
    private String bio;
    private String profilePictureUrl;

    public User() {}

    public User(String username, String email, String password, String bio, String profilePictureUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBio(String bio) { this.bio = bio; }

    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}