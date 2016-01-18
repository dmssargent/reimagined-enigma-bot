/*
 * Copyright © 2016 David Sargent
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM,OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.ftccommunity.robotcore.handler;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;

public class RobotUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final String EXTRA_EMEG_DUMP = "EMEG_DUMP";
    public static Context context;
    private final int delay;

    public RobotUncaughtExceptionHandler(Context ctx, int delay) {
        context = ctx;
        this.delay = delay;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        try {
            Log.wtf("CORE_CONTROLLER::", Throwables.getRootCause(exception));
            Log.i("CORE_CONTROLLER::", "Exception Details:", exception);
            try {
                Toast.makeText(context,
                        "An almost fatal exception occurred." + exception.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Log.i("CORE_CONTROLLER", ex.getLocalizedMessage());
            }

            final Intent restartIntent = new Intent(((Activity) context).getIntent());
            final Crash value = new Crash("Enigma Error", exception, CrashType.EMERGENCY);
            final File dumpDir = new File(Environment.getExternalStorageDirectory().getPath() + "/enigma/dumps/");
            if ((dumpDir.exists() && dumpDir.isDirectory() || dumpDir.mkdirs())) {
                final File dumpFile = new File(Environment.getExternalStorageDirectory().getPath() + "/enigma/dumps/" + System.currentTimeMillis() + ".dmp");
                final Charset charset = Charset.forName("UTF-8");
                Log.i("CRASH_HANDLER", "Writing info at " + dumpFile);
                final BufferedWriter writer = Files.newWriter(dumpFile, charset);
                writer.write(new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create().toJson(value));
                writer.close();
            }

            restartIntent.putExtra(EXTRA_EMEG_DUMP, value);
            PendingIntent intent = PendingIntent.getActivity(context, 0,
                    restartIntent, ((Activity) context).getIntent().getFlags());
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
            System.exit(2);
        } catch (Exception e) {
            Log.wtf("CORE_CONTROLLER::", e);
        }
    }

    public static class Crash implements Serializable {
        private Throwable throwable;
        private long timestamp;
        private String message;
        private CrashType crashType;
        private Throwable cause;
        private String stackTrace;

        protected Crash() {

        }

        public Crash(final String message, final Throwable throwable, final CrashType type) {
            this.throwable = checkNotNull(throwable);
            stackTrace = Throwables.getStackTraceAsString(throwable);
            crashType = checkNotNull(type);
            cause = Throwables.getRootCause(throwable);
            this.message = message;
            timestamp = System.currentTimeMillis();
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public CrashType getCrashType() {
            return crashType;
        }

        public Throwable getCause() {
            return cause;
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}
