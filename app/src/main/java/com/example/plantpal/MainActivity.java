package com.example.plantpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmailEditText;
    private EditText loginPasswordEditText;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginEmailEditText = findViewById(R.id.email);
        loginPasswordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        rememberMeCheckBox = findViewById(R.id.remember_me);
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("rememberMe", false)) {
            openLandingPage(null);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");

                String email = loginEmailEditText.getText().toString().toLowerCase();
                String password = loginPasswordEditText.getText().toString();

                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean userFound = false;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            assert user != null;
                            if (user.getPassword().equals(password)) {
                                userFound = true;

                                sharedPreferences.edit()
                                        .putBoolean("rememberMe", rememberMeCheckBox.isChecked())
                                        .putString("username", user.getUsername())
                                        .apply();

                                openLandingPage(null);
                                break;
                            }
                        }
                        if (!userFound) {
                            Toast.makeText(MainActivity.this, "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void openRegisterPage(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void openLandingPage(View view) {
        Intent intent = new Intent(this, LandingPageActivity.class);
        startActivity(intent);
    }

    public void openForgotPasswordPage(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}