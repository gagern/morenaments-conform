package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

class CircleBoundary implements Boundary {

    private double cx, cy, r2;

    private boolean complement;

    public CircleBoundary(double cx, double cy, double r2) {
        this(cx, cy, r2, false);
    }

    public CircleBoundary(double cx, double cy, double r2, boolean complement) {
        this.cx = cx;
        this.cy = cy;
        this.r2 = r2;
        this.complement = complement;
    }

    public void invert() {
        complement = !complement;
    }

    public boolean contains(Point2D p) {
        double dx = p.getX() - cx, dy = p.getY() - cy;
        return (dx*dx + dy*dy <= r2) ^ complement;
    }

    public Point2D intersect(Point2D inside, Point2D outside) {
        double x1 = inside.getX() - cx, x2 = outside.getX() - cx;
        double y1 = inside.getY() - cy, y2 = outside.getY() - cy;
        double dx = x2 - x1, ax = Math.abs(dx);
        double dy = y2 - y1, ay = Math.abs(dy);
        double x3, y3, x4, y4;
        if (ax > ay) { // more horizontal-like line
            double a = dy/dx;
            double b = y1 - a*x1;
            double c = -a*b;
            double d = a*a*r2 - b*b + r2;
            if (d < 0) noIntersection(inside, outside);
            double sd = Math.sqrt(d);
            double e = a*a + 1;
            x3 = (c + sd)/e;
            y3 = a*x3 + b;
            x4 = (c - sd)/e;
            y4 = a*x4 + b;
        }
        else {
            double a = dx/dy;
            double b = x1 - a*y1;
            double c = -a*b;
            double d = a*a*r2 - b*b + r2;
            if (d < 0) noIntersection(inside, outside);
            double sd = Math.sqrt(d);
            double e = a*a + 1;
            y3 = (c + sd)/e;
            x3 = a*y3 + b;
            y4 = (c - sd)/e;
            x4 = a*y4 + b;
        }
        dx = x3 - x1;
        dy = y3 - y1;
        double d3 = dx*dx + dy*dy;
        dx = x4 - x1;
        dy = y4 - y1;
        double d4 = dx*dx + dy*dy;
        if (d3 <= d4)
            return new Point2D.Double(x3 + cx, y3 + cy);
        else
            return new Point2D.Double(x4 + cx, y4 + cy);
    }

    private void noIntersection(Point2D inside, Point2D outside) {
        String msg = String.format(
            "Line (%f, %f) -- (%f, %f) does not intersect " +
            "circle (x - %f)^2 + (y - %f)^2 %s %f",
            inside.getX(), inside.getY(), outside.getX(), outside.getY(),
            cx, cy, complement ? ">" : "<=", r2
        );
        throw new IllegalArgumentException(msg);
    }

}
