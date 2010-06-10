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

abstract class Kite extends ReflectionBasedGroup {

    protected Kite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] res = new HypTrafo[4];
        HypTrafo c = fundamentalTriangle.getReflection(2);
        res[0] = fundamentalTriangle.getEdgeInsideness(0);
        res[1] = fundamentalTriangle.getEdgeInsideness(1);
        res[2] = res[0].clone().concatenate(c);
        res[3] = res[1].clone().concatenate(c);
        return res;
    }

    public List<Point2D> getHypTileCorners() {
        ensureFundamentalTriangle();
        List<Point2D> corners = new ArrayList<Point2D>(4);
        for (int i = 0; i < 3; ++i) {
            Vec2C v = fundamentalTriangle.getCorner(i);
            corners.add(v.dehomogenize(new Point2D.Double()));
        }
        Vec2C v = fundamentalTriangle.getCorner(2).clone();
        fundamentalTriangle.getReflection(2).transform(v, v);
        corners.add(1, v.dehomogenize(new Point2D.Double()));
        assert corners.size() == 4;
        return corners;
    }

}
