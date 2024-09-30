package com.example.plantpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityForumActivity extends AppCompatActivity {

    private ImageView createForumButton;

    private EditText searchBar;

    private BottomNavigationView bottomNavigationView;

    private RecyclerView forumsRecyclerView;
    private ForumAdapter forumAdapter;
    private List<Forum> forumList;
    private List<Forum> filteredForumList;

    private DatabaseReference forumsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_forum_page);

        searchBar = findViewById(R.id.search_bar);

        createForumButton = findViewById(R.id.create_forum_button);

        bottomNavigationView = findViewById(R.id.navigation_bar);

        forumsRecyclerView = findViewById(R.id.forums_recycler_view);

        forumList = new ArrayList<>();
        filteredForumList = new ArrayList<>();
        forumAdapter = new ForumAdapter(filteredForumList, this);

        forumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forumsRecyclerView.setAdapter(forumAdapter);

        forumsRef = FirebaseDatabase.getInstance().getReference().child("forums");

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterForums(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        int selectedItemId = getIntent().getIntExtra("selected_item_id", -1);

        if (selectedItemId != -1) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        createForumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityForumActivity.this, CreateForumActivity.class);
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {@Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_plant_scan) {
                    Intent intent = new Intent(CommunityForumActivity.this, ScanActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_plant_archive) {
                    Intent intent = new Intent(CommunityForumActivity.this, PlantArchiveActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_community_hub) {
                    return true;
                } else if (itemId == R.id.nav_my_garden) {
                    Intent intent = new Intent(CommunityForumActivity.this, MyGardenActivity.class);
                    intent.putExtra("selected_item_id", itemId);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        loadForums();
    }

    private void filterForums(String query) {
        filteredForumList.clear();

        if (query.isEmpty()) {
            filteredForumList.addAll(forumList);
        } else {
            for (Forum forum : forumList) {
                if (forum.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredForumList.add(forum);
                }
            }
        }
        forumAdapter.notifyDataSetChanged();
    }

    private void loadForums() {
        forumsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forumList.clear();
                filteredForumList.clear();

                Log.d("FirebaseData", "DataSnapshot: " + dataSnapshot.toString());

                for (DataSnapshot forumSnapshot : dataSnapshot.getChildren()) {
                    String forumId = forumSnapshot.child("forumId").getValue(String.class);
                    String title = forumSnapshot.child("title").getValue(String.class);
                    String context = forumSnapshot.child("context").getValue(String.class);
                    String createdAt = forumSnapshot.child("createdAt").getValue(String.class);
                    String author = forumSnapshot.child("author").getValue(String.class);
                    Long repliesCountLong = forumSnapshot.child("repliesCount").getValue(Long.class);

                    int repliesCount = (repliesCountLong != null) ? repliesCountLong.intValue() : 0;

                    List<String> forumImageUrls = new ArrayList<>();
                    for (DataSnapshot imageUrlSnapshot : forumSnapshot.child("forumImageUrls").getChildren()) {
                        String imageUrl = imageUrlSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            forumImageUrls.add(imageUrl);
                        }
                    }

                    Forum forum = new Forum(forumId, title, context, author, createdAt, null, repliesCount, forumImageUrls);

                    forumList.add(forum);

                    Log.d("ForumItem", "Title: " + title + ", Author: " + author);
                }

                filterForums(searchBar.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CommunityForumActivity", "Failed to load forums", databaseError.toException());
            }
        });
    }
}
