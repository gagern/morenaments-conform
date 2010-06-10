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

import de.tum.in.gagern.hornamente.HypTrafo;

abstract class ReflectionBasedGroup extends Group {

    protected FundamentalTriangle fundamentalTriangle;

    protected ReflectionBasedGroup(int... euclideanAngles) {
        super(euclideanAngles);
    }

    public abstract String[] getGeneratorStrings();

    protected HypTrafo[] constructGenerators() {
        ensureFundamentalTriangle();
        String[] gs = getGeneratorStrings();
        HypTrafo[] ts = new HypTrafo[gs.length];
        for (int i = 0; i < gs.length; ++i)
            ts[i] = s2t(gs[i]);
        return ts;
    }

    protected HypTrafo s2t(CharSequence s) {
        HypTrafo t = new HypTrafo();
        for (int i = 0; i < s.length(); ++i)
            t.concatenate(fundamentalTriangle.getReflection(s.charAt(i) - 'a'));
        return t.normalize();
    }

    @Override public void setHyperbolicAngles(int[] hyperbolicAngles) {
        fundamentalTriangle = null;
        super.setHyperbolicAngles(hyperbolicAngles);
    }

    protected void ensureFundamentalTriangle() {
        if (fundamentalTriangle != null) return;
        fundamentalTriangle = constructTriangle();
    }

    protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(hyperbolicAngles);
    }

}
