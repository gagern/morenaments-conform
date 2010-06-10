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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.Mat3x3R;

public class HypLayout {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private static final Logger logger = Logger.getLogger(HypLayout.class);

    private Triangle orbifoldCenter;

    protected List<HypTrafo> insidenessChecks;

    private HashMap<Vertex, Integer> angleCounts;

    private List<Triangle> triangles;

    public HypLayout(Triangle orbifoldCenter,
                     List<HypTrafo> insidenessChecks) {
        this.orbifoldCenter = orbifoldCenter;
        this.insidenessChecks = insidenessChecks;
        triangles = new ArrayList<Triangle>();
    }

    public void setAngles(Map<Vertex, Double> angles) {
        angleCounts = new HashMap<Vertex, Integer>(angles.size()*4/3 + 1);
        for (Map.Entry<Vertex, Double> e: angles.entrySet()) {
            double c = Math.PI*2/e.getValue();
            angleCounts.put(e.getKey(), (int)(c + .5));
        }
    }

    /**
     * Calculate layout by determining suitable vertex coordinates.
     * This is the main method of this class.
     */
    public Triangulation layout() {
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
        return new Triangulation(triangles);
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
        logger.trace("Lengths: ab=" + abLen +
                     ", bc=" + bcLen + ", ca=" + caLen);

        HypEdgePos abPos, bcPos, caPos;
        abPos = new HypEdgePos();
        abPos.setVertex(aOrb);
        es[2].setHypPos(abPos);
        double beta = hypAngle(abLen, bcLen, caLen);
        bcPos = abPos.derive(bOrb, abLen, -beta);
        es[0].setHypPos(bcPos);
        double alpha = hypAngle(caLen, abLen, bcLen);
        caPos = abPos.derive(aOrb, abLen, alpha);
        es[1].setHypPos(caPos);
        logger.trace("alpha=" + alpha + ", beta=" + beta);
        logger.trace("Edge positions: ab=" + abPos +
                     ", bc=" + bcPos + ", ca=" + caPos);

        vs[0].setLocation(0, 0);
        vs[1].setLocation(abPos.derive(bOrb, abLen).dehomogenize());
        vs[2].setLocation(caPos.derive(cOrb, caLen).dehomogenize());

        Triangle tFlat = new Triangle(vs[0], vs[1], vs[2],
                                      es[0], es[1], es[2]);
        logger.trace("Start triangle: " + tFlat);
        tFlat.registerWithEdges();
        tFlat.setOrbifoldElement(tOrb);
        tFlat.setProj(tOrb.getProj());
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
        if (eInFlat.otherTriangle(tInFlat) != null)
            return null; // already know our neighbour
        Triangle tInOrb = tInFlat.getOrbifoldElement();
        Edge eInOrb = eInFlat.getOrbifoldElement();
        logger.trace("layout: tInFlat=" + tInFlat + ", eInFlat=" + eInFlat +
                     ", tInOrb=" + tInOrb + ", eInOrb=" + eInOrb);
        Triangle tOutOrb = eInOrb.otherTriangle(tInOrb);
        if (tOutOrb == null)
            return null; // triangle at reflecting boundary
        Vertex aFlat = eInFlat.getP2(), bFlat = eInFlat.getP1();
        Vertex aOrb = eInOrb.getP2(), bOrb = eInOrb.getP1();
        if (aFlat.getOrbifoldElement() != aOrb) {
            Vertex tmp = bOrb;
            bOrb = aOrb;
            aOrb = tmp;
        }
        assert aFlat.getOrbifoldElement() == aOrb;
        assert bFlat.getOrbifoldElement() == bOrb;
        TwoMeshCircler tmca, tmcb;
        tmca = new TwoMeshCircler(tInFlat, eInFlat, aFlat,
                                  tInOrb, eInOrb, aOrb,
                                  angleCounts.get(aOrb), tOutOrb);
        tmcb = new TwoMeshCircler(tInFlat, eInFlat, bFlat,
                                  tInOrb, eInOrb, bOrb,
                                  angleCounts.get(bOrb), tOutOrb);
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
            logger.trace("beta=" + beta);
            assert beta > 0 && beta < Math.PI;
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
            logger.trace("alpha=" + alpha);
            assert alpha > 0 && alpha < Math.PI;
            caPos = abPos.derive(aOrb, abLen, alpha);
            caFlat.setHypPos(caPos);
        }
        else {
            caPos = caFlat.getHypPos();
        }

