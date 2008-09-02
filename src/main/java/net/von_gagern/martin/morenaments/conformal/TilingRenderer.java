package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class TilingRenderer {

    private final Group g;

    private final HypTrafo[] insidenessChecks;

    private final HypTrafo[] generators;

    private PixelLookupSource source;

    private int r;

    private BufferedImage target;

    public TilingRenderer(Group group) {
        this.g = group;
        insidenessChecks = group.getInsidenessChecks();
        generators = group.getGenerators();
    }

    public BufferedImage render(PixelLookupSource source,
                                int size, BufferedImage target) {
        this.source = source;
        this.r = size / 2;
        size = r*2;
        if (target == null ||
            target.getWidth() != size || target.getHeight() != size) {
            target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }
        this.target = target;
        HypTrafo[][] t = new HypTrafo[8][r];
        HypTrafo id = HypTrafo.getIdentity();
        for (int i = 0; i < 8; ++i)
            t[i][0] = id;
        violationStatistics = new int[4*r];
        for (int i = 0; i < r; ++i) {
            // We render eight octants by eight calls           major, minor
            renderMinor(t[0],  i  ,    0,  0,  1, i  ); // +x, +y
            renderMinor(t[1],    0,  i  ,  1,  0, i+1); // +y, +x
            renderMinor(t[2],   -1,  i  , -1,  0, i  ); // +y, -x
            renderMinor(t[3], -i-1,    0,  0,  1, i+1); // -x, +y
            renderMinor(t[4], -i-1,   -1,  0, -1, i  ); // -x, -y
            renderMinor(t[5],   -1, -i-1, -1,  0, i+1); // -y, -x
            renderMinor(t[6],    0, -i-1,  1,  0, i  ); // -y, +x
            renderMinor(t[7],  i  ,   -1,  0, -1, i+1); // +x, -y
        }
        if (true) {
            System.out.println("Violation statistics:");
            int maxViol;
            for (maxViol = violationStatistics.length - 1;
                 violationStatistics[maxViol] == 0; --maxViol);
            for (int i = 0; i <= maxViol; ++i)
                System.out.printf("%3d:%7d%n", i + 1, violationStatistics[i]);
        }
        return target;
    }

    private void renderMinor(HypTrafo[] ts,
                             int x, int y, int dx, int dy, int n) {
        Vec2C v = new Vec2C();
        HypTrafo t = null;
        for (int i = 0; i < n; ++i) {
            double px = (x + 0.5)/r, py = (y + 0.5)/r;
            if (px*px + py*py > 1) return;
            v.assign(px, py, 1, 0);
            try {
                ts[i] = t = findTrafo(v, ts[i], t, false);
            }
            catch (InfiniteLoopException e) {
                ts[i] = t = findTrafo(v, ts[i], t, true);
            }
            target.setRGB(x + r, y + r, getColor(t, v));
            x += dx;
            y += dy;
        }
    }

    private int[] violationStatistics;

    public HypTrafo findTrafo(Vec2C v, int maxIterations)
        throws InfiniteLoopException
    {
        violationStatistics = new int[maxIterations];
        return findTrafo(v, HypTrafo.getIdentity(), null, false);
    }

    private HypTrafo findTrafo(Vec2C v, HypTrafo t, HypTrafo t2,
                               boolean debug) throws InfiniteLoopException {
        Vec2C v2 = new Vec2C();
        if (t == null) t = t2;
        t.inverseTransform(v, v2);
        int violated = insidenessCheck(v2, debug);
        if (violated == -1) return t;
        if (t2 != null && t2 != t) {
            t = t2;
            t.inverseTransform(v, v2);
            violated = insidenessCheck(v2, debug);
            if (violated == -1) return t;
        }
        t = t.clone();
        for (int i = 0; i < violationStatistics.length; ++i) {
            if (debug)
                System.out.println("t = " + t + " * " + generators[violated]);
            t.concatenate(generators[violated]);
            t.inverseTransform(v, v2);
            violated = insidenessCheck(v2, debug);
            if (violated == -1) {
                ++ violationStatistics[i];
                return t;
            }
        }
        // Should have gotten inside by now. Prevent a loop.
        throw new InfiniteLoopException("Could not get inside the main tile.");
    }

    private int insidenessCheck(Vec2C v, boolean debug) {
        Vec2C v2 = new Vec2C();
        int mostViolated = -1;
        double maxViolation = 0;
        for (int i = 0; i < insidenessChecks.length; ++i) {
            insidenessChecks[i].transform(v, v2);
            v2.normalize();
            double violation = v2.x.r*v2.y.i - v2.x.i*v2.y.r;
            if (violation < maxViolation) continue;
            maxViolation = violation;
            mostViolated = i;
        }
        if (debug) {
            if (mostViolated != -1)
                System.out.println(mostViolated + " violated by " +
                                   maxViolation);
            else
                System.out.println("none violated");
        }
        return mostViolated;
    }

    java.util.Map<HypTrafo, Integer> colorMap =
	new java.util.HashMap<HypTrafo, Integer>();

    java.util.Random rnd = new java.util.Random(1271476327);

    protected int getColor(HypTrafo t, Vec2C v) {
        if (source == null) { // debug use
            Integer color = colorMap.get(t);
            if (color == null) {
                color = 0xff000000 | rnd.nextInt(1 << 24);
                colorMap.put(t, color);
            }
            return color;
        }
        t.inverseTransform(v, v);
        Point2D p = v.dehomogenize(new Point2D.Double());
        return source.getRGB(p);
    }

}
