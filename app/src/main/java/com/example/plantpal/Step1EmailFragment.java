package com.example.plantpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class Step1EmailFragment extends Fragment {

    private ImageView backArrow;
    private EditText emailEditText;
    private Button sendEmailButton;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.step1_email_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backArrow = view.findViewById(R.id.back_arrow);
        emailEditText = view.findViewById(R.id.email);
        sendEmailButton = view.findViewById(R.id.send_email_button);

        backArrow.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        sendEmailButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            checkEmailExistsAndSendOTP(email);
        });
    }

    private void checkEmailExistsAndSendOTP(String email) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    sendOTP(email);
                } else {
                    Toast.makeText(getContext(), "The provided email does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOTP(String email) {
        String otp = generateOTP();
        String subject = "PlantPal Password Reset OTP";
        String message = "Your OTP is " + otp + ". Please enter this OTP to reset your password.";

        JavaMailAPI javaMailAPI = new JavaMailAPI(email, subject, message);
        javaMailAPI.execute();

        ((ForgotPasswordActivity) requireActivity()).showStep2VerificationFragment(otp, email);
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
}