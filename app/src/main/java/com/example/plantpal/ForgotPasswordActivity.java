package com.example.plantpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            showStep1EmailFragment();
        }
    }

    private void showStep1EmailFragment() {
        Fragment step1EmailFragment = new Step1EmailFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, step1EmailFragment)
                .commit();
    }

    public void showStep2VerificationFragment(String otp, String email) {
        Fragment step2VerificationFragment = new Step2VerificationFragment();

        Bundle args = new Bundle();
        args.putString("currentOtpCode", otp);
        args.putString("email", email);
        step2VerificationFragment.setArguments(args);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, step2VerificationFragment)
                .addToBackStack(null)
                .commit();
    }

    public void showStep3ResetPasswordFragment(String email) {
        Fragment step3ResetPasswordFragment = new Step3ResetPasswordFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        step3ResetPasswordFragment.setArguments(args);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, step3ResetPasswordFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}