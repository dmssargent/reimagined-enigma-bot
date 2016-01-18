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

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ftccommunity.robotcore.R;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class AuthDialog extends DialogFragment {
    private String passcode;
    private TextView passcodeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container);
        getDialog().setTitle("Authentication");
        passcodeView = (TextView) view.findViewById(R.id.passcode);

        SecureRandom random = new SecureRandom();
        passcode = Integer.toString(Math.abs((random.nextInt() % 90000) + 10000));
        passcodeView.setText(passcode);
        return view;
    }

    public boolean correct(String data) {
        data = data.trim();
        try {
            data = Pattern.compile(".*\\d+").split(data)[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e("Auth Dialog:", "Pattern failed on " + data);
        }
        final boolean equals = data.equals(passcode);

        if (!equals) {
            passcodeView.post(new Runnable() {
                @Override
                public void run() {
                    passcodeView.setTextColor(Color.RED);
                }
            });
        } else {
            passcodeView.post(new Runnable() {
                @Override
                public void run() {
                    passcodeView.setTextColor(Color.GREEN);
                }
            });
            this.dismiss();
        }
        return equals;
    }
}
