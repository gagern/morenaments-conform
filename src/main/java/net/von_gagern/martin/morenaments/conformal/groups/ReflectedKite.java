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

}
