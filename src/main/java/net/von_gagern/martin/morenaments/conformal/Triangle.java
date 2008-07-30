package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import net.von_gagern.martin.cetm.mesh.CorneredTriangle;

class Triangle implements CorneredTriangle<Point2D> {

    private List<Point2D> vs;

    private List<Edge> es;

    public Triangle(Point2D a, Point2D b, Point2D c,
                    Edge bc, Edge ca, Edge ab) {
        vs = Arrays.asList(a, b, c);
        es = Arrays.asList(bc, ca, ab);
    }

    public List<Edge> edges() {
        return es;
    }

    public List<Point2D> vertices() {
        return vs;
    }

    public Point2D getCorner(int i) {
        return vs.get(i);
    }

}
