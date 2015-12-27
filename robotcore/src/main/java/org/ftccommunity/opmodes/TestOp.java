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

package org.ftccommunity.opmodes;


import org.ftccommunity.annonations.Inject;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.opmodes.dagger.Gamepad;
import org.ftccommunity.opmodes.dagger.HardwareMap;

/**
 * Created by David on 12/19/2015.
 */
public class TestOp implements AbstractOpMode {
    private Gamepad gamepadA;
    private Gamepad gamepadB;

    @Inject
    public TestOp(Gamepad gamepad1, @Named("gamepad2") Gamepad gamepad2, HardwareMap hardwareMap) {
        gamepadA = gamepad1;
        gamepadB = gamepad2;
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
