/*
 * Copyright © 2016 David Sargent
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM,OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.ftccommunity.core;

import com.google.common.base.Strings;

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
import org.ftccommunity.opmodes.dagger.HardwareMap;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The new version of the hardware map that provides a safer use for the hardware map. This prevents
 * client code from unintentionally modifying the devices available for use
 *
 * @author David Sargent
 * @since 0.1
 */

@SuppressWarnings("unused")
public class ExtensibleHardwareMap implements HardwareMap {
    private HardwareMapBuilder.MockHardwareMap basicMap;
    private DeviceMultiMap fullMap;

    /**
     * Builds the base ExtensibleHardwareMap; need to complete setup afterwards, specifically
     * calling {@link ExtensibleHardwareMap#createDeviceMaps()}
     */

    private ExtensibleHardwareMap() {
        fullMap = new DeviceMultiMap();
    }

    /**
     * Builds a new {@code ExtensibleHardwareMap} for use
     *
     * @param hwMap the HardwareMap to base off of
     */

    public ExtensibleHardwareMap(@NotNull HardwareMap hwMap) {
        this();
        createDeviceMaps();
    }

    /**
     * Rebuilds the Extensible HardwareMap from scratch based on a given {@link HardwareMap}. This
     * modifies the reference to the maps within inside this HardwareMap, but not the reference to
     * the HardwareMap itself.
     *
     * @param hwMap a valid non-null {@code HardwareMap} that contains what this needs to build on
     */

    public void rebuild(@NotNull HardwareMapBuilder.MockHardwareMap hwMap) {
        basicMap = checkNotNull(hwMap);
        fullMap.clear();
        createDeviceMaps();
    }

    /**
     * Move the MocK Map to our {@link DeviceMap} for our internal use
     */
    private void createDeviceMaps() {
        fullMap.checkedPut(DcMotorController.class, new DeviceMap<>(basicMap.dcMotorController));
        fullMap.checkedPut(DcMotor.class, new DeviceMap<>(basicMap.dcMotor));
        fullMap.checkedPut(ServoController.class, new DeviceMap<>(basicMap.servoController));
        fullMap.checkedPut(Servo.class, new DeviceMap<>(basicMap.servo));
        fullMap.checkedPut(LegacyModule.class, new DeviceMap<>(basicMap.legacyModule));
        fullMap.checkedPut(TouchSensorMultiplexer.class, new DeviceMap<>(basicMap.touchSensorMultiplexer));
        fullMap.checkedPut(DeviceInterfaceModule.class, new DeviceMap<>(basicMap.deviceInterfaceModule));
        fullMap.checkedPut(AnalogInput.class, new DeviceMap<>(basicMap.analogInput));
        fullMap.checkedPut(DigitalChannel.class, new DeviceMap<>(basicMap.digitalChannel));
        fullMap.checkedPut(OpticalDistanceSensor.class, new DeviceMap<>(basicMap.opticalDistanceSensor));
        fullMap.checkedPut(TouchSensor.class, new DeviceMap<>(basicMap.touchSensor));
        fullMap.checkedPut(PwmOutput.class, new DeviceMap<>(basicMap.pwmOutput));

//        DeviceMap<I2cDevice> i2cDeviceDeviceMap = new DeviceMap<>(basicMap.i2cDevice);
//        HashMap<String, I2cDevice> i2cDeviceHashMap = new HashMap<>();
//        for (Map.Entry<String, I2cDevice> deviceEntry : i2cDeviceDeviceMap.entrySet()) {
//            i2cDeviceHashMap.put(deviceEntry.getKey(), new ForwardedI2cDevice(deviceEntry.getValue()));
//        }

        //fullMap.checkedPut(I2cDevice.class, new DeviceMap<I2cDevice>(i2cDeviceHashMap));
        fullMap.checkedPut(AnalogOutput.class, new DeviceMap<>(basicMap.analogOutput));
        fullMap.checkedPut(ColorSensor.class, new DeviceMap<>(basicMap.colorSensor));
        fullMap.checkedPut(Led.class, new DeviceMap<>(basicMap.led));
        fullMap.checkedPut(AccelerationSensor.class, new DeviceMap<>(basicMap.accelerationSensor));
        fullMap.checkedPut(CompassSensor.class, new DeviceMap<>(basicMap.compassSensor));
        fullMap.checkedPut(GyroSensor.class, new DeviceMap<>(basicMap.gyroSensor));
        fullMap.checkedPut(IrSeekerSensor.class, new DeviceMap<>(basicMap.irSeekerSensor));
        fullMap.checkedPut(LightSensor.class, new DeviceMap<>(basicMap.lightSensor));
        fullMap.checkedPut(UltrasonicSensor.class, new DeviceMap<>(basicMap.ultrasonicSensor));
        fullMap.checkedPut(VoltageSensor.class, new DeviceMap<>(basicMap.voltageSensor));
    }

