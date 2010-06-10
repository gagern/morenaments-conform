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
import de.tum.in.gagern.hornamente.HypTrafo;

class ColoredLizards extends ReflectionBasedGroup {

    protected ColoredLizards() {
        super(new int[0]);
    }

    // Generators are point reflections in the centers of the six
    // boundary edges of the fundamental domain.
    private static final String[] GENERATOR_STRINGS = {
        "acababca",
        "abab",
        "bacababcab",
        "bcababcb",
        "bcbacbabcabc",
        "cababc",
    };

    private static final String[] INSIDENESS_STRINGS = {
        "acba",
        "ab",
        "bacba",
        "bcab",
        "cbacba",
        "cab",
    };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] ts = new HypTrafo[INSIDENESS_STRINGS.length];
        for (int i = 0; i < INSIDENESS_STRINGS.length; ++i) {
            String s = INSIDENESS_STRINGS[i];
            int len = s.length() - 1;
            int lastEdge = s.charAt(len) - 'a';
            HypTrafo t = s2t(s.subSequence(0, len));
            t.invert();
            t.preConcatenate(fundamentalTriangle.getEdgeInsideness(lastEdge));
            ts[i] = t;
        }
        return ts;
    }

    @Override protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(3, 3, 4);
    }

    private UnsupportedOperationException noMesh() {
        return new UnsupportedOperationException("Lizards can't be meshed");
    }

    public List<Point2D> getHypTileCorners() {
        throw noMesh();
    }

    public double getEuclideanCornerAngle(int index) {
        throw noMesh();
    }

    public double[] getEucCornerCoordinates() {
        throw noMesh();
    }

}
