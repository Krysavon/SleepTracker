package com.example.sleeptracker;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.ImageView;
import android.content.Intent;
import android.view.ViewGroup;




import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.LinearLayout;
import com.example.sleeptracker.db.SleepDatabase;
import com.example.sleeptracker.db.SleepRecord;
import com.example.sleeptracker.db.SleepDao;



public class MainActivity extends AppCompatActivity {

    private SleepViewModel viewModel;
    private TextView sleepStartText, sleepEndText, durationText;
    private TextView sleepStartLabel, wakeTimeLabel;
    private boolean isSleeping = false;
    private long sleepStartTime = 0;
    private SleepDatabase database;
    private LinearLayout historyContainer;
    private android.os.Handler sleepDurationHandler = new android.os.Handler();
    private Runnable sleepDurationRunnable;
    private TextView comparisonText;
    private View sleepLineView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        comparisonText = findViewById(R.id.sleepComparisonText);

        viewModel = new ViewModelProvider(this).get(SleepViewModel.class);
        database = SleepDatabase.getInstance(this);

        historyContainer = findViewById(R.id.historyListContainer);
        loadHistory();  // сразу после создания экрана загрузим существующие записи

        sleepStartText = findViewById(R.id.sleepStartTime1);
        sleepEndText = findViewById(R.id.wakeTime);
        durationText = findViewById(R.id.sleepDurationText1);

        sleepLineView = findViewById(R.id.sleepGraphBar);
        sleepStartLabel = findViewById(R.id.sleepStartLabel);
        wakeTimeLabel = findViewById(R.id.wakeTimeLabel);

        // Устанавливаем обработчики кликов на TextView для выбора времени
        sleepStartText.setOnClickListener(v -> showTimePicker(true));
        sleepEndText.setOnClickListener(v -> showTimePicker(false));
        sleepStartLabel.setOnClickListener(v -> showTimePicker(true));
        wakeTimeLabel.setOnClickListener(v -> showTimePicker(false));

        LinearLayout sleepToggleButton = findViewById(R.id.sleepToggleButton);
        TextView sleepButtonText = findViewById(R.id.sleepButtonText);
        TextView sleepStartTimeText = findViewById(R.id.sleepStartTime);
        TextView sleepEndTimeText = findViewById(R.id.sleepEndTime);
        ImageView sleepToggleIcon = findViewById(R.id.iconSleep);

