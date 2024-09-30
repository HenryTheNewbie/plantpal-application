package com.example.plantpal;

import java.util.ArrayList;
import java.util.List;

public class Reply {
    private String author;
    private String content;
    private String createdAt;
    private List<String> replyImageUrls;

    public Reply() {
    }

    public Reply(String author, String content, String createdAt, List<String> replyImageUrls) {
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
        this.replyImageUrls = replyImageUrls;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<String> getReplyImageUrls() {
        return replyImageUrls;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setReplyImageUrls(List<String> replyImageUrls) {
        this.replyImageUrls = replyImageUrls;
    }
}

