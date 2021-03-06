/*
 * morenaments conformal - Hyperbolization of ornaments
 *                         via discrete conformal maps
 * Copyright (C) 2009-2010 Martin von Gagern
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import de.tum.in.gagern.hornamente.PoincareLine;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;

public class Triangulation
    extends AbstractCollection<Triangle>
    implements LocatedMesh<Vertex>
{

    private double maxLengthSq = 1e-3;

    private Collection<Triangle> triangles;

    private PriorityQueue<Edge> q;

    public Triangulation() {
    }

    public Triangulation(Collection<Triangle> triangles) {
        this.triangles = triangles;
    }

    public double getMaxLength() {
        return Math.sqrt(maxLengthSq);
    }

    public void setMaxLength(double maxLength) {
        maxLengthSq = maxLength*maxLength;
    }

    private Edge poincareEdge(Vertex p1, Vertex p2) {
        PoincareLine l;
        l = new PoincareLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        Point2D c = l.circleCenter();
        if (c == null)
            return new Edge(p1, p2);
        double r1 = c.distance(p1), r2 = c.distance(p2);
        double r = (r1 + r2)/2;
        return new ArcEdge(p1, p2, c, r);
    }

    public void triangulatePoincarePoints(List<Point2D> corners) {
        List<Vertex> vertices = new ArrayList<Vertex>(corners.size());
        for (Point2D corner: corners) vertices.add(new Vertex(corner));
        triangulatePoincareVertices(vertices);
    }

    public void triangulatePoincareVertices(List<Vertex> corners) {
        triangles = new ArrayList<Triangle>(corners.size() - 2);
        delaunay(corners);
        assert triangles.size() == corners.size() - 2;
        triangulateImpl();
    }

    private Edge delaunay(List<Vertex> corners) {
        Vertex a = corners.get(corners.size() - 1), b = corners.get(0);
        Edge ab = poincareEdge(a, b);
        if (corners.size() > 2) {
            double ax = a.getX(), ay = a.getY();
            double bx = b.getX() - ax, by = b.getY() - ay, bz = bx*bx + by*by;
            // Find c, the corner with least violation (= maximum determinant)
            // of circumcircle predicate
            double maxDet = Double.NEGATIVE_INFINITY;
            int ci = -1;
            for (int i = 1; i < corners.size() - 1; ++i) {
                Vertex c = corners.get(i);
                double cx = c.getX() - ax, cy = c.getY() - ay;
                double cz = cx*cx + cy*cy;
                // Find d, causing maximum violation (= minimum determinant)
                // for triangle abc
                double minDet = Double.POSITIVE_INFINITY;
                for (int j = 2; j < corners.size(); ++j) {
                    if (j == i) continue;
                    Vertex d = corners.get(j);
                    double dx = d.getX() - ax, dy = d.getY() - ay;
                    double dz = dx*dx + dy*dy;
                    double det = bx*cy*dz + by*cz*dx + bz*cx*dy
                               - bx*cz*dy - by*cx*dz - bz*cy*dx;
                    if (minDet > det)
                        minDet = det;
                }
                if (maxDet > minDet) continue;
                maxDet = minDet;
                ci = i;
            }
            Vertex c = corners.get(ci);
            Edge bc = delaunay(corners.subList(0, ci + 1));
            Edge ca = delaunay(corners.subList(ci, corners.size()));
            Triangle t = new Triangle(a, b, c, bc, ca, ab);
            ab.setLeft(t);
            bc.setRight(t);
            ca.setRight(t);
            triangles.add(t);
        }
        return ab;
    }

    private void triangulateImpl() {
        Set<Edge> edgeSet = new HashSet<Edge>();
        for (Triangle t: triangles)
            edgeSet.addAll(t.edges());
        q = new PriorityQueue<Edge>(1000, EdgeLengthComparator.getInstance());
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
        Vertex m = e.getCenter();
        Edge e1 = e.getFirstPart();
        Edge e2 = e.getSecondPart();
        enqueue(e1);
        enqueue(e2);
        subdivide(tl, e, m, e1, e2);
        subdivide(tr, e, m, e2, e1);
    }

    private void subdivide(Triangle t, Edge ab, Vertex m, Edge am, Edge mb) {
        if (t == null) return;

        // get all the vertices and edges we need
        int i = t.edges().indexOf(ab);
        Vertex a = t.vertices().get((i + 1)%3);
        Vertex b = t.vertices().get((i + 2)%3);
        Vertex c = t.vertices().get(i);
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
        enqueue(mc);
    }

    public int size() {
        return triangles.size();
    }

    public Iterator<Triangle> iterator() {
        return triangles.iterator();
    }

    public double getX(Vertex v) {
        return v.getX();
    }

    public double getY(Vertex v) {
        return v.getY();
    }

    public double getZ(Vertex v) {
        return 0;
    }

    public double edgeLength(Vertex v1, Vertex v2) {
        return v1.distance(v2);
    }

}
