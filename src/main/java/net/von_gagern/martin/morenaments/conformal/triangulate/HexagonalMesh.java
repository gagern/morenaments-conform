package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.Point;
import java.awt.geom.*;
import java.util.*;

class HexagonalMesh extends Mesh {

    private static final double SQRT3_2 = Math.sqrt(3.)/2.;

    private Set<Point> doneFace;

    public HexagonalMesh() {
        super();
        setScale(0.01);
    }

    public void setScale(double scale) {
        setBase(scale, 0, 0);
    }

    public void setBase(double scale, double ox, double oy) {
        gridBase = new AffineTransform(scale, 0,
                                       scale*0.5, scale*SQRT3_2,
                                       ox, oy);
    }

    protected void doTriangulate() {
        doneFace = new HashSet<Point>();
        doFace(1, 0, 1);
    }

    private void doFace(int a, int b, int sign) {
        Point coords = new Point(a, b);
        if (!doneFace.add(coords)) return;
        ArrayList<Point2D> vs = new ArrayList<Point2D>(3);
        vs.add(gridPoint(a-sign, b));
        vs.add(gridPoint(a, b+sign));
        vs.add(gridPoint(a+sign, b-sign));

        ArrayList<Point2D> inside = new ArrayList<Point2D>(3);
        ArrayList<Point2D> outside = new ArrayList<Point2D>(3);
        for (Point2D v: vs) {
            if (contains(v)) inside.add(v);
            else outside.add(v);
        }
        switch (inside.size()) {
        case 0:
            return; // abort traversal
        case 1:
            onePointInside(inside.get(0), outside.get(0), outside.get(1));
            break;
        case 2:
            twoPointsInside(inside.get(0), inside.get(1), outside.get(0));
            break;
        case 3:
            triangle(inside.get(0), inside.get(1), inside.get(2));
            break;
        }

        doFace(a+sign, b, -sign);
        doFace(a-sign, b+sign, -sign);
        doFace(a, b-sign, -sign);
    }

    private void onePointInside(Point2D in, Point2D out1, Point2D out2) {
        Intersection i1 = intersect(in, out1);
        Intersection i2 = intersect(in, out2);
        Point2D v1 = i1.getPoint();
        Point2D v2 = i2.getPoint();
        Point2D corner = intersect(i1.getBoundary(), i2.getBoundary());
        if (corner == null) {
            triangle(in, v1, v2);
        }
        else {
            double dv = v1.distanceSq(v2);
            double dc = in.distanceSq(corner);
            if (dv < dc) {
                triangle(in, v1, v2);
                triangle(v1, v2, corner);
            }
            else {
                triangle(in, v1, corner);
                triangle(in, v2, corner);
            }
        }
    }

    private void twoPointsInside(Point2D in1, Point2D in2, Point2D out) {
        Intersection i1 = intersect(in1, out);
        Intersection i2 = intersect(in2, out);
        Point2D v1 = i1.getPoint();
        Point2D v2 = i2.getPoint();
        Point2D corner = intersect(i1.getBoundary(), i2.getBoundary());
        if (corner != null)
            triangle(v1, v2, corner);
        double d1 = in1.distanceSq(v2);
        double d2 = in2.distanceSq(v1);
        if (d1 < d2) {
            triangle(in1, in2, v2);
            triangle(in1, v2, v1);
        }
        else {
            triangle(in1, in2, v1);
            triangle(in2, v2, v1);
        }
    }

}
