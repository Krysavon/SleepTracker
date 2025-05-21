package com.example.sleeptracker.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepDao {

    @Insert
    void insert(SleepRecord record);

    @Query("SELECT * FROM sleep_records ORDER BY timesleepstart DESC LIMIT 3")
    List<SleepRecord> getLastThree();

    @Query("SELECT * FROM sleep_records WHERE date = :date ORDER BY timesleepstart")
    List<SleepRecord> getRecordsByDate(String date);

    @Query("SELECT * FROM sleep_records ORDER BY date DESC")
    List<SleepRecord> getAll();

    @Query("SELECT AVG((timesleepend - timesleepstart)/60000.0) FROM sleep_records")
    Double getAverageSleepDuration();

}

