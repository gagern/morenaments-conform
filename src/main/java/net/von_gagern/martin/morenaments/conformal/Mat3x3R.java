package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.AffineTransform;
import de.tum.in.gagern.hornamente.Vec3R;

public class Mat3x3R implements Cloneable {

    private double[] m;

    public Mat3x3R() {
        m = new double[9];
    }

    public Mat3x3R(double... components) {
        if (components.length != 9)
            throw new IllegalArgumentException("Need exactly 9 components");
        m = components;
    }

    public Mat3x3R(AffineTransform t) {
        this(t.getScaleX(), t.getShearX(), t.getTranslateX(),
             t.getShearY(), t.getScaleY(), t.getTranslateY(),
                        0.,            0.,                1.);
    }

    public Mat3x3R clone() {
        return new Mat3x3R((double[])m.clone());
    }

    public double get(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3)
            throw new IndexOutOfBoundsException();
        return m[3*row + col];
    }

    public void set(int row, int col, double value) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3)
            throw new IndexOutOfBoundsException();
        m[3*row + col] = value;
    }

    public double det() {
        return m[0]*m[4]*m[8] + m[1]*m[5]*m[6] + m[2]*m[3]*m[7]
             - m[0]*m[5]*m[7] - m[1]*m[3]*m[8] - m[2]*m[4]*m[6];
    }

    public Vec3R multiply(Vec3R in) {
        Vec3R out = new Vec3R();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                out.add(i, get(i, j)*in.get(j));
            }
        }
        return out;
    }

    public Mat3x3R multiply(Mat3x3R that) {
        Mat3x3R res = new Mat3x3R();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double sum = 0;
                for (int k = 0; k < 3; ++k)
                    sum += this.get(i, k)*that.get(k, j);
                res.set(i, j, sum);
            }
        }
        return res;
    }

    public void scale(double factor) {
        for (int i = 0; i < 9; ++i)
            m[i] *= factor;
    }

    private int[] INV = {
        4, 8, 5, 7,    2, 7, 1, 8,    1, 5, 2, 4,
        5, 6, 3, 8,    0, 8, 2, 6,    2, 3, 0, 5,
        3, 7, 4, 6,    1, 6, 0, 7,    0, 4, 1, 3
    };

    public Mat3x3R getInverse() {
        double d = det();
        double[] ci = new double[9];
        for (int i = 0; i < 9*4; i += 4)
            ci[i/4] = (m[INV[i]]*m[INV[i+1]] - m[INV[i+2]]*m[INV[i+3]])/d;
        return new Mat3x3R(ci);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        String startRow = "Mat3x3R[";
        for (int row = 0; row < 3; ++row) {
            String startCol = startRow;
            startRow = "; ";
            for (int col = 0; col < 3; ++col) {
                buf.append(startCol).append(get(row, col));
                startCol = ", ";
            }
        }
        buf.append("]");
        return buf.toString();
    }

}
