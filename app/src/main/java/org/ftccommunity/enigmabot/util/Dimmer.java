/*
 * Copyright 2015 David Sargent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ftccommunity.enigmabot.util;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by David on 12/13/2015.
 */
public class Dimmer implements Runnable, View.OnTouchListener {
    private final String TAG = "DIMMER:";
    private final float minBrightness = 0.05f;
    private final float restoreBrightness;
    private final Window window;
    private final Thread executingThread;
    private long delay;
    private int interval;
    private float currentBrightness;
    private volatile long sleepUntil = -1;

    public Dimmer(final Window window, long delay, int speed) {
        this.window = window;
        this.delay = delay;
        this.interval = speed;

        currentBrightness = window.getAttributes().screenBrightness;
        restoreBrightness = Math.abs(currentBrightness);

        // Setup
        executingThread = new Thread(this);
        executingThread.setName("Dimmer");
        window.getDecorView().setOnTouchListener(this);
        window.getDecorView().setKeepScreenOn(true);
        executingThread.start();
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public void run() {
        final long numOfCycles = delay / interval;
        int currentCycle = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (sleepUntil != -1) {
                try {
                    if (sleepUntil < 0) {
                        Log.d(TAG, "Supposed to be sleeping, but the sleep value is negative " +
                                sleepUntil);
                    } else {
                        Thread.sleep(sleepUntil);
                        // Reset after wakeup
                        sleepUntil = -1;
                        currentCycle = 0;
                        continue;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (currentCycle >= numOfCycles) {
                continue;
            }

            // Safe to continue
            synchronized (window) {
                final WindowManager.LayoutParams layoutParams = window.getAttributes();
//                layoutParams.screenBrightness = (float)
//                        (restoreBrightness * (1 / 0.89) * (Math.tanh((numOfCycles - currentCycle) / numOfCycles) + 1) / 2);
                layoutParams.screenBrightness = (restoreBrightness) * (numOfCycles - currentCycle) / numOfCycles;

                if (layoutParams.screenBrightness < minBrightness) {
                    continue;
                }
                window.getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Setting dimmer to " + layoutParams.screenBrightness);
                        window.setAttributes(layoutParams);
                    }
                });

            }
            currentCycle++;
        }

        Log.e(TAG, "Exiting Dimmer Thread");
    }

    public void sleepDimmer(long delay) {
        sleepUntil = delay;
        synchronized (window) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = restoreBrightness;
            window.setAttributes(layoutParams);
        }
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        Log.d(TAG, "Touch Caught, sleeping dimmer");
        sleepDimmer(10000);

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }, 500);

        return false;
    }
}
