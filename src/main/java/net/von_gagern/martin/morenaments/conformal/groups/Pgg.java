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

class Pgg extends OrbifoldBasedGroup {

    private static final int[] EUCLIDEAN_ANGLES = { 2, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, .5,
        0, 0,
        .5, 0,
        1, 0,
        1, .5,
        .5, .5,
    };

    public Pgg() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

    protected HypTrafo[] constructGenerators() {
        HypTrafo[] gens = new HypTrafo[6];
        gens[1] = rotation(2, hyperbolicAngles[1]);
        gens[2] = gens[1].getInverse();
        gens[4] = rotation(5, hyperbolicAngles[0]);
        gens[5] = gens[4].getInverse();
        gens[0] = rotation(1, hyperbolicAngles[0]).concatenate(gens[1]);
        gens[3] = gens[0].getInverse();
        return gens;
    }

    public double getEuclideanCornerAngle(int index) {
        switch (index) {
        case 1:
        case 4:
            return eucBaseAngle;
        case 3:
        case 0:
            return Math.PI - eucBaseAngle;
        case 2:
        case 5:
            return Math.PI;
        default:
            throw new IllegalArgumentException();
        }
    }

    protected EucOrbifold createEucOrbifold() {
        return new Orbifold();
    }

    protected Vec2C[] constructCorners(Vec2C[] specialPoints) {
        Vec2C[] corners = new Vec2C[6];
        corners[1] = specialPoints[0];
        corners[2] = specialPoints[1];
        corners[5] = specialPoints[2];
        corners[0] = specialPoints[3];
        corners[3] = rotate(corners[1], corners[2], -hyperbolicAngles[1]);
        corners[4] = rotate(corners[0], corners[5], hyperbolicAngles[0]);
        return corners;
    }

    protected Map<Vertex, Double>
        getHypAngles(Vertex[] specialPoints, int[] hyperbolicAngles) {
        Map<Vertex, Double> angles = new HashMap<Vertex, Double>(6);
        for (int i = 0; i < 4; ++i)
            angles.put(specialPoints[i], Math.PI*2/hyperbolicAngles[i&1]);
        return angles;
    }

    private static class Orbifold extends EucOrbifold {

        public Orbifold() {
            super(2, 1);
            for (int i = 0; i <= UNIT; ++i) {
                vs[DOUBLE][i] = vs[0][i]; // glue together outer boundaries
                vs[DOUBLE - i][0] = vs[i][0]; // fold and glue lower boundary
                vs[DOUBLE - i][UNIT] = vs[i][UNIT]; // same for upper boundary
            }
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
