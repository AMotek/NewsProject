package com.example.newsproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class NewsReport implements Serializable {

    private String title;
    private String content;
    private String date;
    private String imageURL;
    private String siteName;
    private String articleUrl;

    public NewsReport(String title, String content, String date, @Nullable String imageURL,
                      String siteName, @NonNull String articleUrl) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.imageURL = imageURL;
        this.siteName = siteName;
        this.articleUrl = articleUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }
}
