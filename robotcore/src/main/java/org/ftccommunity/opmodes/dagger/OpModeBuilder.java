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

package org.ftccommunity.opmodes.dagger;

import android.content.Context;
import android.widget.RelativeLayout;

import org.ftccommunity.annonations.Module;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.Provides;


/**
 * Created by David on 12/18/2015.
 */
@Module
public class OpModeBuilder {
    @Provides
    public Context provideContext() {
        return null;
    }

    @Provides
    @Named("robotContainer")
    public RelativeLayout provideRobotContainer() {
        return null;
    }

    @Provides
    public Gamepad provideGamepad1() {
        return null;
    }

    @Provides
    @Named("gamepad2")
    public Gamepad provideGamepad2() {
        return null;
    }

    @Provides
    public HardwareMap provideHardwareMap() {
        return null;
    }
}
