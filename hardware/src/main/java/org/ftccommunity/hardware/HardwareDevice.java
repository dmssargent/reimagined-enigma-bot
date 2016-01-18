package org.ftccommunity.hardware;

import android.support.annotation.Nullable;

import org.ftccommunity.hardware.information.SerialNumber;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The basic interface for HardwareDevices. Implement this interface only if you are defining a new
 * Hardware type that has not already been implemented. Users of this should verify that {@link
 * #close()} has been called by overriding {@link Object#finalize()} in your sub-class. Please see
 * the notes regarding the methods that know how they should behave.
 *
 * @author David Sargent
 * @version 0.5
 * @see org.ftccommunity.hardware for pre-defined implementations
 * @since 0.0.1
 */
public interface HardwareDevice {
    /**
     * Returns the common name of the device in a format that easily recognizable to identify the
     * hardware for people unfamiliar with the hardware. If a product from a vendor is already been
     * defined, use the same format as previously defined for that vendor.
     *
     * @return the device name
     */
    String getDeviceName();

    /**
     * Returns the connect type along with information on how well the device is connected, and the
     * routing info for that device. Be sure to include the port numbers for which this device is
     * attached to.
     *
     * @return the current connection status
     */
    String getConnectionInfo();

    /**
     * The hardware revision number for the connected hardware. Use the {@code @version} javadoc for
     * the software, and if the your implementation requires add a {@code getSWVersion()} method to
     * the subclasses
     *
     * @return the hardware version
     */
    int getVersion();

    /**
     * Closes and releases all handles to this HardwareDevice. If you are implemented a device in
     * which other devices are exclusively connected to then you should call this method on all
     * devices exclusively connected to this device, if and only if your implementation is aware of
     * those devices. Implementations should throw a {@link IllegalStateException} if the device is
     * closed, as determined by {@link #isClosed()}
     */
    void close() throws IllegalStateException;

    /**
     * Returns whether or not {@link #close()} or {@link #finalize()} has been called by this
     * object.
     *
     * @return <code>true</code> if the device has been closed, otherwise <code>false</code>
     */
    boolean isClosed();

    DeviceInfo deviceInfo();

    class DeviceInfo<E extends HardwareDevice> implements Serializable {
        private Class<E> type;
        private String name;
        private int port;
        private ArrayList<SerialNumber> serialNumber;
        private HardwareDevice superDevice;

        /**
         * A constructor that fulfills the contract that {@link Serializable} provides, you must
         * provide a no-arg constructor in any subclasses of this
         */
        @Deprecated
        protected DeviceInfo() {

        }

        /**
         * Creates a DeviceInfo to represent the serializable information found in a {@link
         * HardwareDevice}. This object will be used as the information necessary to load the
         * respective Hardware Device, if any info needed to construct a object is left out the
         * creation of the object will not succeed.
         *
         * @param name          the user-given name of the device
         * @param port          the port number that this device is connected to, see the range of
         *                      avialable port numbers provided by the super device
         * @param superDevice   the device that this device is connected to
         * @param serialNumbers the known serial numbers for this device
         */
        public DeviceInfo(@NotNull Class<E> type, @NotNull String name,
                          final int port, @Nullable HardwareDevice superDevice,
                          @Nullable SerialNumber... serialNumbers) {
            this.name = checkNotNull(name);
            this.type = checkNotNull(type);
            this.port = port;
            this.superDevice = superDevice;
            if (serialNumbers != null) {
                this.serialNumber = new ArrayList<>(Arrays.asList(serialNumbers));
            } else {
                this.serialNumber = null;
            }
        }

        /**
         * Returns the user-given name for this device
         *
         * @return the user-given name for this device
         */
        @NotNull
        public String name() {
            return name;
        }

        /**
         * The port the device is plugged into on the device's super device
         *
         * @return the port this device is plugged into
         * @see #superDevice()
         */
        public int port() {
            return port;
        }

        /**
         * An array of the possible serial numbers for this device
         *
         * @return the possible known {@link SerialNumber}s for this device
         */
        @Nullable
        public SerialNumber[] serialNumber() {
            return serialNumber.toArray(new SerialNumber[serialNumber.size()]);
        }

        /**
         * Returns the software implementation of the device that this is connected to
         *
         * @return the device that is connected to
         */
        @Nullable
        public HardwareDevice superDevice() {
            return superDevice;
        }

        /**
         * Checks if a reference is equal to <code>null</code>
         *
         * @param ref the reference to check
         * @param <T> the type of the reference
         * @return the reference if it is not null
         * @throws NullPointerException if the reference is null
         */
        private <T> T checkNotNull(T ref) throws NullPointerException {
            if (ref == null) {
                throw new NullPointerException();
            }

            return ref;
        }

        /**
         * Returns the implementation class of the the Hardware
         *
         * @return the implementing class
         */
        public Class<E> getType() {
            return type;
        }
    }
}
