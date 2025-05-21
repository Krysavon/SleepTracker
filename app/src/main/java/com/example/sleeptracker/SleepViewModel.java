package com.example.sleeptracker;

import androidx.lifecycle.ViewModel;

public class SleepViewModel extends ViewModel {
    private long sleepStartTime1 = 0;  // Время начала сна
    private long wakeTime = 0;         // Время пробуждения

    // Метод для установки времени начала сна
    public void setSleepStartTime1(long timeMillis) {
        sleepStartTime1 = timeMillis;
    }

    // Метод для получения времени начала сна
    public long getSleepStartTime1() {
        return sleepStartTime1;
    }

    // Метод для установки времени пробуждения
    public void setWakeTime(long timeMillis) {
        wakeTime = timeMillis;
    }

    // Метод для получения времени пробуждения
    public long getWakeTime() {
        return wakeTime;
    }

    // Метод для получения продолжительности сна
    public long getSleepDuration1() {
        return wakeTime > sleepStartTime1 ? wakeTime - sleepStartTime1 : 0;
    }
}