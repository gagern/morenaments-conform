package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

class Edge {

    private Point2D p1;

    private Point2D p2;

    private Triangle left;

    private Triangle right;

    private Point2D center;

    private Edge firstPart;

    private Edge secondPart;

    public Edge(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point2D getP1() {
        return p1;
    }

    public Point2D getP2() {
        return p2;
    }

    public Point2D getCenter() {
        if (center == null)
            center = createCenter();
        return center;
    }

    public Edge getFirstPart() {
        if (firstPart == null)
            firstPart = createFirstPart();
        return firstPart;
    }

    public Edge getSecondPart() {
        if (secondPart == null)
            secondPart = createSecondPart();
        return secondPart;
    }

    public Edge getPart(Point2D endpoint) {
        if (endpoint == p1) {
            return getFirstPart();
        }
        else {
            assert endpoint == p2;
            return getSecondPart();
        }
    }

    protected Point2D createCenter() {
        return new Point2D.Double((p1.getX() + p2.getX())/2.,
                                  (p1.getY() + p2.getY())/2.);
    }

    protected Edge createFirstPart() {
        return new Edge(p1, getCenter());
    }

    protected Edge createSecondPart() {
        return new Edge(getCenter(), p2);
    }

    public Triangle getLeft() {
        return left;
    }

    public void setLeft(Triangle t) {
        left = t;
    }

    public Triangle getRight() {
        return right;
    }

    public void setRight(Triangle t) {
        right = t;
    }

    public void setTriangle(Point2D p1, Point2D p2, Triangle t) {
        if (this.p1 == p1) {
            assert this.p2 == p2;
            left = t;
        }
        else {
            assert this.p1 == p2;
            assert this.p2 == p1;
            right = t;
        }
    }

    public double lengthSq() {
        return p1.distanceSq(p2);
    }

    public Triangle otherTriangle(Triangle t) {
        if (left == t)
            return right;
        assert right == t;
        return left;
    }

}
