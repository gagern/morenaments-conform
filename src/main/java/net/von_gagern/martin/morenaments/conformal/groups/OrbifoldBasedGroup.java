package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.Geometry;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.MetricMesh;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.TileTransformer;
import net.von_gagern.martin.morenaments.conformal.triangulate.EucOrbifold;
import net.von_gagern.martin.morenaments.conformal.triangulate.Triangulation;
import net.von_gagern.martin.morenaments.conformal.triangulate.Vertex;

abstract class OrbifoldBasedGroup extends Group {

    private final Logger logger = Logger.getLogger(OrbifoldBasedGroup.class);

    protected OrbifoldBasedGroup(int... euclideanAngles) {
        super(euclideanAngles);
    }

    protected Vec2C[] hypCorners;

    protected Vec2C[] hypEdges;

    protected double eucBaseAngle = Double.NaN;

    protected HypTrafo[] constructInsidenessChecks() {
        if (hypCorners == null) init();
        final int n = hypCorners.length;
        HypTrafo[] edges = new HypTrafo[n];
        for (int i = 0; i < n; ++i) {
            Vec2C v1 = hypCorners[i], v2 = hypCorners[(i + 1)%n];
            // we want v1 in the origin and v2 on the positive real ray
            HypTrafo t1 = new HypTrafo(v1, false);
            t1.invert(); // t now moves v1 to origin
            Vec2C v2a = t1.transform(v2, new Vec2C());
            // TODO: Avoid dehomogenization, do this homogenously
            Point2D p2 = v2a.dehomogenize(new Point2D.Double());
            double alpha = Math.atan2(p2.getY(), p2.getX());
            HypTrafo t2 = HypTrafo.getRotation(-alpha);
            edges[i] = HypTrafo.product(t2, t1);
        }
        return edges;
    }

    public List<Point2D> getHypTileCorners() {
        if (hypCorners == null) init();
        ArrayList<Point2D> corners = new ArrayList<Point2D>(hypCorners.length);
        for (int i = 0; i < hypCorners.length; ++i)
            corners.add(hypCorners[i].dehomogenize(new Point2D.Double()));
        if (logger.isDebugEnabled())
            for (Point2D p: corners)
                logger.debug("Corner at (" + p.getX() + ", " + p.getY() + ")");
        return corners;
    }

    private void init() {
        getTriangulation();
    }

    @Override public Triangulation getTriangulation() {
        logger.debug("Creating euclidean mesh");
        EucOrbifold eo = createEucOrbifold();
        eo.mesh(getEuclideanTransform());
        // TileTransformer.dumpTriangles("eucOrbifold", eo); // not located!
        Vertex[] sp = eo.getSpecialPoints();
        ResultMesh<Vertex> ho; // hyperbolic orbifold
        try {
            logger.debug("Transforming to hyperbolic mesh");
            Conformal<Vertex> c = Conformal.getInstance(eo);
            Map<Vertex, Double> angles = getHypAngles(sp, hyperbolicAngles);
            c.setAngleErrorBound(1e-10);
            c.fixedBoundaryCurvature(angles);
            c.setOutputGeometry(Geometry.HYPERBOLIC);
            c.setLayoutStartTriangle(eo.getCenter());
            ho = c.transform();
            TileTransformer.dumpTriangles("hypOrbifold", ho);
        }
        catch (MeshException e) {
            throw new RuntimeException(e);
        }
        Vec2C[] sv = new Vec2C[sp.length];
        for (int i = 0; i < sp.length; ++i) {
            Vertex spi = sp[i];
            double x = ho.getX(spi), y = ho.getY(spi);
            assert !Double.isNaN(x): "x must not be NaN";
            assert !Double.isNaN(y): "y must not be NaN";
            assert !Double.isInfinite(x): "x must be finite";
            assert !Double.isInfinite(y): "y must be finite";
            sv[i] = new Vec2C(x, y, 1, 0);
            logger.debug("Special point " + i + " at (" + x + ", " + y + ")");
        }
        hypCorners = constructCorners(sv);

        throw new UnsupportedOperationException("Not implemented");        
    }

    protected abstract EucOrbifold createEucOrbifold();

    protected abstract Vec2C[] constructCorners(Vec2C[] specialPoints);

    protected abstract Map<Vertex, Double>
        getHypAngles(Vertex[] specialPoints, int[] hyperbolicAngles);

    @Override public void setEuclideanTransform(AffineTransform tr) {
        super.setEuclideanTransform(tr);
        double ax = tr.getScaleX(), ay = tr.getShearY();
        double bx = tr.getShearX(), by = tr.getScaleY();
        double angle = ax*by - bx*ay;
        angle /= Math.hypot(ax, ay);
        angle /= Math.hypot(bx, by);
        angle = Math.asin(angle);
        eucBaseAngle = angle;
    }

    protected static Vec2C rotate(Vec2C point, Vec2C center, int angleCount) {
        HypTrafo t1 = new HypTrafo(center, false);
        HypTrafo rot = HypTrafo.getRotation(Math.PI*2/angleCount);
        HypTrafo t2 = t1.getInverse();
        HypTrafo t = HypTrafo.product(t1, rot, t2);
        return t.transform(point, new Vec2C());
    }

    protected HypTrafo reflection(int edgeIndex) {
        HypTrafo insideness = getInsidenessChecks()[edgeIndex];
        return HypTrafo.product(insideness.getInverse(),
                                HypTrafo.getConjugation(),
                                insideness);
    }

    protected HypTrafo rotation(int cornerIndex, int angleCount) {
        HypTrafo t1 = new HypTrafo(hypCorners[cornerIndex], false);
        HypTrafo rot = HypTrafo.getRotation(Math.PI*2/angleCount);
        HypTrafo t2 = t1.getInverse();
        return HypTrafo.product(t1, rot, t2);
    }

}
