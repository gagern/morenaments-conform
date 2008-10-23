package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.von_gagern.martin.confoo.mesh.MetricMesh;
import net.von_gagern.martin.confoo.mesh.SimpleTriangle;

import net.von_gagern.martin.morenaments.conformal.Mat3x3R;

public abstract class EucOrbifold implements MetricMesh<Vertex> {

    public static final int UNIT = 36;

    public static final int HALF = UNIT/2;

    public static final int DOUBLE = UNIT*2;

    private static final double SCALE = 1./UNIT;

    protected Vertex[][] vs;

    protected Vertex[] specialPoints;

    private Point centerCoordinates;

    private Triangle center;

    private List<Triangle> ts;

    private HashMap<VertexPair, Edge> es;

    protected EucOrbifold(int w, int h) {
        w = UNIT*w + 1;
        h = UNIT*h + 1;
        vs = new Vertex[w][h];
        for (int x = 0; x < w; ++x)
            for (int y = 0; y < h; ++y)
                vs[x][y] = new Vertex(x, y);
        ts = new ArrayList<Triangle>(w*h*2);
        es = new HashMap<VertexPair, Edge>(w*h*4, 0.8f);
    }

    public EucOrbifold mesh(AffineTransform tr) {
        final double scale = 1./(1 << 12);
        double ax = tr.getScaleX(), ay = tr.getShearY();
        double bx = tr.getShearX(), by = tr.getScaleY();
        double cx = ax + bx, cy = ay + by;
        double dx = ax - by, dy = ay - by;
        double al = Math.hypot(ax, ay)*scale, bl = Math.hypot(bx, by)*scale;
        double cl = Math.hypot(cx, cy)*scale, dl = Math.hypot(dx, dy)*scale;
        if (cl < dl)
            mesh(al, bl, cl, false);
        else
            mesh(al, bl, dl, true);
        return this;
    }

    public void mesh(double la, double lb, double lc, boolean dc) {
        int ic = dc ? 1 : 0;
        Double len;
        len = Double.valueOf(la);
        for (int x = 1; x < vs.length; ++x)
            for (int y = 0; y < vs[x].length; ++y)
                registerLength(vs[x-1][y], vs[x][y], len);
        len = Double.valueOf(lb);
        for (int x = 0; x < vs.length; ++x)
            for (int y = 1; y < vs[x].length; ++y)
                registerLength(vs[x][y-1], vs[x][y], len);
        len = Double.valueOf(lc);
        for (int x = 1; x < vs.length; ++x) {
            for (int y = 1; y < vs[x].length; ++y) {
                registerLength(vs[x-ic][y], vs[x-1+ic][y-1], len);
                addTriangle(x-1, y-1, x  , y-1, x-ic  , y  );
                addTriangle(x  , y  , x-1, y  , x-1+ic, y-1);
                if (centerCoordinates != null &&
                    x == centerCoordinates.x && y == centerCoordinates.y)
                    center = ts.get(ts.size() - 1);
            }
        }
    }

    private void addTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        Vertex v1 = vs[x1][y1], v2 = vs[x2][y2], v3 = vs[x3][y3];
        Edge e1 = es.get(new VertexPair(v2, v3));
        Edge e2 = es.get(new VertexPair(v3, v1));
        Edge e3 = es.get(new VertexPair(v1, v2));
        Triangle t = new Triangle(v1, v2, v3, e1, e2, e3);
        t.registerWithEdges();
        t.setProj(new Mat3x3R(x1*SCALE, x2*SCALE, x3*SCALE,
                              y1*SCALE, y2*SCALE, y3*SCALE,
                                    1.,       1.,       1.));
        ts.add(t);
    }

    private void registerLength(Vertex v1, Vertex v2, Double len) {
        Edge e = new Edge(v1, v2);
        e.setLength(len);
        es.put(new VertexPair(v1, v2), e);
    }

    protected void setCenterCoordinates(int x, int y) {
        centerCoordinates = new Point(x + 1, y + 1);
    }

    public Triangle getCenter() {
        return center;
    }

    public double edgeLength(Vertex v1, Vertex v2) {
        return es.get(new VertexPair(v1, v2)).getLength();
    }

    public Iterator<Triangle> iterator() {
        return ts.iterator();
    }

    public Vertex[] getSpecialPoints() {
        return specialPoints;
    }

    public Collection<Edge> getEdges() {
        return es.values();
    }

    public Collection<Triangle> getTriangles() {
        return ts;
    }

    private static class VertexPair {

        private Vertex v1;

        private Vertex v2;

        public VertexPair(Vertex v1, Vertex v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        @Override public boolean equals(Object o) {
            if (o == null || !(o instanceof VertexPair)) return false;
            VertexPair that = (VertexPair)o;
            return (this.v1 == that.v1 && this.v2 == that.v2) ||
                (this.v1 == that.v2 && this.v2 == that.v1);
        }

        @Override public int hashCode() {
            int c1 = v1.hashCode(), c2 = v2.hashCode();
            return c1 ^ c2 ^ (17*(c1 & c2));
        }

    }

    static class Pmg extends EucOrbifold {

        public Pmg(int length) {
            super(length/2, length/2);
            assert (length & 3) == 0: "length must be divisible by 4";
            int half = length/2, quart = half/2;
            for (int i = 0; i <= quart; ++i) {
                vs[0][half - i] = vs[0][i]; // fold and glue left boundary
                vs[half][half - i] = vs[half][i]; // same for right boundary
            }
            specialPoints = new Vertex[] {
                vs[0][quart], vs[half][quart], // centers of rotations
                vs[0][0], vs[half][0] // corners of fundamental cell
            };
            setCenterCoordinates(quart, quart);
        }

    }

    static class Pgg extends EucOrbifold {

        public Pgg(int length) {
            super(length/2, length/2);
            assert (length & 1) == 0: "length must be even";
            int half = length/2;
            for (int i = 0; i <= half; ++i) {
                vs[half][half - i] = vs[0][i]; // twist and glue verticals
                vs[half - i][half] = vs[i][0]; // same for horizontals
            }
            specialPoints = new Vertex[] {
                vs[0][0], vs[half][0]
            };
            setCenterCoordinates(half/2, half/2);
        }

    }

    static class Cmm extends EucOrbifold {

        public Cmm(int length) {
            super(length, length);
            assert (length & 2) == 0: "length must be even";
            int half = length/2;
            for (int i = 0; i <= half; ++i) {
                vs[0][length - i] = vs[0][i]; // fold and glue left boundary
            }
            specialPoints = new Vertex[] {
                vs[0][half], vs[0][0], vs[length][0], vs[length][length]
            };
            setCenterCoordinates(half, half);
        }

    }


}
