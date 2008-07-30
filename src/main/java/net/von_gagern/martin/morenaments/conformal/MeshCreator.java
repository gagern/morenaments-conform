package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;
import java.util.Collection;
import de.tum.in.gagern.hornamente.PoincareLine;
import net.von_gagern.martin.cetm.mesh.Mesh2D;

abstract class MeshCreator {

    private int targetCount = 1296;

    abstract protected Collection<Triangle> createMesh(Triangle t);

    public int getTargetTriangleCount() {
        return targetCount;
    }

    public void setTargetTriangleCount(int count) {
        targetCount = count;
    }

    public Mesh2D createHypMesh(Point2D p1, Point2D p2, Point2D p3) {
        Edge e12 = hypEdge(p1, p2);
        Edge e23 = hypEdge(p2, p3);
        Edge e31 = hypEdge(p3, p1);
        Triangle t = new Triangle(p1, p2, p3, e23, e31, e12);
        return new Mesh2D(createMesh(t));
    }

    private ArcEdge hypEdge(Point2D p1, Point2D p2) {
        PoincareLine l;
        l = new PoincareLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        Point2D c = l.circleCenter();
        double r1 = c.distance(p1), r2 = c.distance(p2);
        double r = (r1 + r2)/2;
        return new ArcEdge(p1, p2, c, r);
    }

}
