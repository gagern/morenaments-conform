package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.List;
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

    public abstract List<Point2D> getHypTileCorners();

    public abstract double getEuclideanCornerAngle(int index);

    public static P6m p6m() {
        return new P6m();
    }

    public static P6 p6() {
        return new P6();
    }

    public static P31m p31m() {
        return new P31m();
    }

    public static P3m1 p3m1() {
        return new P3m1();
    }

    public static P3 p3() {
        return new P3();
    }

    public static P4g p4g() {
        return new P4g();
    }

    public static P4m p4m() {
        return new P4m();
    }

    public static P4 p4() {
        return new P4();
    }

}
