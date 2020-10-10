package com.example.employee.model;


import android.icu.text.SimpleDateFormat;
import android.text.Html;

public class Post {
    public Long id;
    public String title;
    public String Image;
    public String Content;
    public String date;
    public String type;
    public String category;

    public Post() {

    }

    public Post(Long id, String title, String image, String content, String date, String type, String category) {
        this.id = id;
        this.title = title;
        this.Image = image;
        this.Content = content;
        this.date = date;
        this.type = type;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getDate() {
        return date;
    }

    // data local or online
    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLimitContent() {
        String text;
        if(Content.length() > 15) {
            text = Html.fromHtml(Content.substring(0, 15)).toString() + "...";
        }else {
            text = Content;
        }

        return text;
    }

    public String getLimitTitle() {
        String text;
        if(title.length() > 10) {
            text = Html.fromHtml(title.substring(0, 10)).toString() + "...";
        }else {
            text = title;
        }

        return text;
    }
}