    /**
     * Searches through this to find a {@link HardwareDevice} with the given name. If there are more
     * than one {@code HardwareDevice}s with the given name, the first one found is the one
     * returned.
     *
     * @param name a non-null, non-empty <code>String</code> to use to match to a HardwareDevice
     * @return a HardwareDevice with the given name
     * @throws RuntimeException         if the HardwareDevice cannot be cast to the correct form
     * @throws NullPointerException     if name is null or empty
     * @throws IllegalArgumentException if the a name with the given name cannot be found
     */

    @NotNull
    public <T extends HardwareDevice> T get(String name) throws RuntimeException {
        name = checkNotNull(Strings.emptyToNull(name));
        try {
            for (DeviceMap<? extends HardwareDevice> deviceMap : fullMap) {
                if (deviceMap.containsKey(name)) {
                    //noinspection unchecked
                    return (T) deviceMap.get(name);
                }
            }
        } catch (ClassCastException ex) {
            throw new RuntimeException("Incorrect cast occurred for the Hardware Device " +
                    "with the name of " + name, ex);
        }

        throw new IllegalArgumentException("Device " + name + " was not found");
    }

    /**
     * Returns a {@link DeviceMap<DcMotorController>} for use to access DC Motor Controller
     * hardware
     *
     * @return a DeviceMap to use for access to representations of DC Motor Controllers
     * @see DeviceMap
     * @see DcMotorController
     */

    public DeviceMap<DcMotorController> dcMotorControllers() {
        return fullMap.checkedGet(DcMotorController.class);
    }

    /**
     * Returns a {@link DeviceMap<DcMotor>} for use to access DC Motor hardware
     *
     * @return a DeviceMap to use for access to representations of DC Motor
     * @see DeviceMap
     * @see DcMotor
     */

    public DeviceMap<DcMotor> dcMotors() {
        return fullMap.checkedGet(DcMotor.class);
    }

    /**
     * Returns a {@link DeviceMap<DcMotorController>} for use to access Servo Controller hardware
     *
     * @return a DeviceMap to use for access to representations of DC Motor Controllers
     * @see DeviceMap
     * @see DcMotorController
     */

    public DeviceMap<ServoController> servoControllers() {
        return fullMap.checkedGet(ServoController.class);
    }

    /**
     * Returns a {@link DeviceMap<Servo>} for use to access Servo hardware
     *
     * @return a DeviceMap to use for access to representations of Servos
     * @see DeviceMap
     * @see Servo
     */

    public DeviceMap<Servo> servos() {
        return fullMap.checkedGet(Servo.class);
    }

    /**
     * Returns a {@link DeviceMap<LegacyModule>} for use to access Legacy Module hardware
     *
     * @return a DeviceMap to use for access to representations of Legacy Modules
     * @see DeviceMap
     * @see LegacyModule
     */

    public DeviceMap<LegacyModule> legacyModules() {
        return fullMap.checkedGet(LegacyModule.class);
    }

    /**
     * Returns a {@link DeviceMap<TouchSensorMultiplexer>} for use to access DC Motor Controller
     * hardware
     *
     * @return a DeviceMap to use for access to representations of DC Motor Controllers
     * @see DeviceMap
     * @see TouchSensorMultiplexer
     */

    public DeviceMap<TouchSensorMultiplexer> touchSensorMultiplexers() {
        return fullMap.checkedGet(TouchSensorMultiplexer.class);
    }

    /**
     * Returns a {@link DeviceMap<DeviceInterfaceModule>} for use to access Device Interface Module
     * hardware
     *
     * @return a DeviceMap to use for access to representations of Device Interface Modules
     * @see DeviceMap
     * @see DeviceInterfaceModule
     */

    public DeviceMap<DeviceInterfaceModule> deviceInterfaceModules() {
        return fullMap.checkedGet(DeviceInterfaceModule.class);
    }

    /**
     * Returns a {@link DeviceMap<AnalogInput>} for use to access Analog Input hardware
     *
     * @return a DeviceMap to use for access to representations of Analog Inputs
     * @see DeviceMap
     * @see AnalogInput
     */

    public DeviceMap<AnalogInput> analogInputs() {
        return fullMap.checkedGet(AnalogInput.class);
    }

    /**
     * Returns a {@link DeviceMap<DigitalChannel>} for use to access Digital Channel hardware
     *
     * @return a DeviceMap to use for access to representations of Digital Channels
     * @see DeviceMap
     * @see DigitalChannel
     */

    public DeviceMap<DigitalChannel> digitalChannels() {
        return fullMap.checkedGet(DigitalChannel.class);
    }

    /**
     * Returns a {@link DeviceMap<OpticalDistanceSensor>} for use to access Optical Distance Sensor
     * hardware
     *
     * @return a DeviceMap to use for access to representations of Optical Distance Sensors
     * @see DeviceMap
     * @see OpticalDistanceSensor
     */

