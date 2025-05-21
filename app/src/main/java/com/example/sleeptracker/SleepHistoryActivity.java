package com.example.sleeptracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;



import com.example.sleeptracker.db.SleepDatabase;
import com.example.sleeptracker.db.SleepRecord;

import java.util.List;
import java.util.Locale;

public class SleepHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SleepRecordAdapter adapter;
    private SleepDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_history);

        database = SleepDatabase.getInstance(this);
        recyclerView = findViewById(R.id.sleepHistoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView backButton = findViewById(R.id.backButtonHistory);
        backButton.setOnClickListener(v -> finish());

        LinearLayout navSettings = findViewById(R.id.navSettings);
        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SleepHistoryActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SleepHistoryActivity.this, MainActivity.class);
                startActivity(intent);
                // finish();
            }
        });


        loadFullHistory();
    }

    private void loadFullHistory() {
        new Thread(() -> {
            List<SleepRecord> records = database.sleepDao().getAll();

            runOnUiThread(() -> {
                adapter = new SleepRecordAdapter(records);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private String formatDurationFromMillis(long millis) {
        long h = millis / (3600_000);
        long m = (millis % 3600_000) / 60000;
        return String.format(Locale.getDefault(), "%dч %02dмин", h, m);
    }
}
