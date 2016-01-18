/*
 * Copyright 2016 David Sargent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ftccommunity.core;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.ftccommunity.collections.DeviceMap;
import org.ftccommunity.collections.DeviceMultiMap;
import org.ftccommunity.hardware.AccelerationSensor;
import org.ftccommunity.hardware.AnalogInput;
import org.ftccommunity.hardware.AnalogOutput;
import org.ftccommunity.hardware.ColorSensor;
import org.ftccommunity.hardware.CompassSensor;
import org.ftccommunity.hardware.DcMotor;
import org.ftccommunity.hardware.DcMotorController;
import org.ftccommunity.hardware.DeviceInterfaceModule;
import org.ftccommunity.hardware.DigitalChannel;
import org.ftccommunity.hardware.GyroSensor;
import org.ftccommunity.hardware.HardwareDevice;
import org.ftccommunity.hardware.I2cDevice;
import org.ftccommunity.hardware.IrSeekerSensor;
import org.ftccommunity.hardware.Led;
import org.ftccommunity.hardware.LegacyModule;
import org.ftccommunity.hardware.LightSensor;
import org.ftccommunity.hardware.OpticalDistanceSensor;
import org.ftccommunity.hardware.PwmOutput;
import org.ftccommunity.hardware.Servo;
import org.ftccommunity.hardware.ServoController;
import org.ftccommunity.hardware.TouchSensor;
import org.ftccommunity.hardware.TouchSensorMultiplexer;
import org.ftccommunity.hardware.UltrasonicSensor;
import org.ftccommunity.hardware.VoltageSensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David on 1/17/2016.
 */
public final class HardwareMapBuilder {
    private final static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final static Charset charset = Charset.forName("UTF-8");

    public static void writeToJson(File location, ExtensibleHardwareMap hardwareMap) throws FileNotFoundException {
        final DeviceMultiMap delegate = hardwareMap.delegate();
        LinkedList<HardwareDevice.DeviceInfo> info = new LinkedList<>();
        for (DeviceMap<? extends HardwareDevice> entry : delegate) {
            for (HardwareDevice device : entry) {
                info.add(device.deviceInfo());
            }
        }

        gson.toJson(info, Files.newWriter(location, charset));
    }

    public ExtensibleHardwareMap readFromJson(File location) throws FileNotFoundException {
        TypeToken<LinkedList<HardwareDevice.DeviceInfo>> deviceInfoType = new TypeToken<LinkedList<HardwareDevice.DeviceInfo>>() {
        };
        LinkedList<HardwareDevice.DeviceInfo> linkedList = gson.fromJson(Files.newReader(checkNotNull(location), charset), deviceInfoType.getType());
        MockHardwareMap mockHardwareMap = new MockHardwareMap();
        for (HardwareDevice.DeviceInfo info :
                linkedList) {
//            if (info.getType() == )
//            devices.put(info.name(), SimpleDag.<HardwareDevice>create(info.getType(), info));
        }

        return null;
    }

    class MockHardwareMap {
        public HashMap<String, DcMotorController> dcMotorController = new HashMap<>();
        public HashMap<String, DcMotor> dcMotor = new HashMap<>();
        public HashMap<String, ServoController> servoController = new HashMap<>();
        public HashMap<String, Servo> servo = new HashMap<>();
        public HashMap<String, LegacyModule> legacyModule = new HashMap<>();
        public HashMap<String, TouchSensorMultiplexer> touchSensorMultiplexer = new HashMap<>();
        public HashMap<String, DeviceInterfaceModule> deviceInterfaceModule = new HashMap<>();
        public HashMap<String, AnalogInput> analogInput = new HashMap<>();
        public HashMap<String, DigitalChannel> digitalChannel = new HashMap<>();
        public HashMap<String, OpticalDistanceSensor> opticalDistanceSensor = new HashMap<>();
        public HashMap<String, TouchSensor> touchSensor = new HashMap<>();
        public HashMap<String, PwmOutput> pwmOutput = new HashMap<>();
        public HashMap<String, I2cDevice> i2cDevice = new HashMap<>();
        public HashMap<String, AnalogOutput> analogOutput = new HashMap<>();
        public HashMap<String, ColorSensor> colorSensor = new HashMap<>();
        public HashMap<String, Led> led = new HashMap<>();
        public HashMap<String, AccelerationSensor> accelerationSensor = new HashMap<>();
        public HashMap<String, CompassSensor> compassSensor = new HashMap<>();
        public HashMap<String, GyroSensor> gyroSensor = new HashMap<>();
        public HashMap<String, IrSeekerSensor> irSeekerSensor = new HashMap<>();
        public HashMap<String, LightSensor> lightSensor = new HashMap<>();
        public HashMap<String, UltrasonicSensor> ultrasonicSensor = new HashMap<>();
        public HashMap<String, VoltageSensor> voltageSensor = new HashMap<>();
    }
}
