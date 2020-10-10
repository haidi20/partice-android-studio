package com.example.employee.table;

import android.icu.text.SimpleDateFormat;

import com.orm.SugarRecord;

public class tbl_m_post extends SugarRecord {
    public String title;
    public String Image;
    public String Content;
    public String date;
    public String type;
    public int paged;
    public String category;

    public tbl_m_post() {
    }

    public tbl_m_post(String title, String image, String content, String date, String type, int paged, String category) {
        this.title = title;
        Image = image;
        Content = content;
        this.date = date;
        this.type = type;
        this.paged = paged;
        this.category = category;
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

    public int getPaged() {
        return paged;
    }

    public void setPaged(int paged) {
        this.paged = paged;
    }
}
