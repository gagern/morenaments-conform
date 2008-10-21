package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

class HypLayout {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(HypLayout.class);

    private Triangle orbifoldCenter;

    private List<HypTrafo> insidenessChecks;

    private List<Triangle> triangles;

    public HypLayout(Triangle orbifoldCenter,
                     List<HypTrafo> insidenessChecks) {
        this.orbifoldCenter = orbifoldCenter;
        this.insidenessChecks = insidenessChecks;
        triangles = new ArrayList<Triangle>();
    }

    /**
     * Calculate layout by determining suitable vertex coordinates.
     * This is the main method of this class.
     */
    public void layout() {
        // Sadly ArrayDeque was added only in java 1.6, so we won't use it yet
        Queue<Triangle> q = new LinkedList<Triangle>();
        q.add(layoutStart(orbifoldCenter));
        while (!q.isEmpty()) {
            Triangle t1 = q.remove();
            for (Edge e: t1.edges()) {
                Triangle t2 = layout(t1, e);
                if (t2 != null)
                    q.add(t2);
            }
        }
    }

    /**
     * Layout the initial triangle.
     * @param t the first triangle to be layed out
     */
    protected Triangle layoutStart(Triangle tOrb) {
        Vertex[] vs = new Vertex[3];
        Edge[] es = new Edge[3];
        for (int i = 0; i < 3; ++i) {
            vs[i] = new Vertex();
            vs[i].setOrbifoldElement(tOrb.vertices().get(i));
        }
        for (int i = 0; i < 3; ++i) {
            es[i] = new Edge(vs[(i + 1)%3], vs[(i + 2)%3]);
            es[i].setOrbifoldElement(tOrb.edges().get(i));
        }

        Vertex aOrb = tOrb.vertices().get(0);
        Vertex bOrb = tOrb.vertices().get(1);
        Vertex cOrb = tOrb.vertices().get(2);
        Edge bcOrb = tOrb.edges().get(0);
        Edge caOrb = tOrb.edges().get(1);
        Edge abOrb = tOrb.edges().get(2);
        double bcLen = bcOrb.getLength();
        double caLen = caOrb.getLength();
        double abLen = abOrb.getLength();

        HypEdgePos abPos, bcPos, caPos;
        abPos = new HypEdgePos();
        abPos.setVertex(vs[0]);
        es[2].setHypPos(abPos);
        double beta = hypAngle(abLen, bcLen, caLen);
        bcPos = abPos.derive(bOrb, abLen, -beta);
        es[0].setHypPos(bcPos);
        double alpha = hypAngle(caLen, abLen, bcLen);
        caPos = abPos.derive(cOrb, abLen, alpha);
        es[1].setHypPos(caPos);

        vs[0].setLocation(0, 0);
        vs[1].setLocation(abPos.derive(bOrb, abLen).dehomogenize());
        vs[2].setLocation(caPos.derive(cOrb, caLen).dehomogenize());

        Triangle tFlat = new Triangle(vs[0], vs[1], vs[2],
                                      es[0], es[1], es[2]);
        tFlat.registerWithEdges();
        tFlat.setOrbifoldElement(tOrb);
        triangles.add(tFlat);
        return tFlat;
    }

