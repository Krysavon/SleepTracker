package com.example.sleeptracker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        ImageView backButton2 = findViewById(R.id.backButtonProfile);
        backButton2.setOnClickListener(v -> finish());
    }
}
