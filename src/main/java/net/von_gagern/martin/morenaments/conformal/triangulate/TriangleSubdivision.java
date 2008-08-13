package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TriangleSubdivision extends MeshCreator {

    private List<Triangle> out;

    protected Collection<Triangle> createMesh(Triangle t) {
        int targetCount = getTargetTriangleCount();
        List<Triangle> in = new ArrayList<Triangle>();
        out = new ArrayList<Triangle>();
        in.add(t);
        while (in.size() < targetCount) {
            for (Triangle ti: in)
                subdivide(ti);
            List<Triangle> tmp = in;
            in = out;
            out = tmp;
            out.clear();
        }
        return in;
    }

    private void subdivide(Triangle t) {
        Point2D c = getCenter(t);
        Edge[] ec = new Edge[3];
        for (int i = 0; i < 3; ++i) {
            ec[i] = new Edge(t.vertices().get(i), c);
        }
        for (int i = 0; i < 3; ++i) {
            Edge e = t.edges().get(i);
            Point2D m = e.getCenter();
            Point2D a = t.vertices().get((i + 1)%3);
            Point2D b = t.vertices().get((i + 2)%3);
            Edge am = e.getPart(a);
            Edge mb = e.getPart(b);
            Edge mc = new Edge(m, c);
            Edge ac = ec[(i + 1)%3];
            Edge bc = ec[(i + 2)%3];
            out.add(new Triangle(a, m, c, mc, ac, am));
            out.add(new Triangle(m, b, c, bc, mc, mb));
        }
    }

    protected Point2D getCenter(Triangle t) {
        double x = 0, y = 0;
        for (Point2D p: t.vertices()) {
            x += p.getX();
            y += p.getY();
        }
        return new Point2D.Double(x/3., y/3.);
    }

}
