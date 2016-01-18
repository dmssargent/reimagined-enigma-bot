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

package org.ftccommunity.services;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import org.ftccommunity.annonations.Inject;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.RobotService;
import org.ftccommunity.opmodes.AbstractOpMode;
import org.ftccommunity.opmodes.TestOp;
import org.ftccommunity.opmodes.dagger.HardwareMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@RobotService
public class OpModeManager extends AbstractExecutionThreadService {
    private static final String STOP_OP_MODE = "Stop Robot";
    Class<?> test = TestOp.class;
    List<Callable<Object>> callables;
    private LinkedHashMap<String, Class<?>> opModeMap;
    private ExecutorService executorService;
    private Queue messageQueue;
    @Inject
    @Named(org.ftccommunity.robotcore.RobotService.ROBOT_GENERAL)
    private EventBus robotGeneral;

    public OpModeManager() {
        opModeMap = new LinkedHashMap<>();
        executorService = Executors.newSingleThreadExecutor();
        messageQueue = new LinkedBlockingQueue();
    }

    public void startUp() {
        robotGeneral.post(this);
    }

    /**
     * Run the service. This method is invoked on the execution thread. Implementations must respond
     * to stop requests. You could poll for lifecycle changes in a work loop:
     * <pre>
     *   public void run() {
     *     while ({@link #isRunning()}) {
     *       // perform a unit of work
     *     }
     *   }
     * </pre>
     * ...or you could respond to stop requests by implementing {@link #triggerShutdown()}, which
     * should cause {@link #run()} to return.
     */
    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            while (messageQueue.size() > 0) {
                messageQueue.poll();
            }

//            if (callables == null) {
//                switch (currentRobotState) {
//                    case LOOP:
//                        callables = new LinkedList<>();
//                        callables.add(new Callable<Object>() {
//                            @Override
//                            public Object call() throws Exception {
//                                running.loop();
//                                return null;
//                            }
//                        });
//                        break;
//                    default:
//                        callables = new LinkedList<>();
//                }
//            }
//            if (robotFuture != null) {
//                if (robotFuture.isDone()) {
//                    robotFuture = executorService.submit(new Callable<Throwable>() {
//                        @Override
//                        public Throwable call() throws Exception {
//                            try {
//                                running.loop();
//                                return null;
//                            } catch (Exception ex) {
//                                return ex;
//                            }
//                        }
//                    });
//                } else {
//                    try {
//                        Throwable exception = robotFuture.get(1, TimeUnit.SECONDS);
//                        if (exception != null) {
//                            RobotState previousState = currentRobotState;
//                            currentRobotState = RobotState.FAILED;
//                            Log.e(running.getClass().getSimpleName(), "Failed in State " + previousState.toString(), exception);
//                        }
//                    } catch (TimeoutException ex) {
//                        robotFuture.cancel(true);
//                    }
//                }
//            } else {
//                robotFuture = executorService.submit(new Callable<Throwable>() {
//                    @Override
//                    public Throwable call() throws Exception {
//                        try {
//                            running.loop();
//                            return null;
//                        } catch (Exception ex) {
//                            return ex;
//                        }
//                    }
//                });
//            }
//
//        }
        }
    }

    private void changeOpMode(Class<AbstractOpMode> opModeClass) {
//        currentRunningOpMode = opModeClass.getSimpleName();
//        running = SimpleDag.create(opModeClass, this);
    }

    private static class StopRobot implements AbstractOpMode {
        private HardwareMap hardwareMap;

        @Inject
        public StopRobot(HardwareMap hardwareMap) {
            this.hardwareMap = hardwareMap;
        }

        @Override
        public void init() {

        }

        @Override
        public void initLoop() {

        }

        @Override
        public void start() {

        }

        @Override
        public void loop() {

        }

        @Override
        public void stop() {

        }
    }
}
