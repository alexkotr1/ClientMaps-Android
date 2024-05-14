package com.alexk.clientmap;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class Preferences {

    private final Context context;

    public Preferences(Context mContext) {
        this.context = mContext;
    }

    public void saveData(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public String getUser(){
        return getData("user");
    }
    public String getHost(){
        return getData("host");
    }
    public String getPass() { return getData("password"); }
}
