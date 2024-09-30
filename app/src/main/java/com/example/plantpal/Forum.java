package com.example.plantpal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Forum {
    private String forumId;
    private String title;
    private String context;
    private String author;
    private String createdAt;
    private Map<String, String> replies;
    private int repliesCount;
    private List<String> forumImageUrls;

    public Forum() {
        this.replies = new HashMap<>();
        this.forumImageUrls = new ArrayList<>();
        this.repliesCount = 0;
    }

    public Forum(String forumId, String title, String context, String author, String createdAt, Map<String, String> replies, int repliesCount, List<String> forumImageUrls) {
        this.forumId = forumId;
        this.title = title;
        this.context = context;
        this.author = author;
        this.createdAt = createdAt;
        this.replies = replies;
        this.repliesCount = repliesCount;
        this.forumImageUrls = forumImageUrls;
    }

    public String getForumId() {
        return forumId;
    }

    public String getTitle() {
        return title;
    }

    public String getContext() {
        return context;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getReplies() {
        return replies;
    }

    public int getRepliesCount() {
        return repliesCount;
    }

    public List<String> getForumImageUrls() {
        return forumImageUrls;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setReplies(Map<String, String> replies) {
        this.replies = replies;
    }

    public void setRepliesCount(int repliesCount) {
        this.repliesCount = repliesCount;
    }

    public void setForumImageUrls(List<String> forumImageUrls) {
        this.forumImageUrls = forumImageUrls;
    }
}
