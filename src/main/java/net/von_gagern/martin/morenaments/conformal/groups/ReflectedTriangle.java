package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

abstract class ReflectedTriangle extends ReflectionBasedGroup {

    protected ReflectedTriangle(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "a", "b", "c" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] res = new HypTrafo[3];
        for (int i = 0; i < 3; ++i)
            res[i] = fundamentalTriangle.getEdgeInsideness(i);
        return res;
    }

    public List<Point2D> getHypTileCorners() {
        ensureFundamentalTriangle();
        List<Point2D> corners = new ArrayList<Point2D>(3);
        for (int i = 0; i < 3; ++i) {
            Vec2C v = fundamentalTriangle.getCorner(i);
            corners.add(v.dehomogenize(new Point2D.Double()));
        }
        return corners;
    }

    public double getEuclideanCornerAngle(int index) {
        return Math.PI/euclideanAngles[index];
    }

}
