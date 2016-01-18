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

import Jama.Matrix;

import org.ftccommunity.ftcxtensible.internal.Alpha;
import org.ftccommunity.ftcxtensible.internal.NotDocumentedWell;

/**
 * This work is licensed under a Creative Commons Attribution 3.0 License, the Creative Commons
 * overrides the MIT
 *
 * @author Ahmed Abdelkader, David Sargent
 */
public final class KalmanFilter {
//    private Matrix X;
//    private Matrix X0;
//    private Matrix F;
//    private Matrix B;
//    private Matrix U;
//    private Matrix Q;
//    private Matrix H;
//    private Matrix R;
//    private Matrix P;
//    private Matrix P0;
//
//    public KalmanFilter(int variables) {
//        setX(new Matrix(variables, variables));
//        setX0(new Matrix(variables, variables));
//
//        F = new Matrix(variables, variables);
//        setB(new Matrix(variables, variables));
//        setU(new Matrix(variables, variables));
//        setQ(new Matrix(variables, variables));
//
//        setH(new Matrix(variables, variables));
//        setR(new Matrix(variables, variables));
//
//        setP(new Matrix(variables, variables));
//        setP0(new Matrix(variables, variables));
//
//    }
//
//    public void predict() {
//        setX0(F.times(getX()).plus(getB().times(getU())));
//        setP0(F.times(getP()).times(F.transpose()).plus(getQ()));
//    }
//
//    public void correct(Matrix Z) {
//        Matrix S = getH().times(getP0()).times(getH().transpose()).plus(getR());
//
//        Matrix K = getP0().times(getH().transpose()).times(S.inverse());
//
//        setX(getX0().plus(K.times(Z.minus(getH().times(getX0())))));
//
//        Matrix I = Matrix.identity(getP0().getRowDimension(), getP0().getColumnDimension());
//        setP((I.minus(K.times(getH()))).times(getP0()));
//    }
//
//    public Matrix getX() {
//        return X;
//    }
//
//    public void setX(Matrix x) {
//        X = x;
//    }
//
//    public Matrix getX0() {
//        return X0;
//    }
//
//    public void setX0(Matrix x0) {
//        X0 = x0;
//    }
//
//    public Matrix getB() {
//        return B;
//    }
//
//    public void setB(Matrix b) {
//        B = b;
//    }
//
//    public Matrix getU() {
//        return U;
//    }
//
//    public void setU(Matrix u) {
//        U = u;
//    }
//
//    public Matrix getQ() {
//        return Q;
//    }
//
//    public void setQ(Matrix q) {
//        Q = q;
//    }
//
//    public Matrix getH() {
//        return H;
//    }
//
//    public void setH(Matrix h) {
//        H = h;
//    }
//
//    public Matrix getR() {
//        return R;
//    }
//
//    public void setR(Matrix r) {
//        R = r;
//    }
//
//    public Matrix getP() {
//        return P;
//    }
//
//    public void setP(Matrix p) {
//        P = p;
//    }
//
//    public Matrix getP0() {
//        return P0;
//    }
//
//    public void setP0(Matrix p0) {
//        P0 = p0;
//    }

}