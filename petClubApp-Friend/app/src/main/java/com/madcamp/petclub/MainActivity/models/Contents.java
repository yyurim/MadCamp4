package com.madcamp.petclub.MainActivity.models;

public class Contents {
    private String contentTitle;
    private String contentDesc;
    private String contentImage;
    private String contentUrl;

    private String type;

    public Contents(String title, String desc, String image, String url, String type) {
        this.contentTitle = title;
        this.contentDesc = desc;
        this.contentImage = image;
        this.contentUrl = url;
        this.type = type;
    }

    public Contents(){}

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public String getContentImage() {
        return contentImage;
    }

    public void setContentImage(String contentImage) {
        this.contentImage = contentImage;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getContentType() {
        return type;
    }

    public void setContentType(String type) {
        this.type = type;
    }

}
