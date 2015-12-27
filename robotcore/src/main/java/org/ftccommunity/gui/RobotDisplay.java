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

package org.ftccommunity.gui;

import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David on 12/18/2015.
 */
public class RobotDisplay {
    public static final String RUNNING = "RUNNING";
    public static final String READY = "READY";
    public static final String STARTING = "STARTING";
    public static final String FAILED = "FAILED";
    public static final String STOPPING = "STOPPING";
    private TextView robotStatus;
    private TextView details;
    private TextView gamepad1;
    private TextView gamepad2;
    private RelativeLayout robotContainer;
    private LinkedList<View> backupForRobotContainer;

    private String currentRobotStatus;
    private String currentRobotDetails;

    public RobotDisplay() {
        this.currentRobotDetails = "Loading...";
        this.currentRobotStatus = "Init";
        backupForRobotContainer = new LinkedList<>();
    }

    private static void setMessage(final String message, final ErrorLevels errorLevel, final TextView details) {
        if (details != null) {
            details.post(new Runnable() {
                @Override
                public void run() {
                    details.setText(message);
                    inferViewColoring(details, errorLevel, message);
                }
            });
        }
    }

    private static void inferViewColoring(TextView view, ErrorLevels errorLevel, String details) {
        if (errorLevel.equals(ErrorLevels.AUTO)) {
            if (details.toUpperCase().contains(" WARN")) {
                view.setTextColor(Color.YELLOW);
            } else if (details.toUpperCase().contains(" ERR")) {
                view.setTextColor(Color.RED);
            } else {
                view.setTextColor(Color.WHITE);
            }
        } else {
            switch (errorLevel) {
                case WARNING:
                    view.setTextColor(Color.YELLOW);
                    break;
                case ERROR:
                    view.setTextColor(Color.RED);
                    break;
                default:
                    view.setTextColor(Color.WHITE);
                    break;
            }
        }
    }

    public void configureViews(TextView status, TextView details, TextView gamepad1,
                               TextView gamepad2, RelativeLayout robotContainer) {
        this.robotStatus = checkNotNull(status);
        this.details = checkNotNull(details);
        this.gamepad1 = checkNotNull(gamepad1);
        this.gamepad2 = checkNotNull(gamepad2);
        this.robotContainer = checkNotNull(robotContainer);

        final int childCount = robotContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            backupForRobotContainer.add(robotContainer.getChildAt(i));
        }
    }

    public boolean configureForUserHandoff() {
        return robotContainer.post(new Runnable() {
            @Override
            public void run() {
                robotContainer.removeAllViews();
            }
        });
    }

    public boolean reconfigureForController() {
        return robotContainer.post(new Runnable() {
            @Override
            public void run() {
                for (View view : backupForRobotContainer) {
                    robotContainer.addView(view);
                }
            }
        });
    }

    public synchronized void setStatus(String status, final ErrorLevels errorLevel) {
        this.currentRobotStatus = status;
        setMessage(status, errorLevel, this.robotStatus);

    }

    public synchronized void setDetails(final String details, final ErrorLevels errorLevel) {
        this.currentRobotDetails = details;
        setMessage(details, errorLevel, this.details);
    }

    public synchronized void gamepad1(final Object object) {
        setMessage(object.toString(), ErrorLevels.INFO, gamepad1);
    }

    public synchronized void gamepad2(final Object object) {
        setMessage(object.toString(), ErrorLevels.INFO, gamepad2);
    }

    public enum ErrorLevels {
        AUTO, INFO, WARNING, ERROR
    }
}
