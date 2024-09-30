package com.example.plantpal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

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

        List<String> replyImageUrls = reply.getReplyImageUrls();

        Log.d("ReplyAdapter", "Reply image urls: " + replyImageUrls);

        if (replyImageUrls != null && !replyImageUrls.isEmpty()) {
            holder.replyImagesSection.setVisibility(View.VISIBLE);

            loadImageIntoView(replyImageUrls, holder.replyImage1, 0);
            loadImageIntoView(replyImageUrls, holder.replyImage2, 1);
            loadImageIntoView(replyImageUrls, holder.replyImage3, 2);

        } else {
            holder.replyImagesSection.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    private void loadImageIntoView(List<String> replyImageUrls, ImageView imageView, int index) {
        if (index < replyImageUrls.size()) {
            Picasso.get()
                    .load(replyImageUrls.get(index))
                    .placeholder(R.drawable.reply_image_placeholder)
                    .error(R.drawable.reply_image_placeholder)
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView replyAuthor;
        TextView replyDate;
        TextView replyContent;
        ImageView replyAuthorAvatar;
        LinearLayout replyImagesSection;
        ImageView replyImage1, replyImage2, replyImage3;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            replyAuthor = itemView.findViewById(R.id.reply_author_username);
            replyDate = itemView.findViewById(R.id.reply_date);
            replyContent = itemView.findViewById(R.id.reply_content);
            replyAuthorAvatar = itemView.findViewById(R.id.reply_author_avatar);

            replyImagesSection = itemView.findViewById(R.id.reply_images_section);
            replyImage1 = itemView.findViewById(R.id.reply_image_1);
            replyImage2 = itemView.findViewById(R.id.reply_image_2);
            replyImage3 = itemView.findViewById(R.id.reply_image_3);
        }
    }
}