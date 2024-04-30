package com.example.plantpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");

                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString().toLowerCase();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(RegisterActivity.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
                        } else {
                            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Toast.makeText(RegisterActivity.this, "Email is already taken.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (password.length() < 8) {
                                            Toast.makeText(RegisterActivity.this, "Password should be at least 8 characters.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (!password.equals(confirmPassword)) {
                                                Toast.makeText(RegisterActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            User user = new User(username, email, password);
                                            usersRef.child(username).setValue(user);
                                            Toast.makeText(RegisterActivity.this, "User registered successfully.", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(RegisterActivity.this, LandingPageActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(RegisterActivity.this, "Failed to check if email is taken.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "Failed to check if username is taken.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void openLoginPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}