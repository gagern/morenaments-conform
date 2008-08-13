package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

class AdaptiveEdgeSubdivision extends MeshCreator {

    private Collection<Triangle> triangles;

    private PriorityQueue<Edge> q;

    protected Collection<Triangle> createMesh(Triangle t) {
        int targetCount = getTargetTriangleCount();
        triangles = new ArrayList<Triangle>();
        triangles.add(t);
        q = new PriorityQueue(targetCount*3, new EdgeLengthComparator());
        for (Edge e: t.edges()) q.add(e);
        while (triangles.size() < targetCount)
            subdivide(q.remove());
        q = null;
        return triangles;
    }

    private void subdivide(Edge e) {
        Triangle tl = e.getLeft();
        Triangle tr = e.getRight();
        Point2D m = e.getCenter();
        Edge e1 = e.getFirstPart();
        Edge e2 = e.getSecondPart();
        q.add(e1);
        q.add(e2);
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

}
