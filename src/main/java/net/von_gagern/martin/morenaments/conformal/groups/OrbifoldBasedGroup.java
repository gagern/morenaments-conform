package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.Geometry;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.morenaments.conformal.TileTransformer;

abstract class OrbifoldBasedGroup extends Group {

    protected OrbifoldBasedGroup(int... euclideanAngles) {
        super(euclideanAngles);
    }

    protected Vec2C[] hypCorners;

    protected Vec2C[] hypEdges;

    protected HypTrafo[] constructInsidenessChecks() {
        init();
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
        init();
        ArrayList corners = new ArrayList(hypCorners.length);
        for (int i = 0; i < hypCorners.length; ++i)
            corners.add(hypCorners[i].dehomogenize(new Point2D.Double()));
        return corners;
    }

    private void init() {
        if (hypCorners != null) return;
        EucOrbifold eo = createEucOrbifold();
        eo.mesh(getEuclideanTransform());
        // TileTransformer.dumpTriangles("eucOrbifold", eo); // not located!
        Object[] sp = eo.getSpecialPoints();
        ResultMesh<Object> ho; // hyperbolic orbifold
        try {
            Conformal<Object> c = Conformal.getInstance(eo);
            Map<Object, Double> angles = getHypAngles(sp, hyperbolicAngles);
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
            Object spi = sp[i];
            double x = ho.getX(spi), y = ho.getY(spi);
            sv[i] = new Vec2C(x, y, 1, 0);
        }
        hypCorners = constructCorners(sv);
    }

    protected abstract EucOrbifold createEucOrbifold();

    protected abstract Vec2C[] constructCorners(Vec2C[] specialPoints);

    protected abstract Map<Object, Double>
        getHypAngles(Object[] specialPoints, int[] hyperbolicAngles);

    protected HypTrafo reflection(int edgeIndex) {
        HypTrafo insideness = getInsidenessChecks()[edgeIndex];
        return HypTrafo.product(insideness.getInverse(),
                                HypTrafo.getConjugation(),
                                insideness);
    }

}
