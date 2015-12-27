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

package org.ftccommunity.enigmabot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.net.InetAddresses;

import org.ftccommunity.enigmabot.util.Dimmer;
import org.ftccommunity.robotcore.RobotService;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

public class LaunchActivity extends AppCompatActivity {
    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RobotService.Messaging binder = (RobotService.Messaging) service;
            onServiceBind(binder.getService(), binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //controllerService = null;
        }
    };
    private Dimmer dimmer;
    private InetAddress currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View main = getWindow().getDecorView();
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        dimmer = new Dimmer(getWindow(), 5000, 50);

        // temp
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkInterface wlan0;
                try {
                    wlan0 = NetworkInterface.getByName("wlan0");
                    currentAddress = Collections.list(wlan0.getInetAddresses()).get(1);
                    final String ipAddress = InetAddresses.toAddrString(currentAddress);
                    Log.i("MAIN:", "IP Address: " + ipAddress);

                    // Objects must be held by a synchronized block before calling notify
                    synchronized (LaunchActivity.this) {
                        LaunchActivity.this.notifyAll();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        final TextView ipAddressLabel = (TextView) findViewById(R.id.IpAddressValue);

        while (currentAddress == null) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        ipAddressLabel.setText(InetAddresses.toAddrString(currentAddress));

        Intent intent = new Intent(this, RobotService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launch, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(connection);
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
        } else if (id == R.id.action_view_logs) {
            startActivity(new Intent(this, ViewLogsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void onServiceBind(RobotService service, RobotService.Messaging binder) {
        Log.i("MAIN:", "Bound to Robot Controller Service");
        binder.setContext(this);
        service.startServices();
        service.configureViews(
                (TextView) findViewById(R.id.status),
                (TextView) findViewById(R.id.status_desc),
                (TextView) findViewById(R.id.gamepad1),
                (TextView) findViewById(R.id.gamePad2),
                (RelativeLayout) findViewById(R.id.robot_container)
        );

    }
}
