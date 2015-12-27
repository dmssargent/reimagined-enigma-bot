package org.ftccommunity.enigmabot;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ViewLogsActivity extends AppCompatActivity {

    private ListeningExecutorService listeningExecutorService;
    private View logViewer;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_logs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingText = (TextView) findViewById(R.id.loadingText);
        try {
            for (String data : getAssets().list(".")) {
                Log.i("FONT", data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Typeface hack = Typeface.createFromAsset(getAssets(), "fonts/Hack-Regular.ttf");

        ((TextView) findViewById(R.id.logText)).setTypeface(hack);
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        updateLog();
    }

    private void updateLog() {
        final ListenableFuture<String> future = listeningExecutorService
                .submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        setLoadingText("Fetching log...");
                        Process logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
                        BufferedReader reader = new BufferedReader(new InputStreamReader(logcat.getInputStream()));
                        Thread.sleep(2000);

                        String returnData = "";
                        //if (logcat.waitFor() == 0) {
                        returnData = CharStreams.toString(reader);
                        //}

                        logcat.destroy();
                        return returnData;
                    }
                });

        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setPriority(3);
                    final Spannable result = highlight(future.get());
                    final TextView viewer = (TextView) findViewById(R.id.logText);
                    setLoadingText("Presenting the log");
                    viewer.post(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setText(result);
                            logViewer = findViewById(R.id.logViewer);
                            logViewer.setVisibility(View.VISIBLE);
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Log.e("ViewLogs", "Failed to get the logcats", e);
                }
            }
        }, listeningExecutorService);
    }

    private Spannable highlight(String string) {
        String[] entries = string.split("\\n");
        SpannableString spannableString = new SpannableString(string);

        int position = 0;
        for (String entry : entries) {
            setLoadingText("Parsing " + Math.round((position / (double) string.length()) * 100) + "%");
            int length = entry.length();
            if (entry.startsWith("W/")) {
                configureColor(spannableString, position, Color.YELLOW, position + length);
            } else if (entry.startsWith("E/")) {
                configureColor(spannableString, position, Color.RED, position + length);
            } else if (entry.startsWith("A/") || entry.startsWith("F/")) {
                configureColor(spannableString, position, Color.argb(0, 128, 0, 0), position + length);
            }

            position += length + 1;
        }

        return spannableString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_clear_log) {
            try {
                Process logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
                updateLog();
                Snackbar.make(logViewer,
                        "Clearing log...", Snackbar.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_save_log) {
            File file = new File(Environment.getExternalStorageDirectory() + "/enigma-bot/logs");
            if (file.mkdirs() || file.isDirectory()) {
            }
            String filename = file.toString() + "/" + System.currentTimeMillis() + ".log";
            try {

                Process logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-f",
                        filename});

            } catch (IOException e) {
                e.printStackTrace();
            }

            Snackbar.make(logViewer,
                    "Saving log to " + filename, Snackbar.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void configureColor(SpannableString spannableString, int position, int color, int end) {
        spannableString.setSpan(new ForegroundColorSpan(color)
                , position, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setLoadingText(final String message) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(8);
                loadingText.setText(message);
            }
        };
        for (int i = 0; !loadingText.post(action) && i < 5; i++) {
            Thread.yield();
        }
    }
}
