package net.von_gagern.martin.morenaments.conformal.groups;

abstract class RotatedKite extends Kite {

    protected RotatedKite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "bc", "cb", "ac", "ca" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

}
