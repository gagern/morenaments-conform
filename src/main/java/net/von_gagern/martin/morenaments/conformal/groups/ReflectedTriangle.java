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

package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;

abstract class ReflectedTriangle extends ReflectionBasedGroup {

    protected ReflectedTriangle(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "a", "b", "c" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] res = new HypTrafo[3];
        for (int i = 0; i < 3; ++i)
            res[i] = fundamentalTriangle.getEdgeInsideness(i);
        return res;
    }

    public List<Point2D> getHypTileCorners() {
        ensureFundamentalTriangle();
        List<Point2D> corners = new ArrayList<Point2D>(3);
        for (int i = 0; i < 3; ++i) {
            Vec2C v = fundamentalTriangle.getCorner(i);
            corners.add(v.dehomogenize(new Point2D.Double()));
        }
        return corners;
    }

    public double getEuclideanCornerAngle(int index) {
        return Math.PI/euclideanAngles[index];
    }

}
