package com.example.plantpal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForumDetailActivity extends AppCompatActivity {

    private ImageView backButton, forumAuthorAvatar;

    private TextView titleTextView, contextTextView, authorTextView, createdAtTextView;

    private RecyclerView repliesRecyclerView;
    private ReplyAdapter replyAdapter;
    private List<Reply> replyList;

    private EditText replyInput;
    private ImageView submitReplyButton;

    private DatabaseReference databaseReference;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_detail_page);

        backButton = findViewById(R.id.forum_detail_back_button);
        forumAuthorAvatar = findViewById(R.id.forum_author_avatar);

        titleTextView = findViewById(R.id.forum_title_placeholder);
        contextTextView = findViewById(R.id.forum_context);
        authorTextView = findViewById(R.id.forum_author_username);
        createdAtTextView = findViewById(R.id.forum_date_placeholder);

        repliesRecyclerView = findViewById(R.id.replies_recycler_view);
        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        replyList = new ArrayList<>();
        replyAdapter = new ReplyAdapter(replyList, this);
        repliesRecyclerView.setAdapter(replyAdapter);

        replyInput = findViewById(R.id.reply_input_field);
        submitReplyButton = findViewById(R.id.reply_submit_button);

        sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (getIntent() != null) {
            String title = getIntent().getStringExtra("title");
            String context = getIntent().getStringExtra("context");
            String author = getIntent().getStringExtra("author");
            String createdAt = getIntent().getStringExtra("createdAt");

            titleTextView.setText(title);
            contextTextView.setText(context);
            authorTextView.setText(author);
            createdAtTextView.setText("Created on: " + createdAt);

            loadAuthorAvatar(author);
            loadReplies();
        }

        submitReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postReply();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForumDetailActivity.this, CommunityForumActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadAuthorAvatar(String authorUsername) {
        databaseReference.child("users").child(authorUsername).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profilePictureUrl = dataSnapshot.getValue(String.class);
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Picasso.get()
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(forumAuthorAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load author avatar: " + databaseError.getMessage());
            }
        });
    }

    private void loadReplies() {
        String forumId = getIntent().getStringExtra("forumId");
        if (forumId == null) {
            Log.e("ForumDetailActivity", "Forum ID is null.");
            return;
        }

        databaseReference.child("forums").child(forumId).child("replies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot repliesSnapshot) {
                if (repliesSnapshot.exists()) {
                    for (DataSnapshot replySnapshot : repliesSnapshot.getChildren()) {
                        String replyAuthor = replySnapshot.child("author").getValue(String.class);
                        String replyContent = replySnapshot.child("content").getValue(String.class);
                        String replyCreatedAt = replySnapshot.child("createdAt").getValue(String.class);

                        if (replyAuthor != null && replyContent != null && replyCreatedAt != null) {
                            Reply reply = new Reply(replyAuthor, replyContent, replyCreatedAt);
                            replyList.add(reply);
                        }
                    }

                    replyAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ForumDetailActivity", "No replies found for forum ID: " + forumId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load replies: " + databaseError.getMessage());
            }
        });
    }

    private void postReply() {
        String author = sharedPreferences.getString("username", "User");
        String content = replyInput.getText().toString();
        String createdAt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

        if (content.trim().isEmpty()) {
            Toast.makeText(this, "Reply cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Reply reply = new Reply(author, content, createdAt);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference repliesRef = databaseReference.child("forums").child(getIntent().getStringExtra("forumId")).child("replies");
        DatabaseReference repliesCountRef = databaseReference.child("forums").child(getIntent().getStringExtra("forumId")).child("repliesCount");

        repliesCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(currentCount + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                if (databaseError != null) {
                    Toast.makeText(ForumDetailActivity.this, "Failed to update reply count.", Toast.LENGTH_SHORT).show();
                } else {
                    Integer newCount = currentData.getValue(Integer.class);
                    String replyKey = "reply" + newCount;

                    repliesRef.child(replyKey).setValue(reply).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                replyInput.setText("");
                                Toast.makeText(ForumDetailActivity.this, "Reply posted successfully.", Toast.LENGTH_SHORT).show();

                                updateReplies();
                            } else {
                                Toast.makeText(ForumDetailActivity.this, "Failed to post reply. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateReplies() {
        replyList.clear();
        loadReplies();
    }

    public void loadRepliesAuthorAvatar(String replyAuthorUsername, ImageView replyAuthorAvatar) {
        databaseReference.child("users").child(replyAuthorUsername).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profilePictureUrl = dataSnapshot.getValue(String.class);
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Picasso.get()
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(replyAuthorAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ForumDetailActivity", "Failed to load author avatar: " + databaseError.getMessage());
            }
        });
    }
}
