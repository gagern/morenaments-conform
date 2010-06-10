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

import java.util.HashMap;
import java.util.Map;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.triangulate.EucOrbifold;
import net.von_gagern.martin.morenaments.conformal.triangulate.Vertex;

class Pmm extends OrbifoldBasedGroup {

    private static final int[] EUCLIDEAN_ANGLES = { 2, 2, 2, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        .5, 0,
        .5, .5,
        0, .5
    };

    public Pmm() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

    protected HypTrafo[] constructGenerators() {
        HypTrafo[] gens = new HypTrafo[4];
        for (int i = 0; i < 4; ++i)
            gens[i] = reflection(i);
        return gens;
    }

    public double getEuclideanCornerAngle(int index) {
        return Math.PI/2;
    }

    protected EucOrbifold createEucOrbifold() {
        return new Orbifold();
    }

    protected Vec2C[] constructCorners(Vec2C[] specialPoints) {
        return specialPoints;
    }

    protected Map<Vertex, Double>
        getHypAngles(Vertex[] specialPoints, int[] hyperbolicAngles) {
        Map<Vertex, Double> angles = new HashMap<Vertex, Double>(6);
        for (int i = 0; i < 4; ++i)
            angles.put(specialPoints[i], Math.PI/hyperbolicAngles[i]);
        return angles;
    }

    private static class Orbifold extends EucOrbifold {

        public Orbifold() {
            super(1, 1);
            specialPoints = new Vertex[] {
                vs[0][0],
                vs[UNIT][0],
                vs[UNIT][UNIT],
                vs[0][UNIT]
            };
            setCenterCoordinates(HALF, HALF);
        }

    }

}
