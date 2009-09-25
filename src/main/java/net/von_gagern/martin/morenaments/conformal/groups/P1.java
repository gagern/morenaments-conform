package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.triangulate.EucOrbifold;
import net.von_gagern.martin.morenaments.conformal.triangulate.Vertex;
import net.von_gagern.martin.morenaments.conformal.triangulate.HypLayout;

class P1 extends OrbifoldBasedGroup {

    private final Logger logger = Logger.getLogger(P1.class);

    private static final int[] EUCLIDEAN_ANGLES = { 1 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        1, 0,
        1, 1,
        0, 1,
    };

    public P1() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

    protected HypTrafo[] constructGenerators() {
        HypTrafo[] gens = new HypTrafo[4];
        gens[0] = translation(3, 2, 0, 1);
        gens[1] = translation(0, 3, 1, 2);
        gens[2] = gens[0].getInverse();
        gens[3] = gens[1].getInverse();
        if (logger.isDebugEnabled())
            for (int i = 0 ; i < 4; ++i)
                logger.debug("Generator " + i + ": " + gens[i]);
        return gens;
    }

    public double getEuclideanCornerAngle(int index) {
        switch (index) {
        case 0:
        case 2:
            return eucBaseAngle;
        case 1:
        case 3:
            return Math.PI - eucBaseAngle;
        default:
            throw new IllegalArgumentException();
        }
    }

    protected EucOrbifold createEucOrbifold() {
        return new Orbifold();
    }

    protected Vec2C[] constructCorners(Vec2C[] specialPoints) {
        Vec2C[] corners = new Vec2C[4];
        corners[0] = specialPoints[0];
        corners[1] = specialPoints[1];
        corners[2] = specialPoints[2];
        corners[3] = specialPoints[3];
        return corners;
    }

    protected Map<Vertex, Double>
        getHypAngles(Vertex[] specialPoints, int[] hyperbolicAngles) {
        Map<Vertex, Double> angles = new HashMap<Vertex, Double>(1);
        angles.put(specialPoints[0], Math.PI*2/hyperbolicAngles[0]);
        return angles;
    }

    protected AffineTransform affineOrbifoldTransform() {
        return new AffineTransform(1, 0, 0, 1, 0, 0);
    }

    private static class Orbifold extends EucOrbifold {

        public Orbifold() {
            super(1, 1);
            for (int i = 0; i <= UNIT; ++i) {
                vs[UNIT][i] = vs[0][i];
                vs[i][UNIT] = vs[i][0];
            }
            specialPoints = new Vertex[] {
                vs[0][0],
                vs[UNIT][0],
                vs[UNIT][UNIT],
                vs[0][UNIT]
            };
            setCenterCoordinates(HALF-2, HALF-4);
        }

    }

    @Override protected HypLayout createHypLayouter(EucOrbifold eo) {
        return new Layout(eo);
    }

    private class Layout extends HypLayout {

        private Vertex[] sp;

        private boolean splitDir;

        private ArrayList<Vertex> spf = new ArrayList<Vertex>(4);

        public Layout(EucOrbifold eo) {
            super(eo.getCenter(), new ArrayList<HypTrafo>(4));
            sp = eo.getSpecialPoints();
            splitDir = eo.getSplitDir();
        }

        @Override protected boolean triangleIsRelevant(Point2D... points) {
            assert points.length == 3;
            Vertex v = (Vertex)points[2];
            if (v.getOrbifoldElement() == sp[0] && !spf.contains(v)) {
                spf.add(v);
                if (spf.size() == 4) {
                    Vec2C[] hc = new Vec2C[4];
                    hypCorners = new Vec2C[4];
                    for (int i = 0; i < 4; ++i) {
                        Vertex spi = spf.get(i);
                        hc[i] = new Vec2C(spi.getX(), spi.getY(), 1, 0);
                        logger.debug("Vertex " + i + ": " + spf.get(i));
                    }
                    logger.debug("splitDir=" + splitDir);
                    if (splitDir) {
                        hypCorners[0] = hc[2];
                        hypCorners[1] = hc[0];
                        hypCorners[2] = hc[3];
                        hypCorners[3] = hc[1];
                    }
                    else {
                        hypCorners[0] = hc[0];
                        hypCorners[1] = hc[2];
                        hypCorners[2] = hc[1];
                        hypCorners[3] = hc[3];
                    }
                    insidenessChecks.addAll(Arrays.asList(getInsidenessChecks()));
                    //System.exit(0);
                }
            }
            return super.triangleIsRelevant(points);
        }

    }

}
