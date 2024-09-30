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

public class ForumPreviewImagesAdapter extends RecyclerView.Adapter<ForumPreviewImagesAdapter.ImageViewHolder> {

    private Context context;
    private ArrayList<Uri> imageUris;
    private HashMap<Uri, Integer> imagePositionsMap;
    private OnRemoveImageListener onRemoveImageListener;


    public interface OnRemoveImageListener {
        void onRemoveImage(int position);
    }

    public ForumPreviewImagesAdapter(Context context, ArrayList<Uri> imageUris, HashMap<Uri, Integer> imagePositionsMap, OnRemoveImageListener listener) {
        this.context = context;
        this.imageUris = imageUris;
        this.imagePositionsMap = imagePositionsMap;
        this.onRemoveImageListener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;
        ImageView previewImageDeleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            previewImage = itemView.findViewById(R.id.preview_image);
            previewImageDeleteButton = itemView.findViewById(R.id.preview_image_delete_button);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.forum_preview_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        holder.previewImage.setImageURI(imageUri);

        holder.previewImageDeleteButton.setOnClickListener(v -> {
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
