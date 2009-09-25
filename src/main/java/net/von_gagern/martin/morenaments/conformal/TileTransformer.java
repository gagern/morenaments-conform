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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.conformal.ResultMesh;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
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
import net.von_gagern.martin.morenaments.conformal.triangulate.Vertex;

public class TileTransformer implements Runnable {

    private static final Logger logger =
        Logger.getLogger(TileTransformer.class);

    private Group g;

    private List<Vertex> hypCorners;

    private Triangulation hypMesh;

    private Triangle centerTriangle;

    private Triangle lastUsedTriangle;

    public TileTransformer(Group group) {
        this.g = group;
    }

    public void transform() throws MeshException {
        hypMesh = g.getTriangulation();
        hypCorners = new ArrayList<Vertex>(g.getHypTileCorners().size());
        for (Point2D corner : g.getHypTileCorners())
            hypCorners.add(new Vertex(corner));
        if (hypMesh != null) {
            dumpTriangles("hypMesh", hypMesh);
            lastUsedTriangle = centerTriangle = findCenter(hypMesh);
            if (logger.isTraceEnabled())
                dumpTriangles("hypMeshes", hypMesh, g.getGenerators());
            return;
        }

        // Approximate hyperbolic tile by a triangulated mesh
        logger.debug("Creating hyperbolic mesh");
        hypMesh = new Triangulation();
        hypMesh.triangulatePoincareVertices(hypCorners);
        dumpTriangles("hypMesh", hypMesh);
        logger.debug("Finding center");
        lastUsedTriangle = centerTriangle = findCenter(hypMesh);

        // Transform mesh to euclidean shape using discrete conformal map
        logger.debug("Transforming hyperbolic mesh");
        Conformal<Vertex> c = Conformal.getInstance(hypMesh);
        Map<Vertex, Double> angles =
            new HashMap<Vertex, Double>(hypCorners.size()*4/3 + 1);
        for (int i = 0; i < hypCorners.size(); ++i) {
            Double angle = g.getEuclideanCornerAngle(i);
            angles.put(hypCorners.get(i), angle);
        }
        c.fixedBoundaryCurvature(angles);
        ResultMesh<Vertex> eucMesh = c.transform();
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
            List<Vertex> vs = t.vertices();
            Vertex v1 = vs.get(0), v2 = vs.get(1), v3 = vs.get(2);
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
        dumpTriangles("affineMesh", new AffineMesh(eucMesh, affine));
        logger.debug("Mesh transformation complete");
    }

    private static class AffineMesh implements LocatedMesh<Vertex> {

        private LocatedMesh<Vertex> inMesh;

        private Mat3x3R mat;

        public AffineMesh(LocatedMesh<Vertex> inMesh, Mat3x3R transform) {
            this.inMesh = inMesh;
            mat = transform.clone();
            mat.scale(1/mat.get(2, 2));
        }

        public Iterator<? extends CorneredTriangle<? extends Vertex>>
        iterator() {
            return inMesh.iterator();
        }

        public double getX(Vertex v) {
            double x = inMesh.getX(v), y = inMesh.getY(v);
            return mat.get(0, 0)*x + mat.get(0, 1)*y + mat.get(0, 2);
        }

        public double getY(Vertex v) {
            double x = inMesh.getX(v), y = inMesh.getY(v);
            return mat.get(1, 0)*x + mat.get(1, 1)*y + mat.get(1, 2);
        }

        public double getZ(Vertex v) {
            return 0;
        }

        public double edgeLength(Vertex v1, Vertex v2) {
            double x1 = inMesh.getX(v1), y1 = inMesh.getY(v1);
            double x2 = inMesh.getX(v2), y2 = inMesh.getY(v2);
            double dx = mat.get(0, 0)*(x1 - x2) + mat.get(0, 1)*(y1 - y2);
            double dy = mat.get(1, 0)*(x1 - x2) + mat.get(1, 1)*(y1 - y2);
            return Math.hypot(dx, dy);
        }

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

