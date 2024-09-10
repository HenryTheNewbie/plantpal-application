package com.example.plantpal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {

    private List<Forum> forumList;
    private Context context;

    public ForumAdapter(List<Forum> forumList, Context context) {
        this.forumList = forumList;
        this.context = context;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forum_item, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {
        Forum forum = forumList.get(position);
        holder.forumTitle.setText(forum.getTitle());
        holder.forumCreatedAt.setText("Created on: " + forum.getCreatedAt());
        holder.forumRepliesCount.setText("Replies: " + forum.getRepliesCount());
        holder.forumAuthor.setText("By: " + forum.getAuthor());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ForumDetailActivity.class);
            intent.putExtra("forumId", forum.getForumId());
            intent.putExtra("title", forum.getTitle());
            intent.putExtra("context", forum.getContext());
            intent.putExtra("createdAt", forum.getCreatedAt());
            intent.putExtra("author", forum.getAuthor());
            intent.putExtra("repliesCount", forum.getRepliesCount());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView forumTitle;
        TextView forumCreatedAt;
        TextView forumRepliesCount;
        TextView forumAuthor;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            forumTitle = itemView.findViewById(R.id.forum_title);
            forumCreatedAt = itemView.findViewById(R.id.forum_date);
            forumRepliesCount = itemView.findViewById(R.id.forum_replies_count);
            forumAuthor = itemView.findViewById(R.id.forum_author);
        }
    }
}
