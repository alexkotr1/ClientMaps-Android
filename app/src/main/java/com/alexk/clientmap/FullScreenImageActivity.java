package com.alexk.clientmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image);
        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);
        Intent intent = getIntent();
        Bitmap bitmap = intent.getParcelableExtra("image");

        if (bitmap != null) {
            fullScreenImageView.setImageBitmap(bitmap);
        }
    }
}
