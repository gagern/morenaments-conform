/*
 * morenaments conformal - Hyperbolization of ornaments
 *                         via discrete conformal maps
 * Copyright (C) 2009-2010 Martin von Gagern
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class TilingRenderer {

    private final Logger logger = Logger.getLogger(TilingRenderer.class);

    protected final Group g;

    protected final HypTrafo[] insidenessChecks;

    protected final HypTrafo[] generators;

    protected PixelLookupSource source;

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
        if (logger.isDebugEnabled()) {
            logger.debug("Violation statistics:");
            int maxViol;
            for (maxViol = violationStatistics.length - 1;
                 violationStatistics[maxViol] == 0; --maxViol);
            for (int i = 0; i <= maxViol; ++i)
                logger.debug(String.format("%3d:%7d", i,
                                           violationStatistics[i]));
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

    public HypTrafo findTrafo(Vec2C v, int maxIterations) {
        violationStatistics = new int[maxIterations];
        return findTrafo(v, HypTrafo.getIdentity(), null, false);
    }

    private HypTrafo findTrafo(Vec2C v, HypTrafo t, HypTrafo t2,
                               boolean debug) {
        Vec2C v2 = new Vec2C();
        if (t == null) t = t2;
        t.inverseTransform(v, v2);
        InsidenessCheck ic = new InsidenessCheck();
        double minViolation = Double.POSITIVE_INFINITY;
        HypTrafo minViolatingTransform = null;
        int violated = ic.insidenessCheck(v2, debug);
        if (violated == -1) return t;
        if (t2 != null && t2 != t) {
            t = t2;
            t.inverseTransform(v, v2);
            violated = ic.insidenessCheck(v2, debug);
            if (violated == -1) return t;
        }
        t = t.clone();
        for (int i = 1; i < violationStatistics.length; ++i) {
            if (debug)
                System.out.println("t = " + t + " * " + generators[violated]);
            t.concatenate(generators[violated]);
            t.inverseTransform(v, v2);
            violated = ic.insidenessCheck(v2, debug);
            if (violated == -1) {
                ++ violationStatistics[i];
                return t;
            }
            if (ic.violation < minViolation) {
                minViolation = ic.violation;
                minViolatingTransform = t;
            }
        }
        // Should have gotten inside by now. Prevent a loop.
        ++ violationStatistics[0];
        return minViolatingTransform;
    }

    private class InsidenessCheck {

        double violation;

        int insidenessCheck(Vec2C v, boolean debug) {
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
            this.violation = maxViolation;
            return mostViolated;
        }

    }

    protected int getColor(HypTrafo t, Vec2C v) {
        t.inverseTransform(v, v);
        Point2D p = v.dehomogenize(new Point2D.Double());
        return source.getRGB(p);
    }

}
