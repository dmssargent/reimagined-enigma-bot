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

package org.ftccommunity.math;

/**
 * A representation of a point in the Cartesian coordinate system
 *
 * @author David
 * @since 0.2.1
 */
public class PolarCoordinates {
    private double r;
    private double theta;

    /**
     * Builds an coordinate object based on a certain point
     *
     * @param r     the distance from the origin
     * @param theta the rotation in radians from the origin axis to the point
     */
    public PolarCoordinates(int r, int theta) {
        this.r = r;
        this.theta = theta;
    }

    /**
     * Builds an coordinate object based on a certain point
     *
     * @param r     the distance from the origin
     * @param theta the rotation in radians from the origin axis to the point
     */
    public PolarCoordinates(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    /**
     * Builds a polar coordinate system from a cartesian coordinates
     *
     * @param coordinates the Cartesian coordinates of a point
     */
    public PolarCoordinates(CartesianCoordinates coordinates) {
        getCoordinates(coordinates);
    }

    /**
     * Builds a polar coordinate system from a cartesian coordinates
     *
     * @param coordinates the Cartesian coordinates of a point
     */
    private void getCoordinates(CartesianCoordinates coordinates) {
        double x = coordinates.getX();
        double y = coordinates.getY();

        r = Math.sqrt(x * x + y * y);
        // Purposely flip the X and Y to get rotation on the X relative to Y
        theta = Math.atan2(y, x);
    }

    /**
     * Gets the distance from the origin for this point
     *
     * @return distance
     */
    public double getR() {
        return r;
    }

    /**
     * Gets the theta in radians for this point
     *
     * @return theta in radians
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Inverts the current point (reflect over y=x axis)
     *
     * @return this object after being reflected
     */
    public PolarCoordinates invert() {
        CartesianCoordinates temp = new CartesianCoordinates(this);
        temp.invert();
        getCoordinates(temp);

        return this;
    }

    /**
     * Gets the current theta in degrees
     *
     * @return theta in degrees
     */
    public double getThetaInDegrees() {
        return theta / Math.PI * 180;
    }
}
