package com.example.sleeptracker.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_records")
public class SleepRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;           // Например: "2025-05-16"
    public long timesleepstart;   // Время начала сна в миллисекундах
    public long timesleepend;     // Время конца сна в миллисекундах

    public SleepRecord(String date, long timesleepstart, long timesleepend) {
        this.date = date;
        this.timesleepstart = timesleepstart;
        this.timesleepend = timesleepend;
    }
}
