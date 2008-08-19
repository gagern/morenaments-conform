package net.von_gagern.martin.morenaments.conformal.groups;

import de.tum.in.gagern.hornamente.HypTrafo;

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

}
