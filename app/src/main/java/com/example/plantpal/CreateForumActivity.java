package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateForumActivity extends AppCompatActivity {

    private ImageView backButton;

    private EditText titleEditText;
    private EditText contextEditText;
    private Button createForumButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_forum_page);

        backButton = findViewById(R.id.create_forum_back_button);

        titleEditText = findViewById(R.id.forum_title_edit_text);
        contextEditText = findViewById(R.id.forum_context_edit_text);
        createForumButton = findViewById(R.id.create_forum_button);

        sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateForumActivity.this, CommunityForumActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        createForumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference forumsRef = database.getReference("forums");
                DatabaseReference forumCounterRef = database.getReference("counters").child("forumCount");

                String title = titleEditText.getText().toString();
                String context = contextEditText.getText().toString();
                String username = sharedPreferences.getString("username", "");

                if (title.isEmpty()) {
                    Toast.makeText(CreateForumActivity.this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (context.isEmpty()) {
                    Toast.makeText(CreateForumActivity.this, "Context cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

                forumCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long forumCount = 0;
                        if (dataSnapshot.exists()) {
                            forumCount = (long) dataSnapshot.getValue();
                        }

                        long newForumId = forumCount + 1;

                        String forumId = "forum" + newForumId;
                        Map<String, String> replies = new HashMap<>();
                        Forum forum = new Forum(forumId, title, context, username, currentDate, replies, 0);

                        forumsRef.child(forumId).setValue(forum);

                        forumCounterRef.setValue(newForumId);

                        Toast.makeText(CreateForumActivity.this, "Forum created successfully.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(CreateForumActivity.this, CommunityForumActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CreateForumActivity.this, "Failed to create forum.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}