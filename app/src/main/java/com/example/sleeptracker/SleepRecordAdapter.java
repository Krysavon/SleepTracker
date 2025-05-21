package com.example.sleeptracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sleeptracker.db.SleepRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepRecordAdapter extends RecyclerView.Adapter<SleepRecordAdapter.ViewHolder> {

    private final List<SleepRecord> sleepRecords;

    public SleepRecordAdapter(List<SleepRecord> records) {
        this.sleepRecords = records;
    }

    @NonNull
    @Override
    public SleepRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_record, parent, false);  // исправил на ваш layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepRecordAdapter.ViewHolder holder, int position) {
        SleepRecord record = sleepRecords.get(position);

        // Форматируем даты и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String dateText = dateFormat.format(new Date(record.timesleepstart));
        String sleepStartText = "Время засыпания: " + timeFormat.format(new Date(record.timesleepstart));
        String sleepEndText = "Время пробуждения: " + timeFormat.format(new Date(record.timesleepend));

        long durMillis = record.timesleepend - record.timesleepstart;
        String durationText = "Ночной сон " + formatDurationFromMillis(durMillis);

        // Заполняем поля
        holder.dateTextView.setText(dateText);
        holder.sleepDurationTextView.setText(durationText);
        holder.sleepStartTextView.setText(sleepStartText);
        holder.sleepEndTextView.setText(sleepEndText);

        // Логика статуса сна
        if (durMillis < 6 * 3600_000L) {
            holder.normLabelTextView.setText("Ниже нормы");
            holder.normLabelTextView.setTextColor(Color.parseColor("#FFAD3C"));
            holder.normImageView.setImageResource(R.drawable.lucide_circle_alert);
            holder.normValueTextView.setText("Норма: 6-10ч");
        } else if (durMillis <= 10 * 3600_000L) {
            holder.normLabelTextView.setText("Норма");
            holder.normLabelTextView.setTextColor(Color.parseColor("#08B200"));
            holder.normImageView.setImageResource(R.drawable.lucide_circle_check);
            holder.normValueTextView.setText("Норма: 6-10ч");
        } else {
            holder.normLabelTextView.setText("Выше нормы");
            holder.normLabelTextView.setTextColor(Color.parseColor("#FFAD3C"));
            holder.normImageView.setImageResource(R.drawable.lucide_circle_alert);
            holder.normValueTextView.setText("Норма: 6-10ч");
        }
    }

    @Override
    public int getItemCount() {
        return sleepRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView sleepDurationTextView;
        TextView sleepStartTextView;
        TextView sleepEndTextView;
        TextView normLabelTextView;
        TextView normValueTextView;
        ImageView normImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            sleepDurationTextView = itemView.findViewById(R.id.sleepDurationTextView);
            sleepStartTextView = itemView.findViewById(R.id.sleepStartTextView);
            sleepEndTextView = itemView.findViewById(R.id.sleepEndTextView);
            normLabelTextView = itemView.findViewById(R.id.normLabelTextView);
            normValueTextView = itemView.findViewById(R.id.normValueTextView);
            normImageView = itemView.findViewById(R.id.normImageView); // В вашем layout ImageView без id — нужно добавить id!
        }
    }

    private String formatDurationFromMillis(long millis) {
        long h = millis / 3600_000;
        long m = (millis % 3600_000) / 60000;
        return String.format(Locale.getDefault(), "%dч %02dмин", h, m);
    }
}
