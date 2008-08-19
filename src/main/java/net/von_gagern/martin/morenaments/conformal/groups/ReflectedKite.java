package net.von_gagern.martin.morenaments.conformal.groups;

abstract class ReflectedKite extends Kite {

    protected ReflectedKite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "b", "cbc", "ac", "ca" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

}