    private Triangle findCenter(Collection<Triangle> triangles) {
        double x = 0, y = 0;
        for (Point2D p: hypCorners) {
            x += p.getX();
            y += p.getY();
        }
        x /= hypCorners.size();
        y /= hypCorners.size();
        Point2D c = new Point2D.Double(x, y);
        Triangle t1 = triangles.iterator().next(), t2;
        for (int i = 0; i < 100; ++i) {
            t2 = t1.neighbourContaining(c);
            if (t1 == t2) return t1;
            t1 = t2;
        }
        return t1;
    }

    public static <V> Mat3x3R triple(LocatedMesh<V> m, V c1, V c2, V c3) {
        return new Mat3x3R(m.getX(c1), m.getX(c2), m.getX(c3),
                           m.getY(c1), m.getY(c2), m.getY(c3),
                                   1.,         1.,         1.);
    }

    public static Mat3x3R triple(Point2D p1, Point2D p2, Point2D p3) {
        return new Mat3x3R(p1.getX(), p2.getX(), p3.getX(),
                           p1.getY(), p2.getY(), p3.getY(),
                                  1.,        1.,        1.);
    }

    public static Mat3x3R diag(double d1, double d2, double d3) {
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
        Triangle t1, t2;
        for (t1 = lastUsedTriangle; true; t1 = t2) {
            t2 = t1.neighbourContaining(in);
            // logger.trace(t1 + " -> " + t2);
            if (t1 == t2) break;
            if (t2 == null) {
                if (lastUsedTriangle != centerTriangle) {
                    t2 = lastUsedTriangle = centerTriangle; // fall back
                }
                else {
                    logger.warn("Dealing with pixel " + in +
                                " outside triangle mesh");
                    break;
                }
            }
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
        TilingRenderer tiling = new TilingRenderer(g);
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
        b.add(b.getMinX() - 1, b.getMinY() - 1);
        Vec2C v1 = new Vec2C(0, 0, 1, 0), v2 = new Vec2C();
        for (int y = b.y; y <= b.y + b.height; ++y) {
            for (int x = b.x; x <= b.x + b.width; ++x) {
                if (!pixelCornerInside(tri, x, y))
                    continue;
                // Logical integral coordinates are at pixel corners.
                // Therefore pixel centers are between integral coordinates.
                p.setLocation(x + .5, y + .5);
                tri.transform(p, p);
                v1.x.assign(p.getX(), p.getY());
                HypTrafo ht = tiling.findTrafo(v1, 32);
                ht.inverseTransform(v1, v2);
                v2.dehomogenize(p);
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

    private boolean pixelCornerInside(AffineTransform tr, double x, double y) {
        return pointInside(tr, x+.5, y+.5) ||
               pointInside(tr, x+1, y+1) || pointInside(tr, x, y) ||
               pointInside(tr, x+1, y) || pointInside(tr, x, y+1);
    }

    private boolean pointInside(AffineTransform tr, double x, double y) {
        Point2D p = new Point2D.Double(x, y);
        tr.transform(p, p);
        Vec2C v1 = new Vec2C(p.getX(), p.getY(), 1, 0), v2 = new Vec2C();
        for (HypTrafo ic: g.getInsidenessChecks()) {
            ic.transform(v1, v2);
            if (v2.signImag() < 0)
                return false;
        }
        return true;
    }

    public static void dumpTriangles(String name, LocatedMesh<?> mesh) {
        double inset = 0.01;
        double width = 600, height = 600;
        File debugDir = new File("debug");
        if (!debugDir.isDirectory()) return;
        try {
            File outFile = new File(debugDir, name + ".svg");
            logger.debug("Writing triangles to " + outFile.getPath());
            logger.debug("Creating mesh");
            Mesh2D m2d = new Mesh2D(mesh);
            logger.debug("Mesh created, getting boundary");
            // Rectangle2D rect = m2d.getBoundary().getBounds2D();
            Rectangle2D rect = new Rectangle2D.Double();
            for (Triangle2D t: m2d)
                for (int i = 0; i < 3; ++i)
                    rect.add(t.getCorner(i));
            logger.debug("Got boundary rect");
            if (inset > 0) {
                rect.add(rect.getMinX() - inset, rect.getMinY() - inset);
                rect.add(rect.getMaxX() + inset, rect.getMaxY() + inset);
            }
            double factor = Math.min(width/rect.getWidth(),
                                     height/rect.getHeight());
            width = factor*rect.getWidth();
            height = factor*rect.getHeight();
            logger.debug("Writing mesh");
            FileOutputStream outStream = new FileOutputStream(outFile);
            SvgWriter svg = new SvgWriter(outStream);
            svg.head(rect, width, height);
            //svg.writeTriangles(mesh);
            svg.writeMesh(m2d);
            svg.tail();
            logger.debug("Mesh written");
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

    public static void dumpTriangles(String name, LocatedMesh<Vertex> mesh,
                                     HypTrafo[] trafos) {
        double inset = 0.01;
        double width = 600, height = 600;
        File debugDir = new File("debug");
        if (!debugDir.isDirectory()) return;
        try {
            File outFile = new File(debugDir, name + ".svg");
            Rectangle2D rect = new Rectangle2D.Double();
            logger.debug("Writing triangles to " + outFile.getPath());
            Iterator<? extends CorneredTriangle<? extends Vertex>> ti;
            ti = mesh.iterator();
            Point2D p2d = new Point2D.Double();
            while (ti.hasNext()) {
                CorneredTriangle<? extends Vertex> t = ti.next();
                for (int i = 0; i < 3; ++i) {
                    Vertex v = t.getCorner(i);
                    p2d.setLocation(mesh.getX(v), mesh.getY(v));
                    rect.add(p2d);
                }
            }
            logger.debug("Got boundary rect");
            if (inset > 0) {
                rect.add(rect.getMinX() - inset, rect.getMinY() - inset);
                rect.add(rect.getMaxX() + inset, rect.getMaxY() + inset);
            }
            double factor = Math.min(width/rect.getWidth(),
                                     height/rect.getHeight());
            width = factor*rect.getWidth();
            height = factor*rect.getHeight();
            logger.debug("Writing meshes");
            FileOutputStream outStream = new FileOutputStream(outFile);
            SvgWriter svg = new SvgWriter(outStream);
            svg.head(rect, width, height);
            svg.writeTriangles(mesh);
            for (HypTrafo t: trafos) {
                svg.newLayer();
                svg.writeTriangles(new TransformedMesh(mesh, t));
            }
            svg.tail();
            logger.debug("Mesh written");
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

    private static class TransformedMesh implements LocatedMesh<Vertex> {

        LocatedMesh<Vertex> parent;

        HypTrafo trafo;

        Vertex vertex;

        Vec2C vec = new Vec2C();

        TransformedMesh(LocatedMesh<Vertex> parent, HypTrafo trafo) {
            this.parent = parent;
            this.trafo = trafo;
        }

        public Iterator<? extends CorneredTriangle<? extends Vertex>>
        iterator() {
            return parent.iterator();
        }

        private void setVertex(Vertex v) {
            if (this.vertex == v) return;
            vertex = v;
            vec.assign(parent.getX(v), parent.getY(v), 1, 0);
            trafo.transform(vec, vec);
            vec.dehomogenize();
        }

        public double getX(Vertex v) {
            setVertex(v);
            return vec.x.r;
        }

        public double getY(Vertex v) {
            setVertex(v);
            return vec.x.i;
        }

        public double getZ(Vertex v) {
            return 0;
        }

        public double edgeLength(Vertex v1, Vertex v2) {
            return parent.edgeLength(v1, v2);
        }

    }

}
