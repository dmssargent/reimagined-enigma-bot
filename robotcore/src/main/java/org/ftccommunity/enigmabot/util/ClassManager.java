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

import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.util.Log;

import dalvik.system.DexFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David on 12/14/2015.
 */
public class ClassManager {
    private transient static final List<Class<?>> classes =
            Collections.synchronizedList(new ArrayList<Class<?>>());
    private transient static ClassManager instance;
    private final String TAG = "CLASSMGR:";
    private final Context context;

    private ClassManager(final Context context) {
        this.context = context;
    }

    public static List<Class<?>> getClasses() {
        if (classes.isEmpty()) {
            throw new IllegalStateException("Class Mgr doesn't know anything yet; did you" +
                    "call create()?");
        }

        return ImmutableList.copyOf(classes);
    }

    public static ClassManager create(final Context ctx) {
        // Are we already finished?
        if (instance != null) {
            return instance;
        }
        ClassManager classManager = new ClassManager(ctx);
        classManager.computeList();
        instance = classManager;
        return classManager;
    }

    private void computeList() {
        // Try to load the Dex-File
        final DexFile df;
        try {
            df = new DexFile(context.getPackageCodePath());
        } catch (IOException e) {
            Log.wtf(TAG, e);
            throw new NullPointerException();
        }

        // Migrate the enum to OpMode LinkedList
        LinkedList<String> classes = new LinkedList<>(Collections.list(df.entries()));

        // Create the container for the objects to add
        LinkedList<Class<?>> classesToProcess = new LinkedList<>();
        final ClassLoader classLoader = context.getClassLoader();
        for (String klazz : classes) {
            try {
                classesToProcess.add(Class.forName(klazz, false, classLoader));
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                Log.e(TAG, "Cannot add class to process list: " + klazz + "Reason: " + e.toString());

//                // Add code to prevent loading the next class
//                if (klazz.contains("$")) {
//                    klazz = klazz.substring(0, klazz.indexOf("$") - 1);
//                }
            }
        }

        // Add the classes found to the main class
        ClassManager.classes.addAll(classesToProcess);
    }

    public interface Callback<A> {
        void receiveClasses(LinkedList<Class<?>> classes);
    }
}
