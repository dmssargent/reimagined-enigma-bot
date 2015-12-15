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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ServiceManager;

import org.ftccommunity.enigmabot.util.ClassManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by David on 12/14/2015.
 */
public class RobotService extends Service {
    private static final String TAG = "ROBOT_SERVICE:";
    private final Messaging message = new Messaging(this);

    private ServiceManager serviceManager;

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
        try {
            ClassManager.create(this);
            if (serviceManager == null) {
                serviceManager = generateServiceManager();
                serviceManager.startAsync();
            }
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
        serviceManager.stopAsync();
        serviceManager.awaitStopped();
        Log.i(TAG, "Fully stopped Robot Service");
        return false;
    }

    private ServiceManager generateServiceManager() {
        List<Class<?>> classList = ClassManager.getClasses();
        LinkedList<com.google.common.util.concurrent.Service> services = new LinkedList<>();

        for (Class<?> klazz : classList) {
            if (klazz.isAnnotationPresent(org.ftccommunity.annonations.RobotService.class)) {
                try {
                    services.add(getServiceInstance((Class<com.google.common.util.concurrent.Service>) klazz));
                    Log.d(TAG, "Loaded service: " + klazz.getSimpleName());
                } catch (InstantiationException | ClassCastException ex) {
                    Log.e(TAG, "Cannot load service " + klazz.getSimpleName() + ", due to exception", ex);
                }
            }
        }

        Log.d(TAG, services.size() + " service(s) have been discovered");
        return new ServiceManager(services);
    }

    private com.google.common.util.concurrent.Service getServiceInstance(
            Class<com.google.common.util.concurrent.Service> klazz) throws InstantiationException {
        ArrayList<Throwable> throwables = new ArrayList<>();
        try {
            Constructor constructor = klazz.getConstructor(Context.class);
            return (com.google.common.util.concurrent.Service) constructor.newInstance(this);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throwables.add(ex);
        }

        try {
            return klazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throwables.add(ex);
        }

        // Nothing good happened if we are here
        InstantiationException instantiationException = new InstantiationException(klazz.getSimpleName() +
                " couldn't be instantiated, due to errors.");
        instantiationException.initCause(throwables.get(1));
        instantiationException.addSuppressed(throwables.get(0));

        List<StackTraceElement> stackTraceElements = Throwables.lazyStackTrace(instantiationException);
        instantiationException.setStackTrace(stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]));
        throw instantiationException;
    }

    public class Messaging extends Binder {
        private RobotService service;

        public Messaging(@NotNull RobotService service) {
            super();
            this.service = service;
        }

        public RobotService getService() {
            return service;
        }
    }

   /* @Module
    private class RobotModule  {
        private final Context ctx;

        RobotModule(final Context context) {
            this.ctx = context;
        }

        @Provides
        Context provideContext() {
            return ctx;
        }
    }

    @Component(modules = RobotModule.class)
    private interface Service {
        com.google.common.util.concurrent.Service service();
    }*/
}
