package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

public class Edge {

    private Vertex p1;

    private Vertex p2;

    private Triangle left;

    private Triangle right;

    private Vertex center;

    private Edge firstPart;

    private Edge secondPart;

    private Edge orbifoldElement;

    private double length = Double.NaN;

    private HypEdgePos hypPos;

    public Edge(Vertex p1, Vertex p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Vertex getP1() {
        return p1;
    }

    public Vertex getP2() {
        return p2;
    }

    public Vertex getCenter() {
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

    public Edge getPart(Vertex endpoint) {
        if (endpoint == p1) {
            return getFirstPart();
        }
        else {
            assert endpoint == p2;
            return getSecondPart();
        }
    }

    protected Vertex createCenter() {
        return new Vertex((p1.getX() + p2.getX())/2.,
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

    public void setTriangle(Vertex p1, Vertex p2, Triangle t) {
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

    public void initTriangle(Vertex p1, Vertex p2, Triangle t) {
        if (this.p1 == p1) {
            assert this.p2 == p2;
            assert left == null: "Shouldn't overwrite existing neighbour";
            left = t;
        }
        else {
            assert this.p1 == p2;
            assert this.p2 == p1;
            assert right == null: "Shouldn't overwrite existing neighbour";
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

    public Vertex otherVertex(Vertex v) {
        if (p1 == v)
            return p2;
        assert p2 == v;
        return p1;
    }

    public Edge getOrbifoldElement() {
        return orbifoldElement;
    }

    public void setOrbifoldElement(Edge orbifoldElement) {
        this.orbifoldElement = orbifoldElement;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public HypEdgePos getHypPos() {
        return hypPos;
    }

    public void setHypPos(HypEdgePos hypPos) {
        this.hypPos = hypPos;
    }

    public boolean hasEndpoints(Vertex v1, Vertex v2) {
        return (this.p1 == v1 && this.p2 == v2)
            || (this.p1 == v2 && this.p2 == v1);
    }

    @Override public String toString() {
        return "Edge[" + p1 + ", " + p2 + "]";
    }

}
