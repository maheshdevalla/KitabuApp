package com.example.jgraham.kitabureg1;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduleActivity extends AppCompatActivity implements DialogManual.ManualDialogListener {

    // Calendar for storing the user's time of notification
    protected Calendar m_calendar;

    // ID for URL (associated with Prashant DB)
    protected int m_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmschedule);

        // Store ID
        m_id = getIntent().getIntExtra("id", -1);

        // Is id value returned is invalid then don't set alarm
        if (m_id == -1) {
            Log.d("ALARM", "Error getting id of URL (-1)");
            finish();
        }
        // Init calendar that user sets time and date for
        m_calendar = Calendar.getInstance();
        m_calendar.setTimeInMillis(System.currentTimeMillis());
        ListView list_view = (ListView) findViewById(R.id.alarmschedule_list_view);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d("TAG", "POSITION: " + position);
                // Position 0: Date
                if (position == 0) {
                    DialogFragment dialog_fragment = DialogManual.newInstance(DialogManual.DATE_PICKER_DIALOG);
                    dialog_fragment.show(getFragmentManager(), "date_picker_dialog");
                }
                // Position 1: Time
                else if (position == 1) {
                    DialogFragment dialog_fragment = DialogManual.newInstance(DialogManual.TIME_PICKER_DIALOG);
                    dialog_fragment.show(getFragmentManager(), "time_picker_dialog");
                }
            }
        });
    }

    // Method to select the Data picker: used references from MyRuns.
    @Override
    public void onDatePickerDialogSet(DialogFragment dialog, int year, int month, int day) {
        Log.d("ALARM", "day: " + day);
        m_calendar.set(year, month, (day));
    }

    // Method to select the Time picker: used references from MyRuns.
    @Override
    public void onTimePickerDialogSet(DialogFragment dialog, int hour, int minute) {
        m_calendar.set(Calendar.HOUR_OF_DAY, hour);
        m_calendar.set(Calendar.MINUTE, minute);
    }

    // Save clicked and schedule the alarm.
    public void scheduleClick(View view) {
        // Handle date field
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String date_string = sdf.format(m_calendar.getTime());
        // Schedule alarm based on calendar
        // Calculate the time when it expires.
        Intent intent = new Intent(this, EMAAlarmReceiver.class);
        // Store ID in the Database
        intent.putExtra("id", m_id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, m_calendar.getTimeInMillis(), pendingIntent);
        Log.d("ALARM", "Alarm set at " + date_string + " for url with id: " + m_id);
        Log.d("ALARM", "TIME: " + m_calendar.getTime().toString());
        Log.d("ALARM", "NOW: " + System.currentTimeMillis() + " Cal: " + m_calendar.getTimeInMillis() + " diff: " + (m_calendar.getTimeInMillis() - System.currentTimeMillis()));
        Toast.makeText(getApplicationContext(), "Alarm set for " + date_string, Toast.LENGTH_SHORT).show();
        finish();
    }
}
