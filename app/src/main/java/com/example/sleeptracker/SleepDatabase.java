package com.example.sleeptracker.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SleepRecord.class}, version = 2, exportSchema = false)
public abstract class SleepDatabase extends RoomDatabase {

    private static SleepDatabase instance;

    public abstract SleepDao sleepDao();

    public static synchronized SleepDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SleepDatabase.class, "sleep_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