        LinearLayout moreLayout = findViewById(R.id.rnruwix27rei);
        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SleepHistoryActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout navSettings = findViewById(R.id.navSettings);
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        sleepToggleButton.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            String formattedTime = formatTime(now);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (!isSleeping) {
                // Засыпание
                sleepStartTime = now;
                sleepStartTimeText.setText("Время засыпания " + formattedTime);
                sleepButtonText.setText("Пробуждение");
                sleepToggleButton.setBackgroundColor(android.graphics.Color.parseColor("#F17D3E"));
                sleepToggleIcon.setImageResource(R.drawable.moon1); // ← Смена иконки
                isSleeping = true;

                durationText.setText("00 ч 00 мин");

                sleepDurationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();
                        long durationMillis = now - sleepStartTime;
                        durationText.setText(formatDuration(durationMillis));

                        // повторяем каждую минуту
                        sleepDurationHandler.postDelayed(this, 60 * 1000);
                    }
                };
                sleepDurationHandler.post(sleepDurationRunnable); // старт

            } else {
                // Пробуждение
                long sleepEndTime = now;

                sleepEndTimeText.setText("Время пробуждения " + formattedTime);
                sleepButtonText.setText("Ложусь спать");
                sleepToggleButton.setBackgroundColor(android.graphics.Color.parseColor("#3E98F1"));
                sleepToggleIcon.setImageResource(R.drawable.alarm); // ← Смена иконки
                isSleeping = false;

                if (sleepDurationRunnable != null) {
                    sleepDurationHandler.removeCallbacks(sleepDurationRunnable);
                }


                // Сохраняем в БД
                long finalSleepStartTime = sleepStartTime;
                new Thread(() -> {
                    database.sleepDao().insert(
                            new SleepRecord(date, finalSleepStartTime, sleepEndTime)
                    );
                    runOnUiThread(() -> {
                        loadHistory();

                        // Добавляем вызов сравнения сна здесь:
                        int currentSleepMinutes = (int) ((sleepEndTime - finalSleepStartTime) / 60000);
                        updateSleepComparison(currentSleepMinutes);
                        updateView();
                    });
                }).start();
            }

        });
    }

    private void updateSleepLine(final long sleepStartTimeMillis, final long wakeTimeMillis) {
        if (sleepStartTimeMillis == 0 || wakeTimeMillis == 0) {
            sleepLineView.setVisibility(View.GONE);
            return;
        }

        long adjustedWakeTimeMillis = wakeTimeMillis;
        if (wakeTimeMillis <= sleepStartTimeMillis) {
            adjustedWakeTimeMillis += 24 * 3600 * 1000L;
        }

        sleepLineView.setVisibility(View.VISIBLE);

        int startColor = Color.parseColor("#4074D8");
        int endColor = Color.parseColor("#EC9631");

        long durationMillis = wakeTimeMillis - sleepStartTimeMillis;
        float durationHours = durationMillis / (3600f * 1000f);
        if (durationHours > 24) durationHours = 24;

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTimeInMillis(sleepStartTimeMillis);
        float startHour = calendarStart.get(Calendar.HOUR_OF_DAY) + calendarStart.get(Calendar.MINUTE) / 60f;
        float startPercent = startHour / 24f;

        int containerWidth = ((View) sleepLineView.getParent()).getWidth();
        if (containerWidth == 0) {
            sleepLineView.post(() -> updateSleepLine(sleepStartTimeMillis, wakeTimeMillis));
            return;
        }

        int lineWidth = (int) (containerWidth * Math.min(1f, durationHours / 24f));
        int marginStart = (int) (containerWidth * startPercent);

        LinearGradient gradient = new LinearGradient(
                0, 0, lineWidth, 0,
                startColor, endColor,
                Shader.TileMode.CLAMP
        );

        android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable(
                new android.graphics.drawable.shapes.RectShape()
        );
        shapeDrawable.getPaint().setShader(gradient);

        sleepLineView.setBackground(shapeDrawable);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) sleepLineView.getLayoutParams();
        params.width = lineWidth;
        params.leftMargin = marginStart;
        sleepLineView.setLayoutParams(params);
    }

    // Метод для отображения TimePickerDialog
    private void showTimePicker(boolean isStart) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(MainActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    long timeMillis = calendar.getTimeInMillis();

                    if (isStart) {
                        viewModel.setSleepStartTime1(timeMillis);
                        Toast.makeText(this, "Время засыпания выбрано", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.setWakeTime(timeMillis);
                        Toast.makeText(this, "Время пробуждения выбрано", Toast.LENGTH_SHORT).show();
                    }

                    updateView();
                    updateSleepLine(viewModel.getSleepStartTime1(), viewModel.getWakeTime());
                }, hour, minute, true);

        dialog.show();
    }

    private void updateSleepComparison(int currentSleepMinutes) {
        new Thread(() -> {
            Double averageSleep = database.sleepDao().getAverageSleepDuration();
            runOnUiThread(() -> {
                if (averageSleep != null && averageSleep > 0) {
                    int avg = averageSleep.intValue();
                    int percentDiff = (int)((currentSleepMinutes - avg) * 100.0 / avg);
                    if (percentDiff < 0) {
                        comparisonText.setText("На " + Math.abs(percentDiff) + "% меньше чем обычно");
                    } else if (percentDiff > 0) {
                        comparisonText.setText("На " + percentDiff + "% больше чем обычно");
                    } else {
                        comparisonText.setText("Как обычно");
                    }
                } else {
                    comparisonText.setText("Нет данных для сравнения");
                }
            });
        }).start();
    }

    /** Загружает все записи из БД и отображает их в historyContainer */
    private void loadHistory() {
        new Thread(() -> {
            List<SleepRecord> records = database.sleepDao().getLastThree();
            runOnUiThread(() -> {
                historyContainer.removeAllViews();
                for (SleepRecord rec : records) {
                    View item = getLayoutInflater()
                            .inflate(R.layout.history_item, historyContainer, false);

                    TextView tvDur  = item.findViewById(R.id.textDuration);
                    TextView tvNorm = item.findViewById(R.id.textNorm);
                    TextView tvStat = item.findViewById(R.id.textStatus);
                    ImageView ivStatus = item.findViewById(R.id.imageStatus);

                    long durMillis = rec.timesleepend - rec.timesleepstart;
                    String durText = formatDurationFromMillis(durMillis);
                    tvDur.setText("Ночной сон " + durText);

                    if (durMillis < 6 * 3600_000L) {
                        tvStat.setText("Ниже нормы");
                        tvStat.setTextColor(Color.parseColor("#FFAD3C")); // оранжевый
                        ivStatus.setImageResource(R.drawable.lucide_circle_alert);
                    } else if (durMillis <= 10 * 3600_000L) {
                        tvStat.setText("Норма");
                        tvStat.setTextColor(Color.parseColor("#08B200")); // зелёный
                        ivStatus.setImageResource(R.drawable.lucide_circle_check);
                    } else {
                        tvStat.setText("Выше нормы");
                        tvStat.setTextColor(Color.parseColor("#FFAD3C")); // оранжевый
                        ivStatus.setImageResource(R.drawable.lucide_circle_alert);
                    }

                    historyContainer.addView(item);
                }
            });
        }).start();
    }

    private String formatDurationFromMillis(long millis) {
        long h = millis / (3600_000);
        long m = (millis % 3600_000) / 60000;
        return String.format(Locale.getDefault(), "%dч %02dмин", h, m);
    }

    // Метод для обновления UI
    private void updateView() {
        long start = viewModel.getSleepStartTime1();
        long end = viewModel.getWakeTime();
        long duration = viewModel.getSleepDuration1();

        if (start > 0)
            sleepStartText.setText(formatTime(start));
        else
            sleepStartText.setText("--:--");

        if (end > 0)
            sleepEndText.setText(formatTime(end));
        else
            sleepEndText.setText("--:--");

        if (duration > 0)
            durationText.setText("Продолжительность сна: " + formatDuration(duration));
        else
            durationText.setText("Продолжительность сна: —");

        // Обновление duration в карточке сна
        TextView sleepStartTextView = findViewById(R.id.sleepStartTime);
        TextView sleepEndTextView = findViewById(R.id.sleepEndTime);
        TextView durationTextCard = findViewById(R.id.sleepDurationText1);

        String startTextStr = sleepStartTextView.getText().toString(); // "Время засыпания 01:14"
        String endTextStr = sleepEndTextView.getText().toString();     // "Время пробуждения 07:39"

        if (startTextStr.contains(":") && endTextStr.contains(":")) {
            String startTimeStr = extractTime(startTextStr);
            String endTimeStr = extractTime(endTextStr);

            long durationMinutes = calculateSleepDuration(startTimeStr, endTimeStr);
            durationTextCard.setText(formatDurationFromMinutes(durationMinutes));

            updateSleepComparison((int) durationMinutes);

        } else {
            durationTextCard.setText("Продолжительность сна: —");
        }
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    private String formatDuration(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d ч %02d мин", hours, minutes);
    }

    private String extractTime(String fullText) {
        return fullText.replaceAll(".*?(\\d{2}:\\d{2})", "$1");
    }

    private long calculateSleepDuration(String startTime, String endTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date startDate = format.parse(startTime);
            Date endDate = format.parse(endTime);

            long startMillis = startDate.getTime();
            long endMillis = endDate.getTime();



            return (endMillis - startMillis) / (60 * 1000); // в минутах
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String formatDurationFromMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format(Locale.getDefault(), "%02d ч %02d мин", hours, minutes);
    }

}
