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

class P6m extends ReflectedTriangle {

    private static final int[] EUCLIDEAN_ANGLES = { 6, 3, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0., 0.,
        1/3., 1/3.,
        0, 0.5
    };

    public P6m() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
