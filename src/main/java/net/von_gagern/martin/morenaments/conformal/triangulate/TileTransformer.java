package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.morenaments.conformal.groups.Group;
import net.von_gagern.martin.morenaments.conformal.Mat3x3R;

public class TileTransformer implements Runnable {

    private Group g;

    private Triangulation hypMesh;

    public TileTransformer(Group group) {
        this.g = group;
    }

    public void transform() throws MeshException {
        // Approximate hyperbolic tile by a triangulated mesh
        List<Point2D> hypCorners = g.getHypTileCorners();
        hypMesh = new Triangulation();
        hypMesh.triangulatePoincare(hypCorners);

        // Transform mesh to euclidean shape using discrete conformal map
        Conformal c = Conformal.getInstance(hypMesh);
        Map<Point2D, Double> angles =
            new HashMap<Point2D, Double>(hypCorners.size());
        for (int i = 0; i < hypCorners.size(); ++i) {
            Double angle = g.getEuclideanCornerAngle(i);
            angles.put(hypCorners.get(i), angle);
        }
        ResultMesh eucMesh = c.transform();

        /* For every triangle, find a projective transformation which
         * turns points from the hyperbolic Poincaré model into points
         * in the euclidean unit tile. The idea is to turn corrdinates
         * first into barycentric coordinates, next scale these by the
         * parameters u_i from the discrete conformat mapping, turn
         * them into coordinates in the euclidean plane, and finally
         * shifting the tile to a predefined position using an affine
         * transform.
         */
        List<Point2D> eucCorners = g.getEucTileCorners();
        Mat3x3R srcCorners = triple(eucMesh, hypCorners.get(0),
                                    hypCorners.get(1), hypCorners.get(2));
        Mat3x3R dstCorners = triple(eucCorners.get(0),
                                    eucCorners.get(1), eucCorners.get(2));
        Mat3x3R affine = dstCorners.multiply(srcCorners.getInverse());
        for (Triangle t: hypMesh) {
            List<Point2D> vs = t.vertices();
            Point2D v1 = vs.get(0), v2 = vs.get(1), v3 = vs.get(2);
            Mat3x3R hypTriple = triple(hypMesh, v1, v2, v3);
            Mat3x3R eucTriple = triple(eucMesh, v1, v2, v3);
            Mat3x3R diag = diag(Math.exp(-eucMesh.getU(v1)),
                                Math.exp(-eucMesh.getU(v2)),
                                Math.exp(-eucMesh.getU(v3)));
            Mat3x3R m = affine.multiply(eucTriple).multiply(diag)
                              .multiply(hypTriple.getInverse());
            t.setProj(m);
        }
    }

    private <V> Mat3x3R triple(LocatedMesh<V> m, V c1, V c2, V c3) {
        return new Mat3x3R(m.getX(c1), m.getX(c2), m.getX(c3),
                           m.getY(c1), m.getY(c2), m.getY(c3),
                                   1.,         1.,         1.);
    }

    private Mat3x3R triple(Point2D p1, Point2D p2, Point2D p3) {
        return new Mat3x3R(p1.getX(), p2.getX(), p3.getX(),
                           p1.getY(), p2.getY(), p3.getY(),
                                  1.,        1.,        1.);
    }

    private Mat3x3R diag(double d1, double d2, double d3) {
        return new Mat3x3R(d1, 0., 0.,
                           0., d2, 0.,
                           0., 0., d3);
    }

    /**
     * Cached exception intercepted by <code>run</code>
     */
    private MeshException meshException;

    /**
     * Cached exception intercepted by <code>run</code>
     */
    private RuntimeException runtimeException;

    /**
     * Runnable interface to the <code>transform</code> method.<p>
     *
     * This method allows performing the transformation in a different
     * thread. However, as a <code>call</code> method may not throw
     * any checked exceptions, special care has to be taken to catch
     * these exceptions later on. The main thread that was waiting for
     * the result should call <code>throwInterceptedExceptions</code>
     * to re-throw any exceptions that occurred during execution in a
     * different thread.<p>
     *
     * Any application not using multiple threads should rather call
     * <code>transform</code> directly to deal with exceptions more
     * easily.<p>
     *
     * @see #transform()
     * @see #throwInterceptedExceptions()
     */
    public void run() {
        try {
            transform();
        }
        catch (MeshException e) {
            meshException = e;
        }
        catch (RuntimeException e) {
            runtimeException = e;
        }
    }

    /**
     * Re-throw exceptions intercepted by <code>run</code>.
     * This method must be called after every invocation of
     * <code>run</code> in order to clear and re-throw any exceptions
     * which occurred during the execution of <code>transform</code>.
     * @see #run()
     */
    public void throwInterceptedExceptions() throws MeshException {
        MeshException meshE = meshException;
        if (meshE != null) {
            meshException = null;
            throw meshE;
        }
        RuntimeException runE = runtimeException;
        if (runE != null) {
            runtimeException = null;
            throw runE;
        }
    }

}
