package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.Complex;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

class GridRenderer extends TilingRenderer {

    private final Logger logger = Logger.getLogger(GridRenderer.class);

    private boolean colored = false;

    private double eucThinness = 100.;

    private double hypThinness = 50.;

    private double minHypDist, maxHypDist, minEucDist, maxEucDist;

    public GridRenderer(Group group) {
        super(group);
    }

    public BufferedImage render(int size, BufferedImage target) {
        minEucDist = minHypDist = Double.POSITIVE_INFINITY;
        maxEucDist = maxHypDist = Double.NEGATIVE_INFINITY;
        target = super.render(null, size, target);
        logger.debug("hyp dist: " + minHypDist + " -- " + maxHypDist);
        logger.debug("euc dist: " + minEucDist + " -- " + maxEucDist);
        return target;
    }

    protected int getColor(HypTrafo t, Vec2C v1) {
        Point2D p1 = v1.dehomogenize(new Point2D.Double());
        Vec2C v2 = t.inverseTransform(v1, new Vec2C());
        if (colored) {
            int color = 0;
            for (int i = 0; i < insidenessChecks.length; ++i) {
                color |= edgeColor(t, p1, v2, insidenessChecks[i]);
            }
            return color;
        }
        else {
            for (int i = 0; i < insidenessChecks.length; ++i) {
                if (edgeDist(t, p1, v2, insidenessChecks[i]) < 1.)
                    return 0xff000000;
            }
            return 0xffffffff;
        }
    }

    private int edgeColor(HypTrafo t, Point2D p1, Vec2C v2, HypTrafo edge) {
        double h = hypThinness*hypDist(v2, edge);
        double e = eucThinness*eucDist(t, p1, v2, edge);
        if (minHypDist > h) minHypDist = h;
        if (maxHypDist < h) maxHypDist = h;
        if (minEucDist > e) minEucDist = e;
        if (maxEucDist < e) maxEucDist = e;
        int color = 0xff000000;
        if (h < .5) color |= 0x0000ff;
        if (e < .5) color |= 0xff0000;
        if (h + e < 1.) color |= 0x00ff00;
        return color;
    }

    private double edgeDist(HypTrafo t, Point2D p1, Vec2C v2, HypTrafo edge) {
        double h = hypThinness*hypDist(v2, edge);
        double e = eucThinness*eucDist(t, p1, v2, edge);
        if (minHypDist > h) minHypDist = h;
        if (maxHypDist < h) maxHypDist = h;
        if (minEucDist > e) minEucDist = e;
        if (maxEucDist < e) maxEucDist = e;
        return h + e;
    }

    private double hypDist(Vec2C v, HypTrafo edge) {
        Vec2C v2 = edge.transform(v, new Vec2C());
        Complex z1 = v2.x.clone().div(v2.y);
        Complex z2 = z1.clone().conj();
        Complex z3 = v2.y.clone().div(v2.x);
        Complex z4 = z3.clone().conj();
        Complex cr = z1.clone().sub(z3).mul(z2.clone().sub(z4));
        cr.div(z1.sub(z4).mul(z2.sub(z3)));
        assert Math.abs(cr.i) < 1e-6 : "Cross ratio expected to be real";
        double d = Math.log(cr.r);
        return d;
    }

    private double eucDist(HypTrafo t, Point2D p1, Vec2C v2, HypTrafo edge) {
        Vec2C v3 = edge.transform(v2, new Vec2C()).dehomogenize();
        double k = v3.x.r*2./(1. + v3.x.absSq()); // Kleinian x coordinate
        double b = 1/(k*k);
        double p = (b - Math.sqrt(b*b - b))*k; // Poincare x coordinate
        v3.x.assign(p, 0.);
        v3.normalize();
        edge.inverseTransform(v3, v3);
        t.transform(v3, v3);
        double d = p1.distance(v3.dehomogenize(new Point2D.Double()));
        return d;
    }

}
