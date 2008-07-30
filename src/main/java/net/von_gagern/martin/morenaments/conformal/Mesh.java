package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.*;
import java.util.*;
import java.io.*;

class Mesh {

    protected AffineTransform gridBase;

    protected boolean isTriangulated;

    private Map<Point2D, Vertex> vertices;

    private List<Face> faces;

    private List<Boundary> boundaries;

    private Map<BoundaryPair, Point2D> corners;

    public Mesh() {
        gridBase = new AffineTransform();
        isTriangulated = false;
        vertices = new HashMap<Point2D, Vertex>();
        faces = new ArrayList<Face>();
        boundaries = new ArrayList<Boundary>();
        corners = new HashMap<BoundaryPair, Point2D>();
    }

    public void addBoundary(Boundary b) {
        boundaries.add(b);
    }

    public Vertex vertex(Point2D p) {
        Vertex v = vertices.get(p);
        if (v == null) {
            v = new Vertex(p);
            v.setIndex(vertices.size() + 1);
            vertices.put(v, v);
        }
        return v;
    }

    public Vertex vertex(double x, double y) {
        return vertex(new Point2D.Double(x, y));
    }

    public Vertex vertex(Point2D p, int type) {
        Vertex v = vertex(p);
        if (v.getType() < type)
            v.setType(type);
        return v;
    }

    public Point2D gridPoint(int a, int b) {
        Point2D.Double p = new Point2D.Double(a, b);
        return gridBase.transform(p, p);
    }

    public Face face(Point2D... corners) {
        Vertex[] vs = new Vertex[corners.length];
        for (int i = 0; i < corners.length; ++i)
            vs[i] = vertex(corners[i]);
        Face f = new Face(vs);
        faces.add(f);
        return f;
    }

    public Face triangle(Point2D a, Point2D b, Point2D c) {
        double ax = a.getX(), bx = b.getX(), cx = c.getX();
        double ay = a.getY(), by = b.getY(), cy = c.getY();
        double d = ax*by + bx*cy + cx*ay - ax*cy - bx*ay - cx*by;
        if (d > 0) return face(a, b, c);
        else return face(a, c, b);
    }

    public void triangulate() {
        if (!isTriangulated) {
            doTriangulate();
            isTriangulated = true;
        }
    }

    protected void doTriangulate() {
    }

    protected void invalidate() {
        isTriangulated = false;
    }

    public String getName() {
        return "mesh";
    }

    public void write(String name) {
        try {
            write(new ObjWriter(new File(name + ".obj")));
            write(new CindyWriter(new File(name + ".cdy")));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void write(MeshWriter writer) throws IOException {
        triangulate();
        writer.name(getName());
        ArrayList<Vertex> vs = new ArrayList<Vertex>(vertices.values());
        Collections.sort(vs);
        int index = 0;
        for (Vertex v: vs) {
            v.setIndex(++index);
            writer.vertex(v);
        }
        for (Face f: faces) {
            writer.face(f);
        }
        writer.close();
    }

    public Intersection intersect(Point2D inside, Point2D outside) {
        double minDist = Double.POSITIVE_INFINITY;
        Boundary ib = null;
        Point2D ip = null;
        for (Boundary b: boundaries) {
            if (b.contains(outside)) continue;
            Point2D p = b.intersect(inside, outside);
            double d = p.distanceSq(inside);
            if (d >= minDist) continue;
            minDist = d;
            ib = b;
            ip = p;
        }
        return new Intersection(ib, vertex(ip, 1));
    }

    public Point2D intersect(Boundary b1, Boundary b2) {
        if (b1 == b2) return null;
        return corners.get(new BoundaryPair(b1, b2));
    }

    public boolean contains(Point2D v) {
        for (Boundary b: boundaries)
            if (!b.contains(v))
                return false;
        return true;
    }

    public void corner(Boundary b1, Boundary b2, Point2D p) {
        corners.put(new BoundaryPair(b1, b2), vertex(p, 2));
    }

}
