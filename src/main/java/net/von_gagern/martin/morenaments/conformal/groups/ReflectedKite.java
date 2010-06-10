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

abstract class ReflectedKite extends Kite {

    protected ReflectedKite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "ac", "b", "ca", "cbc" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    @Override protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(hyperbolicAngles[0]*2,
                                       hyperbolicAngles[1], 2);
    }

    public double getEuclideanCornerAngle(int index) {
        switch(index) {
        case 0: // corner A of the triangle
            return Math.PI/euclideanAngles[0];
        case 2: // corner B of the triangle
            return 2.*Math.PI/euclideanAngles[1];
        case 3: // corner C of the triangle
        case 1: // corner C of the reflected triangle
            return Math.PI/2;
        default:
            throw new IndexOutOfBoundsException();
        }
    }

}
