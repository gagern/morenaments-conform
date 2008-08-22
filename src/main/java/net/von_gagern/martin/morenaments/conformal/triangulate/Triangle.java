package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.morenaments.conformal.Mat3x3R;

public class Triangle implements CorneredTriangle<Point2D> {

    private List<Point2D> vs;

    private List<Edge> es;

    private Mat3x3R proj;

    public Triangle(Point2D a, Point2D b, Point2D c,
                    Edge bc, Edge ca, Edge ab) {
        assert ccw(a, b, c) > 0 : "triangle must have positive orientation";
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

    public Mat3x3R getProj() {
        return proj;
    }

    public void setProj(Mat3x3R proj) {
        this.proj = proj;
    }

    public Triangle neighborContaining(Point2D p) {
        Point2D c1 = vs.get(2), c2;
        Triangle res = this;
        double minCcw = 0;
        for (int i = 0; i < 3; ++i) {
            c2 = vs.get(i);
            double ccw = ccw(c1, c2, p);
            if (ccw < 0 && (res == null || ccw < minCcw)) {
                res = es.get((i + 1)%3).otherTriangle(this);
                minCcw = ccw;
            }
            c1 = c2;
        }
        return res;
    }

    public static double ccw(Point2D p1, Point2D p2, Point2D p3) {
        return ccw(p1.getX(), p1.getY(),
                   p2.getX(), p2.getY(),
                   p3.getX(), p3.getY());
    }

    public static double ccw(double x1, double y1,
                             double x2, double y2,
                             double x3, double y3) {
        return x1*y2 - x2*y1 + x2*y3 - x3*y2 + x3*y1 - x1*y3;
    }

}
