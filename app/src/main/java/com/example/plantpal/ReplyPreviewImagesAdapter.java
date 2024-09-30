package com.example.plantpal;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class ReplyPreviewImagesAdapter extends RecyclerView.Adapter<ReplyPreviewImagesAdapter.ImageViewHolder> {

    private Context context;
    private ArrayList<Uri> imageUris;
    private HashMap<Uri, Integer> imagePositionsMap;
    private OnRemoveImageListener onRemoveImageListener;

    public interface OnRemoveImageListener {
        void onRemoveImage(int position);
    }

    public ReplyPreviewImagesAdapter(Context context, ArrayList<Uri> imageUris, HashMap<Uri, Integer> imagePositionsMap, OnRemoveImageListener listener) {
        this.context = context;
        this.imageUris = imageUris;
        this.imagePositionsMap = imagePositionsMap;
        this.onRemoveImageListener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView replyImage;
        ImageView replyImageDeleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            replyImage = itemView.findViewById(R.id.preview_image);
            replyImageDeleteButton = itemView.findViewById(R.id.preview_image_delete_button);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reply_preview_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.replyImage.setImageURI(imageUri);

        holder.replyImageDeleteButton.setOnClickListener(v -> {
            if (onRemoveImageListener != null) {
                onRemoveImageListener.onRemoveImage(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void addImage(Uri imageUri, int databasePosition) {
        imageUris.add(imageUri);
        imagePositionsMap.put(imageUri, databasePosition);
        notifyItemInserted(imageUris.size() - 1);
    }

    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            Uri removedUri = imageUris.get(position);

            imageUris.remove(position);
            imagePositionsMap.remove(removedUri);

            notifyItemRemoved(position);
        }
    }
}
