package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.Geometry;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.Mat3x3R;
import net.von_gagern.martin.morenaments.conformal.TileTransformer;
import net.von_gagern.martin.morenaments.conformal.triangulate.Edge;
import net.von_gagern.martin.morenaments.conformal.triangulate.EucOrbifold;
import net.von_gagern.martin.morenaments.conformal.triangulate.HypLayout;
import net.von_gagern.martin.morenaments.conformal.triangulate.Triangle;
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
        if (logger.isDebugEnabled()) {
            HypTrafo ht = new HypTrafo();
            Vec2C v1 = new Vec2C(), v2 = new Vec2C();
            Point2D p1 = new Point2D.Double(), p2 = new Point2D.Double();
            int n = hypCorners.length;
            for (int i = 0; i < n; ++i) {
                logger.debug("Corner " + i + " at (" + corners.get(i).getX() +
                             ", " + corners.get(i).getY() + ")");
            }
            for (int i = 0; i < n; ++i) {
                ht.vec.assign(hypCorners[i]);
                ht.inverseTransform(hypCorners[(i + n - 1)%n], v1);
                ht.inverseTransform(hypCorners[(i + 1)%n], v2);
                v1.dehomogenize(p1);
                v2.dehomogenize(p2);
                double a1 = Math.atan2(p1.getY(), p1.getX());
                double a2 = Math.atan2(p2.getY(), p2.getX());
                double deg = 180./Math.PI*(a1 - a2);
                while (deg < 0) deg += 360;
                logger.debug("Corner " + i + " has angle of " + deg + "°");
            }
            for (int i = 0; i < n; ++i) {
                ht.vec.assign(hypCorners[i]);
                ht.inverseTransform(hypCorners[(i + 1)%n], v1);
                v1.dehomogenize(p1);
                double len = Math.hypot(p1.getX(), p1.getY());
                logger.debug("Edge " + i + " has length " + len);
            }
        }
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
        Map<Vertex, Double> angles; // target angles for corners & cone points
        try {
            logger.debug("Transforming to hyperbolic mesh");
            Conformal<Vertex> c = Conformal.getInstance(eo);
            angles = getHypAngles(sp, hyperbolicAngles);
            c.setAngleErrorBound(1e-12);
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

        // Change lengths stored in orbifold edges from euclidean to
        // hyperbolic. The euclidean aspects of the orbifold are
        // stored in the projective transformations of the triangles.
        for (Edge e: eo.getEdges())
            e.setLength(ho.edgeLength(e.getP1(), e.getP2()));
        HypLayout hl = new HypLayout(eo.getCenter(),
                                     Arrays.asList(getInsidenessChecks()));
        hl.setAngles(angles);
        Triangulation flat = hl.layout();
        Mat3x3R affine = new Mat3x3R(affineOrbifoldTransform());
        for (Triangle t: flat) {
            List<Vertex> vs = t.vertices();
            Vertex v1 = vs.get(0), v2 = vs.get(1), v3 = vs.get(2);
            Mat3x3R hypTriple = TileTransformer.triple(flat, v1, v2, v3);
            Mat3x3R eucTriple = t.getProj();
            Mat3x3R diag = TileTransformer.diag(
                Math.exp(ho.getU(v1.getOrbifoldElement())),
                Math.exp(ho.getU(v2.getOrbifoldElement())),
                Math.exp(ho.getU(v3.getOrbifoldElement())));
            assert eucTriple != null;
            Mat3x3R m = affine.multiply(eucTriple).multiply(diag)
                              .multiply(hypTriple.getInverse());
            t.setProj(m);
        }
        return flat;
    }

    protected abstract EucOrbifold createEucOrbifold();

    protected abstract Vec2C[] constructCorners(Vec2C[] specialPoints);

    protected abstract Map<Vertex, Double>
        getHypAngles(Vertex[] specialPoints, int[] hyperbolicAngles);

    protected AffineTransform affineOrbifoldTransform() {
        return new AffineTransform(.5, 0, 0, .5, 0, 0);
    }

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
