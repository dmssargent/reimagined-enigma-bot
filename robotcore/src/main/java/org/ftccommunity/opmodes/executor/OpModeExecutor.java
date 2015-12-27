package org.ftccommunity.opmodes.executor;

import com.google.common.util.concurrent.ListenableFuture;

import org.ftccommunity.opmodes.AbstractOpMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David on 12/20/2015.
 */
public class OpModeExecutor {
    AbstractOpMode running;
    String currentRunningOpMode;
    RobotState currentRobotState;
    ListenableFuture<Throwable> robotFuture;
    Throwable result;
    private ExecutorService executorService;

    public OpModeExecutor(AbstractOpMode initOpMode) {
        executorService = Executors.newSingleThreadExecutor();
        running = checkNotNull(initOpMode);
        getOpModeName();
    }

    public String getOpModeName() {
        if (currentRunningOpMode == null) {
            currentRunningOpMode = running.getClass().getSimpleName();
        }

        return currentRunningOpMode;
    }

    private enum RobotState {
        PENDING, INIT, INIT_LOOP, START, LOOP, STOP, FAILED
    }
}
