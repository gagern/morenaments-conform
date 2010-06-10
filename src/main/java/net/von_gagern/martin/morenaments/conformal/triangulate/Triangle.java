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
import java.util.Arrays;
import java.util.List;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.morenaments.conformal.Mat3x3R;

public class Triangle implements CorneredTriangle<Vertex> {

    private List<Vertex> vs;

    private List<Edge> es;

    private Mat3x3R proj;

    private Triangle orbifoldElement;

    public Triangle(Vertex a, Vertex b, Vertex c,
                    Edge bc, Edge ca, Edge ab) {
        vs = Arrays.asList(a, b, c);
        es = Arrays.asList(bc, ca, ab);
        assert bc.hasEndpoints(b, c);
        assert ca.hasEndpoints(c, a);
        assert ab.hasEndpoints(a, b);
    }

    public List<Edge> edges() {
        return es;
    }

    public List<Vertex> vertices() {
        return vs;
    }

    public Vertex getCorner(int i) {
        return vs.get(i);
    }

    public Mat3x3R getProj() {
        return proj;
    }

    public void setProj(Mat3x3R proj) {
        this.proj = proj;
    }

    public Triangle neighbourContaining(Point2D p) {
        assert isCcw() : "triangle must have positive orientation";
        Point2D c1 = vs.get(2), c2;
        Triangle res = this;
        double minCcw = 0;
        for (int i = 0; i < 3; ++i) {
            c2 = vs.get(i);
            double ccw = ccw(c1, c2, p);
            c1 = c2;
            if (ccw >= 0) continue; // we are inside this edge
            if (res != null && ccw > minCcw) continue; // other edge is better
            Triangle other = es.get((i + 1)%3).otherTriangle(this);
            if (other == null && res != this) continue; // prefer non-null
            res = other;
            minCcw = ccw;
        }
        return res;
    }

    public void registerWithEdges() {
        for (int i = 0; i < 3; ++i)
            es.get(i).initTriangle(vs.get((i + 1)%3), vs.get((i + 2)%3), this);
    }

    public Edge otherEdge(Edge e, Vertex v) {
        int vi = indexOf(v);
        Edge e1 = es.get((vi + 1)%3), e2 = es.get((vi + 2)%3);
        if (e1 == e)
            return e2;
        assert e2 == e;
        return e1;
    }

    public int indexOf(Vertex v) {
        int vi = vs.indexOf(v);
        if (vi < 0)
            throw new IllegalArgumentException("not a vertex of this triangle");
        return vi;
    }

    public Triangle getOrbifoldElement() {
        return orbifoldElement;
    }

    public void setOrbifoldElement(Triangle orbifoldElement) {
        this.orbifoldElement = orbifoldElement;
    }

    private boolean isCcw() {
        return ccw(vs.get(0), vs.get(1), vs.get(2)) > 0;
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

    @Override public String toString() {
        return "Triangle[" + vs.get(0) + ", " + vs.get(1) + ", " + vs.get(2) +
               ", " + es.get(0) + ", " + es.get(1) + ", " + es.get(2) + "]";
    }

}
