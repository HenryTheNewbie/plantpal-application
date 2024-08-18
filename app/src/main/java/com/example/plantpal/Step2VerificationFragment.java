package com.example.plantpal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;

import java.util.Random;

import javax.annotation.Nullable;

public class Step2VerificationFragment extends Fragment {

    private String currentOtpCode;
    private String email;

    private ImageView backArrow;
    private PinView otpPinView;
    private TextView resendOtpText;
    private Button verifyButton;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeRemaining = 60000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.step2_verification_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backArrow = view.findViewById(R.id.back_arrow);
        otpPinView = view.findViewById(R.id.otp_pin_view);
        resendOtpText = view.findViewById(R.id.resend_code_text);
        verifyButton = view.findViewById(R.id.verify_button);

        if (getArguments() != null) {
            currentOtpCode = getArguments().getString("currentOtpCode");
            email = getArguments().getString("email");
        }

        startTimer();

        backArrow.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        resendOtpText.setOnClickListener(v -> {
            if (isTimerRunning) {
                Toast.makeText(getContext(), "Try again in " + timeRemaining / 1000 + " seconds.", Toast.LENGTH_SHORT).show();
            } else {
                resendOTP();
                Toast.makeText(getContext(), "OTP resent successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        verifyButton.setOnClickListener(v -> {
            String enteredOtp = otpPinView.getText().toString().trim();
            if (enteredOtp.isEmpty()) {
                Toast.makeText(getContext(), "Please enter the OTP.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredOtp.equals(currentOtpCode)) {
                ((ForgotPasswordActivity) requireActivity()).showStep3ResetPasswordFragment(email);
            } else {
                Toast.makeText(getContext(), "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
            }
        }.start();
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    private void resendOTP() {
        currentOtpCode = generateOTP();
        String subject = "PlantPal Password Reset OTP";
        String message = "Your OTP is " + currentOtpCode + ". Please enter this OTP to reset your password.";

        JavaMailAPI javaMailAPI = new JavaMailAPI(email, subject, message);
        javaMailAPI.execute();

        startTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isTimerRunning) {
            countDownTimer.cancel();
        }
    }
}
