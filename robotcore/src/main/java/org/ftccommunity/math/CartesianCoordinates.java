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

import com.google.common.annotations.Beta;

/**
 * A representation of a point in the Cartesian coordinate system
 *
 * @author David
 * @since 0.2.1
 */
@Beta
public class CartesianCoordinates {
    private double x;
    private double y;

    /**
     * Builds a Cartesian point for the given point
     *
     * @param x the x for the point
     * @param y the y for the point
     */
    public CartesianCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Builds a Cartesian point for the given point
     *
     * @param x the x for the point
     * @param y the y for the point
     */
    public CartesianCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Duplicates an existing Cartesian point
     *
     * @param coordinates the {@code CartesianCoordinates} to copy from
     */
    public CartesianCoordinates(CartesianCoordinates coordinates) {
        x = coordinates.x;
        y = coordinates.y;
    }

    /**
     * Duplicates an existing Polar point
     *
     * @param coordinates the {@link PolarCoordinates} to copy from
     */
    public CartesianCoordinates(PolarCoordinates coordinates) {
        double r = coordinates.getR();
        double theta = coordinates.getTheta();

        x = r * Math.cos(theta);
        y = r * Math.sin(theta);
    }

    /**
     * Gets the current X-coordinate
     *
     * @return X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the current Y-coordinate
     *
     * @return Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Reflect the point over the y=x axis
     *
     * @return this object after being reflected
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public CartesianCoordinates invert() {
        x = this.y;
        y = this.x;

        return this;
    }
}
