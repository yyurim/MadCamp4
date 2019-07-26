package com.madcamp.petclub.News;

public class News {
    private String newsTitle;
    private String newsDesc;
    private String newsImage;
    private String newsUrl;

    public News(String title, String desc, String image, String url) {
        this.newsTitle = title;
        this.newsDesc = desc;
        this.newsImage = image;
        this.newsUrl = url;
    }

    public News(){}

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsDesc() {
        return newsDesc;
    }

    public void setNewsDesc(String newsDesc) {
        this.newsDesc = newsDesc;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl;
    }
}
