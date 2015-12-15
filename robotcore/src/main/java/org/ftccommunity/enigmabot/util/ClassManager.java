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

import android.content.Context;
import android.util.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by David on 12/14/2015.
 */
public class ClassManager {
    private static final List<Class<?>> classes =
            Collections.synchronizedList(new ArrayList<Class<?>>());
    private final String TAG = "CLASSMGR:";
    private final List<Callback> callbacks;
    private final HashMultimap<Callback, Class<?>> callbackMap;
    private final Context context;

    private ClassManager(final Context context) {
        callbacks = Collections.synchronizedList(new LinkedList<Callback>());
        callbackMap = HashMultimap.create();
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
        ClassManager classManager = new ClassManager(ctx);
        classManager.computeList();
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
        this.classes.addAll(classesToProcess);
    }

    public interface Callback<A> {
        void receiveClasses(LinkedList<Class<?>> classes);
    }
}