package net.von_gagern.martin.morenaments.conformal.groups;

import java.util.HashMap;
import java.util.Map;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

class P2 extends OrbifoldBasedGroup {

    private static final int[] EUCLIDEAN_ANGLES = { 2, 2, 2, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, .5,
        0, 0,
        .5, 0,
        1, 0,
        1, .5,
        .5, .5,
    };

    public P2() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

    protected HypTrafo[] constructGenerators() {
        HypTrafo[] gens = new HypTrafo[6];
        gens[1] = rotation(2, hyperbolicAngles[1]);
        gens[2] = gens[1].getInverse();
        gens[4] = rotation(5, hyperbolicAngles[2]);
        gens[5] = gens[4].getInverse();
        gens[0] = rotation(1, hyperbolicAngles[0]).concatenate(gens[1]);
        gens[3] = gens[0].getInverse();
        return gens;
    }

    public double getEuclideanCornerAngle(int index) {
        switch (index) {
        case 1:
        case 4:
            return eucBaseAngle;
        case 3:
        case 0:
            return Math.PI - eucBaseAngle;
        case 2:
        case 5:
            return Math.PI;
        default:
            throw new IllegalArgumentException();
        }
    }

    protected EucOrbifold createEucOrbifold() {
        return new Orbifold();
    }

    protected Vec2C[] constructCorners(Vec2C[] specialPoints) {
        Vec2C[] corners = new Vec2C[6];
        corners[1] = specialPoints[0];
        corners[2] = specialPoints[1];
        corners[5] = specialPoints[2];
        corners[0] = specialPoints[3];
        corners[3] = rotate(corners[1], corners[2], -hyperbolicAngles[1]);
        corners[4] = rotate(corners[0], corners[5], hyperbolicAngles[2]);
        return corners;
    }

    protected Map<Object, Double>
        getHypAngles(Object[] specialPoints, int[] hyperbolicAngles) {
        Map<Object, Double> angles = new HashMap<Object, Double>(6);
        for (int i = 0; i < 4; ++i)
            angles.put(specialPoints[i], Math.PI*2/hyperbolicAngles[i]);
        return angles;
    }

    private static class Orbifold extends EucOrbifold {

        public Orbifold() {
            super(2, 1);
            for (int i = 0; i <= UNIT; ++i) {
                vs[DOUBLE][i] = vs[0][i]; // glue together outer boundaries
                vs[DOUBLE - i][0] = vs[i][0]; // fold and glue lower boundary
                vs[DOUBLE - i][UNIT] = vs[i][UNIT]; // same for upper boundary
            }
            specialPoints = new Object[] {
                vs[0][0],
                vs[UNIT][0],
                vs[UNIT][UNIT],
                vs[0][UNIT]
            };
            setCenterCoordinates(HALF, HALF);
        }

    }

}
