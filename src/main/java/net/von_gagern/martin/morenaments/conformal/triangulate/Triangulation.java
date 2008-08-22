package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import de.tum.in.gagern.hornamente.PoincareLine;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;

class Triangulation
    extends AbstractList<Triangle>
    implements LocatedMesh<Point2D>
{

    private double maxLengthSq = 1e-4;

    private List<Triangle> triangles;

    private PriorityQueue<Edge> q;

    public double getMaxLength() {
        return Math.sqrt(maxLengthSq);
    }

    public void setMaxLength(double maxLength) {
        maxLengthSq = maxLength*maxLength;
    }

    private Edge poincareEdge(Point2D p1, Point2D p2) {
        PoincareLine l;
        l = new PoincareLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        Point2D c = l.circleCenter();
        if (c == null)
            return new Edge(p1, p2);
        double r1 = c.distance(p1), r2 = c.distance(p2);
        double r = (r1 + r2)/2;
        return new ArcEdge(p1, p2, c, r);
    }

    public void triangulatePoincare(List<Point2D> corners) {
        triangles = new ArrayList<Triangle>(corners.size() - 2);
        Point2D a = corners.get(0), b = corners.get(1), c;
        Edge bc, ac, ab = poincareEdge(a, b);
        for (int i = 2; i < corners.size(); ++i) {
            c = corners.get(i);
            bc = poincareEdge(b, c);
            ac = poincareEdge(a, c);
            Triangle t = new Triangle(a, b, c, bc, ac, ab);
            ab.setLeft(t);
            bc.setLeft(t);
            ac.setRight(t);
            triangles.add(t);
            b = c;
            ab = ac;
        }
        assert triangles.size() == corners.size() - 2;
        triangulateImpl();
    }

    public void triangulate(Collection<Triangle> triangles) {
        this.triangles = new ArrayList<Triangle>(triangles);
        triangulateImpl();
    }

    private void triangulateImpl() {
        Set<Edge> edgeSet = new HashSet<Edge>();
        for (Triangle t: triangles)
            edgeSet.addAll(t.edges());
        q = new PriorityQueue(1000, EdgeLengthComparator.getInstance());
        for (Edge e: edgeSet)
            enqueue(e);
        while (!q.isEmpty())
            subdivide(q.remove());
        this.q = null;
    }

    public void enqueue(Edge e) {
        if (e.lengthSq() > maxLengthSq)
            q.add(e);
    }

    private void subdivide(Edge e) {
        Triangle tl = e.getLeft();
        Triangle tr = e.getRight();
        Point2D m = e.getCenter();
        Edge e1 = e.getFirstPart();
        Edge e2 = e.getSecondPart();
        enqueue(e1);
        enqueue(e2);
        subdivide(tl, e, m, e1, e2);
        subdivide(tr, e, m, e2, e1);
    }

    private void subdivide(Triangle t, Edge ab, Point2D m, Edge am, Edge mb) {
        if (t == null) return;

        // get all the vertices and edges we need
        int i = t.edges().indexOf(ab);
        Point2D a = t.vertices().get((i + 1)%3);
        Point2D b = t.vertices().get((i + 2)%3);
        Point2D c = t.vertices().get(i);
        Edge bc = t.edges().get((i + 1)%3);
        Edge ca = t.edges().get((i + 2)%3);
        Edge mc = new Edge(m, c);

        // t now becomes the triangle mbc
        t.vertices().set((i + 1)%3, m);
        t.edges().set(i, mb);
        t.edges().set((i + 2)%3, mc);
        mc.setRight(t);
        mb.setTriangle(m, b, t);

        // t2 is the new triangle amc
        Triangle t2 = new Triangle(a, m, c, mc, ca, am);
        mc.setLeft(t2);
        am.setTriangle(a, m, t2);
        ca.setTriangle(c, a, t2);

        // update data structures
        triangles.add(t2);
        q.add(mc);
    }

    public Triangle get(int index) {
        return triangles.get(index);
    }
    
    public int size() {
        return triangles.size();
    }

    public Iterator<Triangle> iterator() {
        return triangles.iterator();
    }

    public double getX(Point2D v) {
        return v.getX();
    }

    public double getY(Point2D v) {
        return v.getY();
    }

    public double getZ(Point2D v) {
        return 0;
    }

    public double edgeLength(Point2D v1, Point2D v2) {
        return v1.distance(v2);
    }

}
