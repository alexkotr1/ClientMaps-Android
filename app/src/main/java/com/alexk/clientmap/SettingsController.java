package com.alexk.clientmap;


import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SettingsController  extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Ρυθμίσεις");
        setContentView(R.layout.activity_settings_controller);
        Preferences prefs = new Preferences(getApplicationContext());
        EditText host = findViewById(R.id.host);
        EditText user = findViewById(R.id.user);
        EditText pass = findViewById(R.id.password);
        host.setText(prefs.getHost());
        user.setText(prefs.getUser());
        pass.setText(prefs.getPass());
        Button ok = findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.saveData("host",host.getText().toString().endsWith("/") ? host.getText().toString() : host.getText().toString() + "/");
                prefs.saveData("user",user.getText().toString());
                prefs.saveData("password",pass.getText().toString());
                Helper.Toast("OK!",SettingsController.this,MainActivity.class);

            }
        });
    }


}
