package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

class ArcEdge extends Edge {

    private Point2D c;

    private double r;

    public ArcEdge(Point2D p1, Point2D p2, Point2D center, double radius) {
        super(p1, p2);
        c = center;
        r = radius;
    }

    @Override protected Point2D createCenter() {
        Point2D p1 = getP1(), p2 = getP2();
        double x = (p1.getX() + p2.getX())/2. - c.getX();
        double y = (p1.getY() + p2.getY())/2. - c.getY();
        double f = r/Math.hypot(x, y);
        return new Point2D.Double(c.getX() + f*x, c.getY() + f*y);
    }

    @Override protected ArcEdge createFirstPart() {
        return new ArcEdge(getP1(), getCenter(), c, r);
    }

    @Override protected ArcEdge createSecondPart() {
        return new ArcEdge(getCenter(), getP2(), c, r);
    }

}
