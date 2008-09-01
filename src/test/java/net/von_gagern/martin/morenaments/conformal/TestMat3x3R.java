package net.von_gagern.martin.morenaments.conformal;

import java.util.Random;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import de.tum.in.gagern.hornamente.Vec3R;

public class TestMat3x3R {

    Logger logger = Logger.getLogger(TestMat3x3R.class);

    Random rnd;

    double eps = Math.ulp(1.)*8.;

    @Before public void initRnd() {
        rnd = new Random(1993517801);
    }

    double rndDbl() {
        return 4.*rnd.nextDouble() - 2.;
    }

    Vec3R rndVec() {
        double x = rndDbl();
        double y = rndDbl();
        double z = rndDbl();
        return new Vec3R(x, y, z);
    }

    Mat3x3R rndMat() {
        double[] m = new double[9];
        for (int i = 0; i < 9; ++i)
            m[i] = rndDbl();
        return new Mat3x3R(m);
    }

    @Test public void testMatGet() {
        Mat3x3R m = new Mat3x3R(1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3);
        for (int r = 0; r < 3; ++r)
            for (int c = 0; c < 3; ++c)
                assertEquals(r + c*0.1 + 1.1, m.get(r, c), eps);
    }

    @Test public void testVecGet() {
        Vec3R v = new Vec3R(1.1, 2.2, 3.3);
        for (int i = 0; i < 3; ++i)
            assertEquals(i*1.1 + 1.1, v.get(i), eps);
    }

    @Test public void testMultVec() {
        Mat3x3R m = rndMat();
        Vec3R v = rndVec();
        Vec3R mv = m.multiply(v);
        assertNotSame(v, mv);
        for (int r = 0; r < 3; ++r) {
            double sum = 0;
            for (int i = 0; i < 3; ++i)
                sum += m.get(r, i)*v.get(i);
            assertEquals(sum, mv.get(r), eps);
        }
    }

    @Test public void testMultMat() {
        Mat3x3R a = rndMat();
        Mat3x3R b = rndMat();
        Mat3x3R ab = a.multiply(b);
        assertNotSame(a, ab);
        assertNotSame(b, ab);
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 3; ++c) {
                double sum = 0;
                for (int i = 0; i < 3; ++i) {
                    sum += a.get(r, i)*b.get(i, c);
                }
                assertEquals(sum, ab.get(r, c), eps);
            }
        }        
        // check one single entry manually, in case above loops are buggy
        assertEquals(a.get(2, 0)*b.get(0, 1) +
                     a.get(2, 1)*b.get(1, 1) +
                     a.get(2, 2)*b.get(2, 1),
                     ab.get(2, 1), eps);
    }

    @Test public void testInverse() {
        Mat3x3R a = rndMat();
        Mat3x3R b = a.getInverse();
        Mat3x3R ab = a.multiply(b);
        Mat3x3R ba = b.multiply(a);
        Mat3x3R i = new Mat3x3R(1., 0., 0., 0., 1., 0., 0., 0., 1.);
        logger.debug("a = " + a);
        logger.debug("b = " + b);
        assertEqualsMat(i, ab);
        assertEqualsMat(i, ba);
    }

    void assertEqualsMat(Mat3x3R a, Mat3x3R b) {
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 3; ++c) {
                if (Math.abs(a.get(r, c) - b.get(r, c)) > eps) {
                    throw new AssertionError("expected: " + a +
                                             " but was: " + b);
                }
            }
        }
    }

}
