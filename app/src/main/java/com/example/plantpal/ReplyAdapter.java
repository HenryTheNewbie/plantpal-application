package com.example.plantpal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private List<Reply> replyList;
    private Context context;

    public ReplyAdapter(List<Reply> replyList, Context context) {
        this.replyList = replyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replyList.get(position);
        holder.replyAuthor.setText(reply.getAuthor());
        holder.replyDate.setText("Replied on: " + reply.getCreatedAt());
        holder.replyContent.setText(reply.getContent());

        ((ForumDetailActivity) context).loadRepliesAuthorAvatar(reply.getAuthor(), holder.replyAuthorAvatar);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView replyAuthor;
        TextView replyDate;
        TextView replyContent;
        ImageView replyAuthorAvatar;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            replyAuthor = itemView.findViewById(R.id.reply_author_username);
            replyDate = itemView.findViewById(R.id.reply_date);
            replyContent = itemView.findViewById(R.id.reply_content);
            replyAuthorAvatar = itemView.findViewById(R.id.reply_author_avatar);
        }
    }
}