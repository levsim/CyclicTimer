package com.simonov.cyclic.cyclictimer;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final String PAUSE = "PAUSE";
    private static final String INTERVAL = "INTERVAL";

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    private TextView textViewPause;
    private TextView textViewTime;
    private TextView textViewPauseTitle;
    private EditText editTextPause;
    private EditText editTextInterval;

    private TextInputLayout inputLayoutPause;
    private TextInputLayout inputLayoutInterval;

    private Button button;

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputLayoutPause = (TextInputLayout) findViewById(R.id.inputLayoutPause);
        inputLayoutInterval = (TextInputLayout) findViewById(R.id.inputLayoutInterval);

        textViewPause = (TextView) findViewById(R.id.textViewPause);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewPauseTitle = (TextView) findViewById(R.id.textViewPauseTitle);
        editTextPause = (EditText) findViewById(R.id.editTextPause);
        editTextInterval = (EditText) findViewById(R.id.editTextInterval);

        button = (Button) findViewById(R.id.button);

        textViewPauseTitle.setEnabled(false);
        textViewPause.setEnabled(false);
        textViewTime.setEnabled(false);

        sPref = getPreferences(MODE_PRIVATE);
        int interval = sPref.getInt(INTERVAL, 0);
        int pause = sPref.getInt(PAUSE, 0);

        editTextInterval.setText(getDateFromMillis(interval * 1000));
        editTextPause.setText(getDateFromMillis(pause * 1000));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    public void onClickButton(View view) {
        inputLayoutPause.clearFocus();
        inputLayoutInterval.clearFocus();
        editTextPause.clearFocus();
        editTextInterval.clearFocus();

        String pause = editTextPause.getText().toString();
        String interval = editTextInterval.getText().toString();
        Integer p = getSecondFromDate(pause);
        Integer i = getSecondFromDate(interval);

        if (view.getTag().equals("stop")) {
            if (p == null) {
                inputLayoutPause.setError(getString(R.string.error_format));

                return;
            } else {
                inputLayoutPause.setError(null);
            }

            if (i == null) {
                inputLayoutInterval.setError(getString(R.string.error_format));

                return;
            } else {
                inputLayoutInterval.setError(null);
            }

            ((Button) view).setText(R.string.stop);
            view.setTag("start");

            textViewPauseTitle.setEnabled(true);
            textViewPause.setEnabled(true);
            textViewTime.setEnabled(false);
            editTextPause.setEnabled(false);
            editTextInterval.setEnabled(false);

            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask(p, i);

            mTimer.schedule(mMyTimerTask, 1000, 1000);

            sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();

            ed.putInt(PAUSE, p);
            ed.putInt(INTERVAL, i);

            ed.commit();
        } else {
            ((Button) view).setText(R.string.start);
            view.setTag("stop");

            textViewPauseTitle.setEnabled(true);
            textViewPause.setEnabled(true);
            textViewTime.setEnabled(false);
            editTextPause.setEnabled(true);
            editTextInterval.setEnabled(true);

            if (mTimer != null) {
                mTimer.cancel();

                if (p != null)
                    textViewPause.setText(getDateFromMillis(p * 1000));

                if (i != null)
                    textViewTime.setText(getDateFromMillis(i * 1000));
            }
        }
    }

    class MyTimerTask extends TimerTask {

        private int currentPause = 0;
        private int currentInterval = 0;

        private int pause;
        private int interval;

        private MediaPlayer mp;

        public MyTimerTask(int pause, int interval) {
            this.pause = pause;
            this.interval = interval;

            currentPause = pause;
            currentInterval = interval;

            mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        }

        @Override
        public void run() {
            if (currentPause > 0) {
                currentPause--;
            } else if (currentPause == 0) {
                if (currentInterval > 0) {
                    currentInterval--;
                } else if (currentInterval == 0) {
                    currentInterval = interval;
                    alarm();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentPause == 0 && textViewPause.isEnabled()) {
                        textViewPauseTitle.setEnabled(false);
                        textViewPause.setEnabled(false);
                        textViewTime.setEnabled(true);
                    }

                    textViewPause.setText(getDateFromMillis(currentPause * 1000));
                    textViewTime.setText(getDateFromMillis(currentInterval * 1000));
                }
            });
        }

        private void alarm() {
            mp.start();
        }
    }

    public Integer getSecondFromDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date d = formatter.parse(date);

            return (int) (d.getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getDateFromMillis(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
        return formatter.format(new Date(millis));
    }
}