    /**
     * Create flat triangle and lay out third vertex.<p>
     *
     * @param tInFlat a triangle which already has been layed out
     * @param eInFlat an edge between tInFlat and the triangle to be layed out
     * @return a newly created Triangle or <code>null</code> if none was created
     */
    private Triangle layout(Triangle tInFlat, Edge eInFlat) {
        Triangle tInOrb = tInFlat.getOrbifoldElement();
        Edge eInOrb = eInFlat.getOrbifoldElement();
        Triangle tOutOrb = eInOrb.otherTriangle(tInOrb);
        if (tOutOrb == null) // triangle at reflecting boundary
            return null;
        Vertex aFlat = eInFlat.getP2(), bFlat = eInFlat.getP1();
        Vertex aOrb = eInOrb.getP2(), bOrb = eInOrb.getP1();
        if (aFlat.getOrbifoldElement() != aOrb) {
            Vertex tmp = bOrb;
            bOrb = aOrb;
            aOrb = tmp;
        }
        assert aFlat.getOrbifoldElement() == aOrb;
        assert bFlat.getOrbifoldElement() == bOrb;
        int c1 = 1, c2 = 1;
        TwoMeshCircler tmca, tmcb;
        tmca = new TwoMeshCircler(tInFlat, eInFlat, aFlat,
                                  tInOrb, eInOrb, aOrb,
                                  c1, tOutOrb);
        tmcb = new TwoMeshCircler(tInFlat, eInFlat, bFlat,
                                  tInOrb, eInOrb, bOrb,
                                  c2, tOutOrb);
        if (tmca.tFlat != null || tmcb.tFlat != null) {
            assert tmca.tFlat == tmcb.tFlat;
            return null; // triangle already layed out
        }

        Edge bcFlat = tmcb.eFlat, caFlat = tmca.eFlat;
        Edge bcOrb = tOutOrb.otherEdge(eInOrb, bOrb);
        Edge caOrb = tOutOrb.otherEdge(eInOrb, aOrb);
        Vertex cOrb = caOrb.otherVertex(aOrb);
        double abLen = eInOrb.getLength();
        double bcLen = bcOrb.getLength();
        double caLen = caOrb.getLength();
        HypEdgePos abPos = eInFlat.getHypPos(), bcPos, caPos;
        Vertex cFlat;
        boolean cNeedPos = false;
        if (bcFlat != null) {
            cFlat = bcFlat.otherVertex(bFlat);
            assert caFlat == null || cFlat == caFlat.otherVertex(aFlat);
        }
        else if (caFlat != null) {
            cFlat = caFlat.otherVertex(aFlat);
        }
        else {
            cFlat = new Vertex();
            cFlat.setOrbifoldElement(cOrb);
            cNeedPos = true;
        }
        if (bcFlat == null) {
            bcFlat = new Edge(bFlat, cFlat);
            bcFlat.setOrbifoldElement(bcOrb);
            double beta = hypAngle(abLen, bcLen, caLen);
            bcPos = abPos.derive(bOrb, abLen, -beta);
            bcFlat.setHypPos(bcPos);
        }
        else {
            bcPos = bcFlat.getHypPos();
        }
        if (caFlat == null) {
            caFlat = new Edge(cFlat, aFlat);
            caFlat.setOrbifoldElement(caOrb);
            double alpha = hypAngle(caLen, abLen, bcLen);
            caPos = abPos.derive(cOrb, abLen, alpha);
            caFlat.setHypPos(caPos);
        }
        else {
            caPos = caFlat.getHypPos();
        }

        if (cNeedPos) {
            Point2D caC = caPos.derive(cOrb, caLen).dehomogenize();
            Point2D bcC = bcPos.derive(cOrb, bcLen).dehomogenize();
            double x = (caC.getX() + bcC.getX())/2;
            double y = (caC.getY() + bcC.getY())/2;
            assert !Double.isNaN(x): "x must not be NaN";
            assert !Double.isNaN(y): "y must not be NaN";
            assert !Double.isInfinite(x): "x must be finite";
            assert !Double.isInfinite(y): "y must be finite";
            cFlat.setLocation(x, y);
        }

        if (!triangleIsRelevant(aFlat, bFlat, cFlat))
            return null;

        Triangle tOutFlat;
        if (Triangle.ccw(aFlat, bFlat, cFlat) > 0) {
            tOutFlat = new Triangle(aFlat, bFlat, cFlat,
                                    bcFlat, caFlat, eInFlat);
        }
        else {
            tOutFlat = new Triangle(cFlat, bFlat, aFlat,
                                    eInFlat, caFlat, bcFlat);
        }
        tOutFlat.registerWithEdges();
        tOutFlat.setOrbifoldElement(tOutOrb);
        triangles.add(tOutFlat);
        return tOutFlat;
    }

    private boolean triangleIsRelevant(Point2D... points) {
        Vec2C tmp = new Vec2C();
        CHECKS: for (HypTrafo check: insidenessChecks) {
            for (Point2D p: points) {
                tmp.assign(p.getX(), p.getY(), 1, 0);
                check.transform(tmp, tmp);
                double violation = tmp.x.r*tmp.y.i - tmp.x.i*tmp.y.r;
                if (violation < 0)
                    continue CHECKS;
            }
            // all points outside a given check => not relevant
            return false;
        }
        // all points inside at least one check => relevant
        return true;
    }

    private static double hypAngle(double len1, double len2,
                                   double lenOpposite) {
        if (lenOpposite >= len1 + len2) return Math.PI;
        if (len1 >= len2 + lenOpposite || len2 >= len1 + lenOpposite) return 0;
        double nom = Math.sinh((lenOpposite + len1 - len2)/2.);
        nom *= Math.sinh((lenOpposite + len2 - len1)/2.);
        double denom = Math.sinh((len1 + len2 - lenOpposite)/2.);
        denom *= Math.sinh((len1 + len2 + lenOpposite)/2.);
        if (nom < denom)
            return 2.*Math.atan(Math.sqrt(nom/denom));
        else
            return Math.PI - 2.*Math.atan(Math.sqrt(denom/nom));
    }

    private static class TwoMeshCircler {

        Triangle tFlat;

        Edge eFlat;

        Edge eOrb;

        TwoMeshCircler(Triangle tFlat, Edge eFlat, Vertex vFlat,
                       Triangle tOrb, Edge eOrb, Vertex vOrb,
                       int count, Triangle targetOrb) {
            while(tFlat != null) {
                // make sure we are in sync
                assert tFlat.getOrbifoldElement() == tOrb;
                assert eFlat.getOrbifoldElement() == eOrb;
                assert vFlat.getOrbifoldElement() == vOrb;

                // change edge
                eFlat = tFlat.otherEdge(eFlat, vFlat);
                eOrb = tOrb.otherEdge(eOrb, vOrb);

                // change triangle
                tFlat = eFlat.otherTriangle(tFlat);
                tOrb = eOrb.otherTriangle(tOrb);

                // look whether we've reached our target triangle
                if (tOrb != targetOrb) continue;
                if (--count != 0) continue;

                // found what we've been looking for, so remember it
                this.tFlat = tFlat;
                this.eFlat = eFlat;
                this.eOrb = eOrb;
                return;
            }
        }

    }

}
