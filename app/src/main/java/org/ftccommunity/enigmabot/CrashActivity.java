package org.ftccommunity.enigmabot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ftccommunity.robotcore.handler.RobotUncaughtExceptionHandler;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class CrashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Serializable serializableExtra = getIntent().getSerializableExtra(RobotUncaughtExceptionHandler.EXTRA_EMEG_DUMP);
        if (serializableExtra != null) {
            RobotUncaughtExceptionHandler.Crash crash = (RobotUncaughtExceptionHandler.Crash) serializableExtra;
            findOutError(crash);
            findOutParameters(crash);
            findOutStackTrace(crash);
        } else {
            continueOn();
        }

        setupAutoContinue();
    }

    private void setupAutoContinue() {
        final TextView timerView = (TextView) findViewById(R.id.crash_counter_label);
        final Timer timer = new Timer("Continue to Launch");
        timer.scheduleAtFixedRate(new TimerTask() {
            int iterations = 10;

            @Override
            public void run() {
                timerView.post(new Runnable() {
                    @Override
                    public void run() {
                        timerView.setText(String.format("Continuing in %d seconds...", iterations));
                    }
                });
                iterations--;
                if (iterations == 0) {
                    continueOn();
                }
            }
        }, 0, 1000);

        Button stay = (Button) findViewById(R.id.crash_button_stay);
        stay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                timerView.setText("");
            }
        });

        Button continueButton = (Button) findViewById(R.id.crash_button_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueOn();
            }
        });
    }

    private void findOutStackTrace(RobotUncaughtExceptionHandler.Crash crash) {
        if (crash.getStackTrace() != null) {
            TextView details = (TextView) findViewById(R.id.crash_details);
            details.setText(crash.getStackTrace());
        }
    }

    private void findOutError(RobotUncaughtExceptionHandler.Crash crash) {
        ((TextView) findViewById(R.id.crash_error)).setText(crash.getThrowable().getClass().getSimpleName());
    }

    private void findOutParameters(RobotUncaughtExceptionHandler.Crash crash) {
        TextView parameters = (TextView) findViewById(R.id.crash_params);
        String paramMessage = "Parameters: ";
        if (crash.getCause() != null) {
            paramMessage += crash.getCause().getClass().getSimpleName();
        } else {
            paramMessage += "NONE";
        }
        paramMessage += " ";
        if (crash.getThrowable().getMessage() != null) {
            paramMessage += crash.getThrowable().getMessage();
        } else {
            paramMessage += "NOTHING";
        }
        parameters.setText(paramMessage);
    }

    private void continueOn() {
        startActivity(new Intent(CrashActivity.this, LaunchActivity.class));
    }
}
