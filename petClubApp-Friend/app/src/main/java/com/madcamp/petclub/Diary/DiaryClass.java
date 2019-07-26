package com.madcamp.petclub.Diary;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class DiaryClass {

    private String id;
    private String title;
    private String date;
    private String contents;

    public DiaryClass() {}

    public DiaryClass(String id, String title, String contents, String date){
        this.id = id;
        this.title = title;
        this.date = date;
        this.contents = contents;
    }

    public String getId() {
        return id;
    }
    public String getContents() {
        return contents;
    }
    public String getDate() {
        return date;
    }
    public String getTitle() {return title; }

    public void setId(String id) {
        this.id = id;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("contents", contents);
        result.put("date", date);
        result.put("title", title);
        return result;
    }
}
