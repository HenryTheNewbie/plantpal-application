package com.example.plantpal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            showEmailFragment();
        }
    }

    public void showEmailFragment() {
        Fragment emailFragment = new EmailFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, emailFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}