        if (cNeedPos) {
            Point2D caC = caPos.derive(cOrb, caLen).dehomogenize();
            Point2D bcC = bcPos.derive(cOrb, bcLen).dehomogenize();
            double caX = caC.getX(), caY = caC.getY();
            double bcX = bcC.getX(), bcY = bcC.getY();
            assert Math.abs(caX - bcX) < 1e-5 && Math.abs(caY - bcY) < 1e-5:
            "x coordinates should match but differ by " + caC.distance(bcC);
            double x = (caX + bcX)/2;
            double y = (caY + bcY)/2;
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
            Mat3x3R p = tOutOrb.getProj();
        }
        else {
            tOutFlat = new Triangle(cFlat, bFlat, aFlat,
                                    eInFlat, caFlat, bcFlat);
            tOutFlat.setProj(tOutOrb.getProj());
        }
        logger.trace("Created triangle " + tOutFlat);
        tOutFlat.registerWithEdges();
        tOutFlat.setOrbifoldElement(tOutOrb);
        Mat3x3R pOrb = tOutOrb.getProj(), pFlat = new Mat3x3R();
        for (int colFlat = 0; colFlat < 3; ++colFlat) {
            int colOrb = tOutOrb.indexOf(tOutFlat.vertices()
                                         .get(colFlat).getOrbifoldElement());
            for (int row = 0; row < 3; ++row) {
                pFlat.set(row, colFlat, pOrb.get(row, colOrb));
            }
        }
        tOutFlat.setProj(pFlat);
        triangles.add(tOutFlat);
        return tOutFlat;
    }

    protected boolean triangleIsRelevant(Point2D... points) {
        Vec2C tmp = new Vec2C();
        CHECKS: for (HypTrafo check: insidenessChecks) {
            for (Point2D p: points) {
                tmp.assign(p.getX(), p.getY(), 1, 0);
                check.transform(tmp, tmp);
                double violation = tmp.x.r*tmp.y.i - tmp.x.i*tmp.y.r;
                if (violation < -1e-10)
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
                       Integer angleCount, Triangle targetOrb) {
            logger.trace("Circler starting in tFlat=" + tFlat +
                         ", eFlat=" + eFlat +
                         ", vFlat=" + vFlat +
                         ", tOrb=" + tOrb +
                         ", eOrb=" + eOrb +
                         ", vOrb=" + vOrb);
            int count;
            if (angleCount != null) count = angleCount;
            else count = 1;
            while(tFlat != null) {
                // make sure we are in sync
                assert tFlat.getOrbifoldElement() == tOrb;
                assert eFlat.getOrbifoldElement() == eOrb;
                assert vFlat.getOrbifoldElement() == vOrb;

                // change edge
                eFlat = tFlat.otherEdge(eFlat, vFlat);
                eOrb = tOrb.otherEdge(eOrb, vOrb);
                logger.trace("Changed edge to eFlat=" + eFlat +
                             ", eOrb=" + eOrb);

                // change triangle
                tFlat = eFlat.otherTriangle(tFlat);
                tOrb = eOrb.otherTriangle(tOrb);
                logger.trace("Changed triangle to tFlat=" + tFlat +
                             ", tOrb=" + tOrb);

                // look whether we've reached our target triangle
                if (tOrb != targetOrb) continue;
                if (--count != 0) continue;

                // found what we've been looking for, so remember it
                logger.trace("Circler found something");
                this.tFlat = tFlat;
                this.eFlat = eFlat;
                this.eOrb = eOrb;
                return;
            }
            logger.trace("Circler found nothing");
        }

    }

}
