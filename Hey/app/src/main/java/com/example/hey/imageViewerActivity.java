package com.example.hey;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class imageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView=findViewById(R.id.imageViewer);

        imageUrl=getIntent().getStringExtra("url");

        Glide.with(imageViewerActivity.this)
                .load(imageUrl).into(imageView);


    }
}