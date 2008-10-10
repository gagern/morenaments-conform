package net.von_gagern.martin.morenaments.conformal.groups;

import java.util.HashMap;
import java.util.Map;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

class Pmm extends OrbifoldBasedGroup {

    private static final int[] EUCLIDEAN_ANGLES = { 2, 2, 2, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        .5, 0,
        .5, .5,
        0, .5
    };

    public Pmm() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

    protected HypTrafo[] constructGenerators() {
        HypTrafo[] gens = new HypTrafo[4];
        for (int i = 0; i < 4; ++i)
            gens[i] = reflection(i);
        return gens;
    }

    public double getEuclideanCornerAngle(int index) {
        return Math.PI/2;
    }

    protected EucOrbifold createEucOrbifold() {
        return new Orbifold();
    }

    protected Vec2C[] constructCorners(Vec2C[] specialPoints) {
        return specialPoints;
    }

    protected Map<Object, Double>
        getHypAngles(Object[] specialPoints, int[] hyperbolicAngles) {
        Map<Object, Double> angles = new HashMap<Object, Double>(6);
        for (int i = 0; i < 4; ++i)
            angles.put(specialPoints[i], Math.PI/hyperbolicAngles[i]);
        return angles;
    }

    private static class Orbifold extends EucOrbifold {

        public Orbifold() {
            super(1, 1);
            specialPoints = new Object[] {
                vs[0][0],
                vs[MESH_LENGTH][0],
                vs[MESH_LENGTH][MESH_LENGTH],
                vs[0][MESH_LENGTH]
            };
            setCenterCoordinates(HALF_LENGTH, HALF_LENGTH);
        }

    }

}
