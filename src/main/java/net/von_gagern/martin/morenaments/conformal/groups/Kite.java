package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

abstract class Kite extends ReflectionBasedGroup {

    protected Kite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] res = new HypTrafo[4];
        HypTrafo c = fundamentalTriangle.getReflection(2);
        res[0] = fundamentalTriangle.getEdgeInsideness(0);
        res[1] = fundamentalTriangle.getEdgeInsideness(1);
        res[2] = res[0].clone().concatenate(c);
        res[3] = res[1].clone().concatenate(c);
        return res;
    }

    public List<Point2D> getHypTileCorners() {
        List<Point2D> corners = new ArrayList<Point2D>(4);
        for (int i = 0; i < 3; ++i) {
            Vec2C v = fundamentalTriangle.getCorner(i);
            corners.add(v.dehomogenize(new Point2D.Double()));
        }
        Vec2C v = fundamentalTriangle.getCorner(2).clone();
        fundamentalTriangle.getReflection(2).transform(v, v);
        corners.add(1, v.dehomogenize(new Point2D.Double()));
        assert corners.size() == 4;
        return corners;
    }

}
