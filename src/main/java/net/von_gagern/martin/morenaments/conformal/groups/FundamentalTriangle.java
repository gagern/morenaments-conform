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

import de.tum.in.gagern.hornamente.Angle;
import de.tum.in.gagern.hornamente.Distance;
import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Trigonometry;
import de.tum.in.gagern.hornamente.Vec2C;

public class FundamentalTriangle {

    private final HypTrafo[] trafos;

    public FundamentalTriangle(int... angleCounts) {
        /* We have the following triangle:
         * Corner A at the origin
         * Corner B somewhere along the positive real axis
         * Corner C with positive imaginary part
         * Edge a connecting corners B and C
         * Edge b connecting corners C and A
         * Edge c connecting corners A and B
         * Angle alpha at corner A
         * Angle beta at corner B
         * Angle gamma at corner C
         */

        // angles and corresponding rotations
        if (angleCounts.length != 3)
            throw new IllegalArgumentException("need three angles");
        double radAlpha = Math.PI/angleCounts[0];
        double radBeta = Math.PI/angleCounts[1];
        double radGamma = Math.PI/angleCounts[2];
        Angle alpha = Angle.forRadians(radAlpha);
        Angle beta = Angle.forRadians(radBeta);
        Angle gamma = Angle.forRadians(radGamma);
        HypTrafo rotAlpha = HypTrafo.getRotation(alpha);
        HypTrafo rotBeta = HypTrafo.getRotation(beta);
        HypTrafo rotTwoAlpha = HypTrafo.getRotation(2*radAlpha);
        HypTrafo rotTwoBeta = HypTrafo.getRotation(2*radBeta);

        // edge lengths and corresponding translations
        Distance lb = Trigonometry.aaa(beta, gamma, alpha);
        Distance lc = Trigonometry.aaa(gamma, alpha, beta);
        HypTrafo tb = HypTrafo.getTranslation(lb);
        HypTrafo tc = HypTrafo.getTranslation(lc);
        HypTrafo tci = tc.getInverse();

        // Other common transformations
        HypTrafo conj = HypTrafo.getConjugation();

        // reflections along the triangle sides
        HypTrafo rc = HypTrafo.product(conj);
        HypTrafo rb = HypTrafo.product(rotTwoAlpha, conj);
        HypTrafo ra = HypTrafo.product(tc, conj, rotTwoBeta, tci);

        // triangle corners
        HypTrafo pa = HypTrafo.getIdentity();
        HypTrafo pb = tc;
        HypTrafo pc = HypTrafo.product(rotAlpha, tb);

        // triangle edges for insideness checks
        // positive imaginary part of transformed point means "inside"
        HypTrafo ec = HypTrafo.getIdentity();
        HypTrafo eb = HypTrafo.product(rotAlpha, conj);
        HypTrafo ea = HypTrafo.product(conj, rotBeta, tci);

        this.trafos = new HypTrafo[] { ra, rb, rc, pa, pb, pc, ea, eb, ec };
    }

    public HypTrafo getReflection(int i) {
        return trafos[i%3];
    }

    public Vec2C getCorner(int i) {
        return trafos[3 + (i%3)].vec;
    }

    public HypTrafo getEdgeInsideness(int i) {
        return trafos[6 + (i%3)];
    }

}