    public DeviceMap<OpticalDistanceSensor> opticalDistanceSensors() {
        return fullMap.checkedGet(OpticalDistanceSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<TouchSensor>} for use to access Touch Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Touch Sensors
     * @see DeviceMap
     * @see TouchSensor
     */

    public DeviceMap<TouchSensor> touchSensors() {
        return fullMap.checkedGet(TouchSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<PwmOutput>} for use to access PWM Output hardware
     *
     * @return a DeviceMap to use for access to representations of PWM Outputs
     * @see DeviceMap
     * @see PwmOutput
     */

    public DeviceMap<PwmOutput> pwmOutputs() {
        return fullMap.checkedGet(PwmOutput.class);
    }

    /**
     * Returns a {@link DeviceMap<I2cDevice>} for use to access I2C Device hardware
     *
     * @return a DeviceMap to use for access to representations of I2C Devices
     * @see DeviceMap
     * @see I2cDevice
     */

    public DeviceMap<I2cDevice> i2cDevices() {
        return fullMap.checkedGet(I2cDevice.class);
    }

    /**
     * Returns a {@link DeviceMap<AnalogOutput>} for use to access Analog Output hardware
     *
     * @return a DeviceMap to use for access to representations of Analog Outputs
     * @see DeviceMap
     * @see AnalogOutput
     */

    public DeviceMap<AnalogOutput> analogOutputs() {
        return fullMap.checkedGet(AnalogOutput.class);
    }

    /**
     * Returns a {@link DeviceMap<ColorSensor>} for use to access Color Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Color Sensors
     * @see DeviceMap
     * @see ColorSensor
     */

    public DeviceMap<ColorSensor> colorSensors() {
        return fullMap.checkedGet(ColorSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<Led>} for use to access LED hardware
     *
     * @return a DeviceMap to use for access to representations of LEDs
     * @see DeviceMap
     * @see Led
     */

    public DeviceMap<Led> LEDs() {
        return fullMap.checkedGet(Led.class);
    }

    /**
     * Returns a {@link DeviceMap<AccelerationSensor>} for use to access Acceleration Sensor
     * hardware
     *
     * @return a DeviceMap to use for access to representations of Acceleration Sensors
     * @see DeviceMap
     * @see AccelerationSensor
     */

    public DeviceMap<AccelerationSensor> accelerationSensors() {
        return fullMap.checkedGet(AccelerationSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<CompassSensor>} for use to access Compass Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Compass Sensors
     * @see DeviceMap
     * @see CompassSensor
     */
    public DeviceMap<CompassSensor> compassSensors() {
        return fullMap.checkedGet(CompassSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<GyroSensor>} for use to access DC Motor Controller hardware
     *
     * @return a DeviceMap to use for access to representations of DC Motor Controllers
     * @see DeviceMap
     * @see GyroSensor
     */

    public DeviceMap<GyroSensor> gyroSensors() {
        return fullMap.checkedGet(GyroSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<IrSeekerSensor>} for use to access Ir Seeker Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Ir Seeker Sensors
     * @see DeviceMap
     * @see IrSeekerSensor
     */

    public DeviceMap<IrSeekerSensor> irSeekerSensors() {
        return fullMap.checkedGet(IrSeekerSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<LightSensor>} for use to access Light Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Light Sensors
     * @see DeviceMap
     * @see LightSensor
     */

    public DeviceMap<LightSensor> lightSensors() {
        return fullMap.checkedGet(LightSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<DcMotorController>} for use to access Ultrasonic Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of Ultrasonic Sensors
     * @see DeviceMap
     * @see DcMotorController
     */

    public DeviceMap<UltrasonicSensor> ultrasonicSensors() {
        return fullMap.checkedGet(UltrasonicSensor.class);
    }

    /**
     * Returns a {@link DeviceMap<VoltageSensor>} for use to access VoltageSensor Sensor hardware
     *
     * @return a DeviceMap to use for access to representations of VoltageSensor Sensors
     * @see DeviceMap
     * @see VoltageSensor
     */

    public DeviceMap<VoltageSensor> voltageSensors() {
        return fullMap.checkedGet(VoltageSensor.class);
    }
///**
//     * Builds a new {@link Wire} to allow for a better usage of I2C Devices. This method only looks
//     * at devices represented by {@link #i2cDevices()}
//     *
//     * @param name    the name of the I2C to use, as known by {@link #i2cDevices()}
//     * @param address the respective I2C address for the I2C Device
//     * @return a new Wire with the I2C Device represented by the given name
//     * @throws NullPointerException if name is empty or null
//     * @see Wire
//     * @see I2cFactory
//     * @see I2cDevice
//     */
//
//    public Wire wire(String name, byte address) throws NullPointerException {
//        checkNotNull(Strings.emptyToNull(name), "name is empty or null");
//        I2cDevice i2cDevice = i2cDevices().get(name);
//        if (i2cDevice == null) {
//            throw new IllegalArgumentException("The device " + name + " is not found.");
//        }
//        return I2cFactory.createWire(i2cDevice, address);
//    }

    /**
     * Returns the delegate {@link DeviceMultiMap} for this HardwareMap
     *
     * @return the underlying {@code DeviceMultimap} for this HardwareMap
     */

    DeviceMultiMap delegate() {
        return fullMap;
    }

/*    static class ForwardedI2cDevice extends QualcommForwardingI2cDevice {
        private I2cDevice device;

        private ForwardedI2cDevice(@NotNull I2cDevice device) {
            device = checkNotNull(device);
        }

        @Override
        protected I2cDevice delegate() {
            return device;
        }
    }*/
}

