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

package org.ftccommunity.robotcore;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ServiceManager;

import org.ftccommunity.SimpleDag;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.Provides;
import org.ftccommunity.enigmabot.util.ClassManager;
import org.ftccommunity.gui.RobotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public class RobotService extends Service {
    public final static String ROBOT_GENERAL = "ROBOT_GENERAL";
    private static final String TAG = "ROBOT_SERVICE:";
    private final Messaging message = new Messaging(this);

    private ServiceManager serviceManager;
    private WiFiP2pRobotManager wiFiP2pRobotManager;
    private RobotDisplay robotDisplay;
    private HashMap<String, Object> objectMap;
    private EventBus bus;


    public RobotService() {
        robotDisplay = new RobotDisplay();
        objectMap = new HashMap<>();
        bus = new EventBus(ROBOT_GENERAL);
    }


    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Starting Robot Service...");
        robotDisplay.setStatus(RobotDisplay.STARTING, RobotDisplay.ErrorLevels.INFO);
        robotDisplay.setDetails("Starting Robot Service...", RobotDisplay.ErrorLevels.INFO);
        try {
            ClassManager.create(this);
            wiFiP2pRobotManager = new WiFiP2pRobotManager();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            registerReceiver(wiFiP2pRobotManager, filter);
            return message;
        } catch (Exception ex) {
            Log.wtf(TAG, "Something really bad happened starting the main service", ex);
            Log.w(TAG, "Cowardly refusing to start the main service. Sorry for any inconvenience.");
            Throwables.propagateIfPossible(ex);
        }

        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Stopping Robot Service...");
        try {
            serviceManager.stopAsync();
        } catch (NullPointerException ignored) {
        }

        unregisterReceiver(wiFiP2pRobotManager);
        try {
            serviceManager.awaitStopped(1, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            Log.e(TAG, "Not all registered services stopped!", ex);
        }
        Log.i(TAG, "Fully stopped Robot Service");
        return false;
    }

    public void startServices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    robotDisplay.setDetails("Configuring Services...", RobotDisplay.ErrorLevels.INFO);
                    if (serviceManager == null) {
                        serviceManager = generateServiceManager();
                        serviceManager.startAsync();
                    }
                } catch (Exception ex) {
                    Log.wtf(TAG, "Something really bad happened starting the main service", ex);
                    Log.w(TAG, "Cowardly refusing to start the main service. Sorry for any inconvenience.");
                    Throwables.propagateIfPossible(ex);
                }

                try {
                    serviceManager.awaitHealthy();
                } catch (IllegalStateException ex) {
                    // Handled by Service Manager
//                    Log.e(TAG, "Some services failed to start");
//                    ImmutableCollection<com.google.common.util.concurrent.Service> failures =
//                            serviceManager.servicesByState().get(com.google.common.util.concurrent.Service.State.FAILED);
//                    for (com.google.common.util.concurrent.Service failure : failures) {
//                        Log.w(TAG, "Service " + failure.getClass().getSimpleName() + " failure to start", failure.failureCause());
//                    }
                }

                robotDisplay.setStatus(RobotDisplay.READY, RobotDisplay.ErrorLevels.INFO);
                robotDisplay.setDetails("Waiting patiently on user...", RobotDisplay.ErrorLevels.INFO);
                robotDisplay.configureForUserHandoff();
            }
        }).start();
    }

    private ServiceManager generateServiceManager() {
        List<Class<?>> classList = ClassManager.getClasses();
        LinkedList<com.google.common.util.concurrent.Service> services = new LinkedList<>();

        for (Class<?> klazz : classList) {
            if (klazz.isAnnotationPresent(org.ftccommunity.annonations.RobotService.class)) {
                try {
                    final com.google.common.util.concurrent.Service serviceInstance =
                            //getServiceInstance((Class<com.google.common.util.concurrent.Service>) klazz);
                            (com.google.common.util.concurrent.Service) SimpleDag.create(klazz, this);
                    final String value = klazz.getAnnotation(org.ftccommunity.annonations.RobotService.class).value();
                    bind(value.equals("") ? klazz.getSimpleName() : value, serviceInstance);
                    SimpleDag.hasMethodAnnotationsOf(Subscribe.class, serviceInstance.getClass());
                    new EventBus().register(serviceInstance);
                    services.add(serviceInstance);
                    Log.d(TAG, "Loaded service: " + klazz.getSimpleName());
                } catch (SimpleDag.DagInstantiationException | ClassCastException ex) {
                    Log.e(TAG, "Cannot load service " + klazz.getSimpleName() + ", due to exception", ex);
                }
            }
        }

        Log.d(TAG, services.size() + " service(s) have been discovered");
        return new ServiceManager(services);
    }

    @Provides
    @Named("DeviceName")
    public String provideDeviceName() {
        return wiFiP2pRobotManager.getDeviceName();
    }

    @Provides
    @Named(ROBOT_GENERAL)
    public EventBus provideEventBus() {
        return bus;
    }

    @Provides
    public Context provideAppContext() {
        return message.getAppContext();
    }

