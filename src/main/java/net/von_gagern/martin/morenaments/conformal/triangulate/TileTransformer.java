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
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class TileTransformer implements Runnable {

    private Group g;

    private Triangulation hypMesh;

    private ResultMesh eucMesh;

    public TileTransformer(Group group) {
        this.g = group;
    }

    public void transform() throws MeshException {
        // Approximate hyperbolic tile by a triangulated mesh
        List<Point2D> corners = g.getHypTileCorners();
        hypMesh = new Triangulation();
        hypMesh.triangulatePoincare(corners);

        // Transform mesh to euclidean shape using discrete conformal map
        Conformal c = Conformal.getInstance(hypMesh);
        Map<Point2D, Double> angles =
            new HashMap<Point2D, Double>(corners.size());
        for (int i = 0; i < corners.size(); ++i) {
            Double angle = g.getEuclideanCornerAngle(i);
            angles.put(corners.get(i), angle);
        }
        eucMesh = c.transform();
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
