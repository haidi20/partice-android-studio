package com.example.employee;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    public static final String SP_ID = "spId";
    public static final String SP_TITLE = "spTitle";
    public static final String SP_CONTENT = "spContent";
    public static final String SP_URL_IMAGE = "spUrlImage";
    public static final String SP_SAVE_CACHE = "spSaveCache";
    public static final String SP_MODE_EDIT = "spModeEdit";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public SharedPrefManager(Context context){
        sp = context.getSharedPreferences(SP_TITLE, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void setId(String keySP, Long value){
        spEditor.putLong(keySP, value);
        spEditor.commit();
    }

    public void setTitle(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void setUrlImage(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void setContent(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void setSaveCache(String keySP, boolean value){
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void setModeEdit(String keySP, boolean value){
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public Long getId() { return sp.getLong(SP_ID, 0); }

    public String getTitle(){
        return sp.getString(SP_TITLE, "");
    }

    public String getUrlImage() {
        return sp.getString(SP_URL_IMAGE, "");
    }

    public String getContent() {
        return sp.getString(SP_CONTENT, "");
    }

    public Boolean getSaveCache() { return sp.getBoolean(SP_SAVE_CACHE, false); }

    public Boolean getModeEdit() { return sp.getBoolean(SP_MODE_EDIT, false); }
}