//    private com.google.common.util.concurrent.Service getServiceInstance(
//            Class<com.google.common.util.concurrent.Service> klazz) throws InstantiationException {
//        ArrayList<Throwable> throwables = new ArrayList<>();
//        com.google.common.util.concurrent.Service service = null;
//        try {
//            Constructor constructor = klazz.getConstructor(Context.class);
//
//            service = (com.google.common.util.concurrent.Service) constructor.newInstance(message.getAppContext());
//
//        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
//            throwables.add(ex);
//        }
//
//        try {
//            service = klazz.newInstance();
//        } catch (InstantiationException | IllegalAccessException ex) {
//            throwables.add(ex);
//        }
//
//
//        objectMap.put("DeviceName", wiFiP2pRobotManager.getDeviceName());
//        objectMap.put(bus.identifier(), bus);
//        for (Field field : klazz.getDeclaredFields()) {
//            if (field.isAnnotationPresent(Inject.class)) {
//                boolean wasAccessible = field.isAccessible();
//                field.setAccessible(true);
//                try {
//                    if (field.isAnnotationPresent(Named.class)) {
//                        Named name = field.getAnnotation(Named.class);
//                        if (objectMap.containsKey(name.value())) {
//                            field.set(service, objectMap.get(name.value()));
//                        } else {
//                            field.set(service, null);
//                        }
//                    }
//                } catch (IllegalAccessException e) {
//                    Log.e(TAG, "Wanted to inject on field " + field.getName() + ", but this failed");
//                }
//                field.setAccessible(wasAccessible);
//            }
//        }

//        if (service != null) {
//            bus.register(service);
//            return service;
//        }
//
//        // Nothing good happened if we are here
//        InstantiationException instantiationException = new InstantiationException(klazz.getSimpleName() +
//                " couldn't be instantiated, due to errors.");
//        instantiationException.initCause(throwables.get(1));
//        instantiationException.addSuppressed(throwables.get(0));
//
//        List<StackTraceElement> stackTraceElements = Throwables.lazyStackTrace(instantiationException);
//        instantiationException.setStackTrace(stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]));
//        throw instantiationException;
//    }

    public void bind(String name, Object value) {
        checkArgument(!Strings.isNullOrEmpty(name), "Name cannot be empty or null");
        checkArgument(!objectMap.containsKey(name), "Name is already associated");
        objectMap.put(name, checkNotNull(value));
    }

    public void configureViews(TextView status, TextView details, TextView gamepad1,
                               TextView gamepad2, RelativeLayout robotContainer) {
        robotDisplay.configureViews(status, details, gamepad1, gamepad2, robotContainer);
    }

    public class Messaging extends Binder {
        private RobotService service;
        private Context appContext;

        public Messaging(@NotNull RobotService service) {
            super();
            this.service = service;
        }

        @NotNull
        public RobotService getService() {
            return service;
        }

        public void setContext(@NotNull final Context context) {
            checkArgument(checkNotNull(context) instanceof Activity);
            this.appContext = context;
        }

        @NotNull
        public Context getAppContext() {
            checkState(appContext != null, "App Context hasn't be set yet.");
            final Activity activity = (Activity) this.appContext;
            activity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    activity.setTitle(wiFiP2pRobotManager.getDeviceName() + " Robot Controller");
                }
            });
            return this.appContext;
        }
    }
}
