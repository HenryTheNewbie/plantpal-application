package com.example.plantpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;

public class AppSettingsActivity extends AppCompatActivity {

    private ImageView backButton;

    private SwitchMaterial notificationSwitch;

    private Button faqButton;
    private Button privacyPolicyButton;
    private Button termsOfUseButton;
    private Button aboutTheAppButton;

    private Button logoutButton;

    private SharedPreferences settingsPreferences;
    private SharedPreferences loginPreferences;
    private static final String SETTINGS_PREFS_NAME = "settings";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notifications_enabled";
    private static final String LOGIN_PREFS_NAME = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_settings_page);

        backButton = findViewById(R.id.settings_back_button);

        notificationSwitch = findViewById(R.id.notifications_switch);

        faqButton = findViewById(R.id.button_faq);
        privacyPolicyButton = findViewById(R.id.button_privacy_policy);
        termsOfUseButton = findViewById(R.id.button_terms_of_use);
        aboutTheAppButton = findViewById(R.id.button_about_app);

        logoutButton = findViewById(R.id.button_log_out);

        settingsPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        loginPreferences = getSharedPreferences(LOGIN_PREFS_NAME, MODE_PRIVATE);

        boolean notificationsEnabled = settingsPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
        notificationSwitch.setChecked(notificationsEnabled);

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putBoolean(NOTIFICATIONS_ENABLED_KEY, isChecked);
                editor.apply();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSettingsActivity.this, MyGardenActivity.class);
                startActivity(intent);
            }
        });

        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSettingsActivity.this, FAQActivity.class);
                startActivity(intent);
            }
        });

        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSettingsActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        termsOfUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSettingsActivity.this, TermsOfUseActivity.class);
                startActivity(intent);
            }
        });

        aboutTheAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSettingsActivity.this, AboutTheAppActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                loginPreferences.edit()
                        .remove("rememberMe")
                        .remove("username")
                        .remove("email")
                        .apply();

                settingsPreferences.edit()
                        .remove(NOTIFICATIONS_ENABLED_KEY)
                        .apply();

                Intent intent = new Intent(AppSettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
