package net.von_gagern.martin.morenaments.conformal;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.ObjFormat;
import net.von_gagern.martin.confoo.mesh.flat.Mesh2D;
import net.von_gagern.martin.confoo.mesh.flat.Triangle2D;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import de.tum.in.gagern.hornamente.Vec3R;
import net.von_gagern.martin.morenaments.conformal.groups.Group;
import net.von_gagern.martin.morenaments.conformal.triangulate.Triangle;
import net.von_gagern.martin.morenaments.conformal.triangulate.Triangulation;

public class TileTransformer implements Runnable {

    private static Logger logger = Logger.getLogger(TileTransformer.class);

    private Group g;

    private List<Point2D> hypCorners;

    private Triangulation hypMesh;

    private Triangle centerTriangle;

    private Triangle lastUsedTriangle;

    public TileTransformer(Group group) {
        this.g = group;
    }

    public void transform() throws MeshException {
        // Approximate hyperbolic tile by a triangulated mesh
        logger.debug("Creating hyperbolic mesh");
        hypCorners = g.getHypTileCorners();
        hypMesh = new Triangulation();
        hypMesh.triangulatePoincare(hypCorners);
        dumpTriangles("hypMesh", hypMesh);
        logger.debug("Finding center");
        lastUsedTriangle = centerTriangle = findCenter(hypMesh);

        // Transform mesh to euclidean shape using discrete conformal map
        logger.debug("Transforming hyperbolic mesh");
        Conformal c = Conformal.getInstance(hypMesh);
        Map<Point2D, Double> angles =
            new HashMap<Point2D, Double>(hypCorners.size());
        for (int i = 0; i < hypCorners.size(); ++i) {
            Double angle = g.getEuclideanCornerAngle(i);
            angles.put(hypCorners.get(i), angle);
        }
        c.fixedBoundaryCurvature(angles);
        ResultMesh eucMesh = c.transform();
        dumpTriangles("eucMesh", eucMesh);

        /* For every triangle, find a projective transformation which
         * turns points from the hyperbolic Poincaré model into points
         * in the euclidean unit tile. The idea is to turn corrdinates
         * first into barycentric coordinates, next scale these by the
         * parameters u_i from the discrete conformat mapping, turn
         * them into coordinates in the euclidean plane, and finally
         * shifting the tile to a predefined position using an affine
         * transform.
         */
        logger.debug("Calculating projective transformations");
        List<Point2D> eucCorners = g.getEucTileCorners();
        Mat3x3R srcCorners = triple(eucMesh, hypCorners.get(0),
                                    hypCorners.get(1), hypCorners.get(2));
        Mat3x3R dstCorners = triple(eucCorners.get(0),
                                    eucCorners.get(1), eucCorners.get(2));
        Mat3x3R affine = dstCorners.multiply(srcCorners.getInverse());
        //affine = diag(1, 1, 1);
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
            //m = eucTriple.multiply(hypTriple.getInverse());
            t.setProj(m);
        }
        dumpTriangles("projMesh", projMesh(hypMesh));
        logger.debug("Mesh transformation complete");
    }

    private Mesh2D projMesh(List<Triangle> triangles) {
        List<Triangle2D> l = new ArrayList<Triangle2D>();
        Point2D[] ps = new Point2D[3];
        for (int i = 0; i < ps.length; ++i)
            ps[i] = new Point2D.Double();
        for (Triangle t: triangles) {
            Mat3x3R proj = t.getProj();
            for (int i = 0; i < 3; ++i) {
                Point2D c = t.getCorner(i);
                Vec3R v = new Vec3R(c.getX(), c.getY());
                v = proj.multiply(v);
                ps[i] = v.dehomogenize(ps[i]);
            }
            l.add(new Triangle2D(ps[0], ps[1], ps[2]));
        }
        return new Mesh2D(l);
    }

    private Triangle findCenter(List<Triangle> triangles) {
        double x = 0, y = 0;
        for (Point2D p: hypCorners) {
            x += p.getX();
            y += p.getY();
        }
        x /= hypCorners.size();
        y /= hypCorners.size();
        Point2D c = new Point2D.Double(x, y);
        Triangle t1 = triangles.get(0), t2;
        while (true) {
            t2 = t1.neighbourContaining(c);
            if (t1 == t2) return t1;
            t1 = t2;
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

    public Point2D transform(Point2D in, Point2D out) {
        Triangle t1 = lastUsedTriangle, t2;
        while (true) {
            t2 = t1.neighbourContaining(in);
            // logger.trace(t1 + " -> " + t2);
            if (t1 == t2) break;
            if (t2 == null) {
                break;
            }
            t1 = t2;
        }
        Vec3R v = new Vec3R(in.getX(), in.getY());
        v = t1.getProj().multiply(v);
        logger.trace("(" + in.getX() + ", " + in.getY() + ", 1) -> (" +
                     v.x + ", " + v.y + ", " + v.z + ") @ " + t1);
        lastUsedTriangle = t1;
        if (out == null)
            out = new Point2D.Double();
        return v.dehomogenize(out);
    }

    public BufferedImage render(PixelLookupSource source,
                                int size, BufferedImage target) {
        logger.debug("rendering image at size " + size);
        int r = size/2;
        size = r*2;
        if (target == null ||
            target.getWidth() != size || target.getHeight() != size) {
            target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }
        AffineTransform tr = SimplePixelLookupSource.unitDiskTransform(size);
        AffineTransform tri;
        try { tri = tr.createInverse(); }
        catch (NoninvertibleTransformException e) { throw new Error(e); }
        Point2D p = new Point2D.Double();
        tr.transform(hypCorners.get(0), p);
        Rectangle2D rect = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
        for (int i = 1; i < hypCorners.size(); ++i) {
            tr.transform(hypCorners.get(i), p);
            rect.add(p);
        }
        /* HypSimplexPredicate uses inverse transforms and is thus unsuited.
        HypSimplexPredicate pred =
            new HypSimplexPredicate(Arrays.asList());
        */
        Rectangle b = rect.getBounds();
        Vec2C v1 = new Vec2C(0, 0, 1, 0), v2 = new Vec2C();
        for (int y = b.y; y <= b.y + b.height; ++y) {
            PIXELS: for (int x = b.x; x <= b.x + b.width; ++x) {
                p.setLocation(x, y);
                tri.transform(p, p);
                v1.x.assign(p.getX(), p.getY());
                for (HypTrafo ic: g.getInsidenessChecks()) {
                    ic.transform(v1, v2);
                    if (v2.signImag() < 0) {
                        if (logger.isTraceEnabled())
                            logger.trace(String.format("Tile (%3d,%3d) outside",
                                                       x, y));
                        continue PIXELS;
                    }
                }
                transform(p, p);
                int rgb = source.getRGB(p);
                target.setRGB(x, y, rgb);
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Tile (%3d,%3d): %7f,%7f: %08x",
                                               x, y, p.getX(), p.getY(), rgb));
            }
        }
        logger.debug("rendered image at size " + size + " complete");
        return target;
    }

    private void dumpTriangles(String name, LocatedMesh<?> mesh) {
        double inset = 0.01;
        double width = 600, height = 600;
        File debugDir = new File("debug");
        if (!debugDir.isDirectory()) return;
        try {
            Mesh2D m2d = new Mesh2D(mesh);
            Rectangle2D rect = m2d.getBoundary().getBounds2D();
            if (inset > 0) {
                rect.add(rect.getMinX() - inset, rect.getMinY() - inset);
                rect.add(rect.getMaxX() + inset, rect.getMaxY() + inset);
            }
            double factor = Math.min(width/rect.getWidth(),
                                     height/rect.getHeight());
            width = factor*rect.getWidth();
            height = factor*rect.getHeight();
            File outFile = new File(debugDir, name + ".svg");
            FileOutputStream outStream = new FileOutputStream(outFile);
            SvgWriter svg = new SvgWriter(outStream);
            svg.head(rect, width, height);
            svg.writeTriangles(mesh);
            svg.tail();
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                ObjFormat obj = new ObjFormat(mesh, null);
                File outFile = new File(debugDir, "exception.obj");
                FileOutputStream outStream = new FileOutputStream(outFile);;
                obj.write(outStream);
                outStream.close();
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

}
