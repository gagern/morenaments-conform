package net.von_gagern.martin.morenaments.conformal.groups;

import de.tum.in.gagern.hornamente.HypTrafo;

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

}
