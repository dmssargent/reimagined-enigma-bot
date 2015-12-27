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

package org.ftccommunity;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.ftccommunity.annonations.Component;
import org.ftccommunity.annonations.Inject;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.Provides;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleDag {
    private static final String TAG = "DI";

    public static <T> T create(Class<T> tClass, @Nullable Object refer) {
        if (isNull(refer)) {
            refer = new SimpleDag();
        }

        List<Constructor<T>> constructors = constructors(tClass);
        List<Field> fields = getFields(tClass);
        T instance = null;

        // Check if we are already done
        if (constructors.isEmpty() && fields.isEmpty()) {
            instance = getSimpleInstance(tClass);
        }

        // Fulfill all of the components
        List<Method> providers = new ArrayList<>();
        HashMap<Method, Object> methodMap = new HashMap<>();
        createComponentInstances(getComponents(tClass), providers, methodMap, refer);

        if (isNull(instance)) {
            // We failed to give every value a name, lets try to create the object with respect to Nullable
            for (Constructor<T> constructor : constructors) {
                Class[] params = constructor.getParameterTypes();
                Annotation[][] annotations = constructor.getParameterAnnotations();
                LinkedList<Method> resolution = new LinkedList<>();
                for (int i = 0; i < params.length; i++) {
                    Method method = inferMethodForParameter(params[i], annotations[i], providers);
                    if (method != null) {
                        resolution.add(method);
                    } else {
                        for (Annotation annotation : annotations[i]) {
                            if (annotation instanceof Nullable) {
                                resolution.add(null);
                                break;
                            }
                        }
                    }
                }

                Log.d(TAG, "Resolution has size of " + resolution.size() + ", parameters has size of " + params.length);
                if (resolution.size() == params.length) { // Solution works
                    instance = createUsingSolution(methodMap, constructor, resolution);
                } // We either threw or returned with the generated resolution, if it worked
                // On to the next constructor
            }
        }

        if (isNull(instance)) {
            declareFailure(tClass, constructors, providers);
        }

        // Continue on to field loading
        bindFields(instance, fields, providers, methodMap);
        return instance;
    }

    @NonNull
    private static <T> T createUsingSolution(HashMap<Method, Object> methodMap, Constructor<T> constructor, LinkedList<Method> resolution) {
        T instance;
        try {
            List<Object> objs = new LinkedList<>();
            for (Method method : resolution) {
                Object holder = methodMap.get(method);
                Object result = invokeProvider(method, holder);
                objs.add(result);
            }
            instance = constructor.newInstance(objs.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new DagInstantiationException(ex.getMessage(), ex);
        }
        return instance;
    }

    private static void createComponentInstances(@NotNull List<Component> components,
                                                 @NotNull List<Method> providers,
                                                 @NotNull HashMap<Method, Object> methodMap,
                                                 @NotNull Object refer) {
        registerComponent(providers, methodMap, refer.getClass(), refer);
        for (Component component : components) {
            for (Class module :
                    component.modules()) {
                Object obj = create(module, refer);
                registerComponent(providers, methodMap, module, obj);
            }
        }
    }

    private static void registerComponent(@NotNull List<Method> providers,
                                          @NotNull HashMap<Method, Object> methodMap,
                                          @NotNull Class module, @NotNull Object obj) {
        for (Method method : module.getMethods()) {
            if (method.isAnnotationPresent(Provides.class)) {
                methodMap.put(method, obj);
                providers.add(method);
            }
        }
    }

    private static <T> T getSimpleInstance(Class<T> tClass) {
        T instance;
        try {
            instance = tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new DagInstantiationException(ex.getMessage(), ex);
        }
        return instance;
    }

    private static <T> void declareFailure(Class<T> tClass, List<Constructor<T>> constructors, List<Method> providers) {
        // Constructor info build
        String constructorInfo = "";
        for (Constructor constructor : constructors) {
            constructorInfo += "Constructor: ";
            Gson dump = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
            ConstructorInfoDump constructorInfoDump = new ConstructorInfoDump(constructor.getParameterTypes(), constructor.getParameterAnnotations());
            constructorInfo += dump.toJson(constructorInfoDump);
        }
        final String message = "Cannot figure out how to load " + tClass.getSimpleName() + ".\n"
                + constructors.size() + " constructors were found.\n" + constructorInfo + " I have " +
                providers.size() + " providers available";
        throw new DagInstantiationException(message);
    }

    private static <T> void bindFields(T instance, List<Field> fields, List<Method> providers,
                                       HashMap<Method, Object> methodMap)
            throws DagInstantiationException {
        for (Field field : fields) {
            Class type = field.getType();
            final Method method = inferMethodForParameter(type, field.getAnnotations(), providers);
            if (!isNull(method)) {
                boolean wasAccessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    field.set(instance, invokeProvider(method, methodMap.get(method)));
                } catch (IllegalAccessException ex) {
                    throw new DagInstantiationException(ex.getMessage(), ex);
                }
                field.setAccessible(wasAccessible);
            }
        }
    }

    private static Object invokeProvider(Method method, Object obj) throws DagInstantiationException {
        try {
            return method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new DagInstantiationException(ex.getMessage(), ex);
        }
    }

    private static <T> boolean isNull(T instance) {
        return instance == null;
    }

    private static List<Component> getComponents(Class tClass) {
        ArrayList<Component> components = new ArrayList<>();
        for (Class _interface : tClass.getInterfaces()) {
            if (_interface.isAnnotationPresent(Component.class)) {
                components.add((Component) _interface.getAnnotation(Component.class));
            }
        }

        return components;
    }

    private static <T> List<Constructor<T>> constructors(Class<T> tClass) {
        ArrayList<Constructor<T>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : tClass.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                try {
                    constructors.add((Constructor<T>) constructor);
                } catch (ClassCastException ignored) {
                }
            }
        }
        return constructors;
    }

    private static List<Field> getFields(Class tClass) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field :
                tClass.getFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                fields.add(field);
            }
        }

        return fields;
    }

    @Nullable
    private static Method inferMethodForParameter(Class param, Annotation[] annotations, List<Method> providers) {
        for (Method method : providers) {
            boolean correctMethod = false;
            if (!method.getReturnType().equals(param)) {
                continue;
            }

            // Found a possible provider
            // Check for Named annotations
            boolean hasNamedAnnotation = false;
            for (Annotation annotation : annotations) {
                if ((annotation instanceof Named)) {
                    hasNamedAnnotation = true;
                    if (method.isAnnotationPresent(Named.class)) {
                        if (((Named) annotation).value().equals(method.getAnnotation(Named.class).value())) {
                            // We found a valid provider for one parameter
                            correctMethod = true;
                            break;
                        }
                    }
                }
            }
            if (!hasNamedAnnotation) {
                correctMethod = true;
            }

            if (correctMethod) {
                return method;
            }
        }

        return null;
    }

    public static boolean hasMethodAnnotationsOf(Class<? extends Annotation> annotation, Class<?> forThis) {
        for (Method method : forThis.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }

        return false;
    }

    public static class DagInstantiationException extends RuntimeException {
        public DagInstantiationException(String message) {
            super(message);
        }

        public DagInstantiationException(String message, Throwable initCause) {
            super(message, initCause);
        }
    }

    private static class ConstructorInfoDump {
        public ParameterInfo[] parameterInfos;

        private ConstructorInfoDump(Class[] parameters, Annotation[][] annotations) {
            parameterInfos = new ParameterInfo[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                ParameterInfo info = new ParameterInfo();
                info.className = parameters[i].getSimpleName();
                info.annotations = new String[annotations[i].length];
                for (int j = 0; j < annotations[i].length; j++) {
                    info.annotations[j] = annotations[i][j].annotationType().getSimpleName();
                }
                parameterInfos[i] = info;
            }
        }

        private class ParameterInfo {
            String className;
            String[] annotations;
        }
    }
}
