package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

class HypLayout {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(HypLayout.class);

    /**
     * The list if all triangles.
     */
    private final List<Triangle> triangles;

    /**
     * The central triangle to layout first.
     */
    private Triangle startTriangle;

    /**
     * Construct layouter for given mesh.
     * @param mesh the mesh to be layed out.
     * @see #layout()
     */
    public HypLayout(InternalMesh mesh) {
        triangles = mesh.getTriangles();
    }

    /**
     * Set the triangle to be layed out first.
     * @param start The first triangle to layout
     */
    public void setStartTriangle(Triangle start) {
        startTriangle = start;
    }

    /**
     * Calculate layout by determining suitable vertex coordinates.
     * This is the main method of this class.
     */
    public void layout() {
        // Sadly ArrayDeque was added only in java 1.6, so we won't use it yet
        Queue<Triangle> q = new LinkedList<Triangle>();
        clearIterFlags();
        if (startTriangle == null) startTriangle = findStart();
        logger.debug("Start triangle: " + startTriangle);
        layoutStart(startTriangle);
        q.add(startTriangle);
        clearIterFlags();
        while (!q.isEmpty()) {
            Triangle t1 = q.remove();
            t1.setIterFlag();
            for (Edge e: t1.getEdges()) {
                Triangle t2 = e.getOtherTriangle(t1);
                if (t2 == null || t2.getIterFlag()) continue;
                q.add(t2);
                t2.setIterFlag();
                layoutEdge(e, t2);
            }
        }
    }

    /**
     * Reset all triangles to unvisited state.
     */
    private void clearIterFlags() {
        for (Triangle t: triangles)
            t.clearIterFlag();
    }

    /**
     * Find the triangle to start with.
     * To keep distances from the starting point and therefore errors
     * low, the starting triangle should be far inside near the
     * combinatoric center of the mesh. This is achieved through a BFS
     * starting at all boundary triangles.
     */
    private Triangle findStart() {
        // Sadly ArrayDeque was added only in java 1.6, so we won't use it yet
        Queue<Triangle> q = new LinkedList<Triangle>();
        int unqueued = 0;
        for (Triangle t: triangles) {
            if (t.isBoundary()) {
                q.add(t);
            }
            else {
                ++unqueued;
            }
        }
        if (q.isEmpty() || unqueued == 0) {
            return triangles.get(triangles.size()/2);
        }
        for (;;) {
            Triangle t1 = q.remove();
            t1.setIterFlag();
            for (Edge e: t1.getEdges()) {
                Triangle t2 = e.getOtherTriangle(t1);
                if (t2 == null || t2.getIterFlag()) continue;
                q.add(t2);
                t2.setIterFlag();
                --unqueued;
                if (unqueued == 0)
                    return t2;
            }
        }
    }

    /**
     * Adjust edge angle for orientation.
     * If the edges have different orientation with respect to the
     * vertex at their intersection, i.e. one points toward that
     * vertex and the other away from it, the angle will be rotated by
     * &#960;. The method will also normalize the angle to the range
     * (-&#960;, &#960;].
     *
     * @param angle the angle the edge of interest would have if both
     *              edges were pointing away from <code>v</code>
     * @param e1 the first edge involved in angle calculation
     * @param e2 the second edge involved in angle calculation
     * @param v the vertex incident to both edges
     * @return the adjusted and normalized angle
     */
    private double edgeAngle(double angle, Edge e1, Edge e2, Vertex v) {
        assert e1.v1 == v || e1.v2 == v: "v must be endpoint of e1";
        assert e2.v1 == v || e2.v2 == v: "v must be endpoint of e2";
        if ((e1.v1 == v) != (e2.v1 == v)) {
            if (angle > 0) angle -= Math.PI;
            else angle += Math.PI;
        }
        while (angle > Math.PI) angle -= 2*Math.PI;
        while (angle <= -Math.PI) angle += 2*Math.PI;
        return angle;
    }

    /**
     * Runnable interface to the <code>layout</code> method.<p>
     *
     * Any application not using multiple threads should rather call
     * <code>layout</code> directly.
     *
     * @see #layout()
     */
    public void run() {
        layout();
    }

    /**
     * Layout the initial triangle.
     * @param t the first triangle to be layed out
     */
    protected void layoutStart(Triangle t) {
        Angle a = t.getAngles().get(0);
        Vertex v1 = a.vertex, v2 = a.nextVertex, v3 = a.prevVertex;
        Edge e12 = a.nextEdge;
        double l12 = e12.length;

        // set locations and edge positions
        HypEdgePos pos1 = new HypEdgePos();
        pos1.setVertex(v1);
        v1.offerLocation(0, 0);
        e12.offerHypPos(pos1);
        Point2D p2 = pos1.derive(v2, l12).dehomogenize();
        v2.offerLocation(p2.getX(), p2.getY());
        layoutEdge(e12, t);
    }

    /**
     * Lay out third vertex in triangle.<p>
     *
     * This method is called when triangle was entered in order to fix
     * the location of the vertex opposite the entering angle.
     *
     * @param e the edge by which the triangle was entered
     * @param t the triangle just entered
     */
    protected void layoutEdge(Edge e, Triangle t) {
        /* Imagine t is an oriented triangle ABC. We entered the triangle
         * through the unoriented edge [AB], so e is either [AB] or [BA].
         * We want to find the coordinates for C, based on those of A.
         * Thus we determine the Angle BAC and the edge [AC].
         * Each edge has an associated orientation, but we have to take
         * care of the orientation of these edges. If both edges point
         * towards A or both away from A, we can simply add angles.
         * Otherwise we have to add PI in order to invert orientation.
         */

        Vertex c = t.getOppositeVertex(e);
        Angle bac = t.getNextAngle(c), cba = t.getPrevAngle(c);
        Vertex a = bac.vertex, b = cba.vertex;
        Edge ca = bac.prevEdge, bc = cba.nextEdge;
        double alpha = bac.angle, beta = cba.angle;

        HypEdgePos abPos = e.getHypPos();
        HypEdgePos caPos = abPos.derive(a, e.length, alpha);
        HypEdgePos bcPos = abPos.derive(b, e.length, -beta);
        logger.trace("ab = " + abPos);
        logger.trace("ca = " + caPos);
        logger.trace("bc = " + bcPos);
        caPos = ca.offerHypPos(caPos);
        bcPos = bc.offerHypPos(bcPos);
        logger.trace("ca = " + caPos);
        logger.trace("bc = " + bcPos);

        Point2D caC = caPos.derive(c, ca.length).dehomogenize();
        Point2D bcC = bcPos.derive(c, bc.length).dehomogenize();
        double x = (caC.getX() + bcC.getX())/2;
        double y = (caC.getY() + bcC.getY())/2;
        assert !Double.isNaN(x): "x must not be NaN";
        assert !Double.isNaN(y): "y must not be NaN";
        assert !Double.isInfinite(x): "x must be finite";
        assert !Double.isInfinite(y): "y must be finite";
        c.offerLocation(x, y);
        logger.trace("layoutEdge(" + e + ", " + t + ") set " + c +
                     " to (" + x + ", " + y + ")");
    }

}
