package net.von_gagern.martin.morenaments.conformal.groups;

import de.tum.in.gagern.hornamente.HypTrafo;

public abstract class Group {

    protected final int[] euclideanAngles;

    protected int[] hyperbolicAngles;

    private HypTrafo[] insidenessChecks;

    private HypTrafo[] generators;

    protected Group(int[] euclideanAngles) {
	this.euclideanAngles = euclideanAngles;
    }

    public int[] getEuclideanAngles() {
	return euclideanAngles;
    }

    public boolean checkHyperbolicAngles(int[] hyperbolicAngles) {
	return true;
    }

    public void setHyperbolidAngles(int[] hyperbolicAngles) {
	if (!checkHyperbolicAngles(hyperbolicAngles))
	    throw new IllegalArgumentException("Invalid hyperbolic angles");
	this.hyperbolicAngles = hyperbolicAngles;
    }

    public HypTrafo[] getGenerators() {
        if (generators == null)
            generators = constructGenerators();
        return generators;
    }

    protected abstract HypTrafo[] constructGenerators();

    public HypTrafo[] getInsidenessChecks() {
        if (insidenessChecks == null)
            insidenessChecks = constructInsidenessChecks();
        return insidenessChecks;
    }

    protected abstract HypTrafo[] constructInsidenessChecks();

    public static P6m p6m() {
        return new P6m();
    }

}
