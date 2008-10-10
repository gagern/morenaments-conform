package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import de.tum.in.gagern.hornamente.HypTrafo;

public abstract class Group implements Cloneable {

    protected final int[] euclideanAngles;

    protected int[] hyperbolicAngles;

    private HypTrafo[] insidenessChecks;

    private HypTrafo[] generators;

    private AffineTransform euclideanTransform;

    protected Group(int[] euclideanAngles) {
	this.euclideanAngles = euclideanAngles;
    }

    public int[] getEuclideanAngles() {
	return euclideanAngles;
    }

    public void setEuclideanTransform(AffineTransform tr) {
        euclideanTransform = tr;
    }

    public AffineTransform getEuclideanTransform() {
        return euclideanTransform;
    }

    public boolean checkHyperbolicAngles(int[] hyperbolicAngles) {
	return true;
    }

    public void setHyperbolicAngles(int[] hyperbolicAngles) {
	if (!checkHyperbolicAngles(hyperbolicAngles))
	    throw new IllegalArgumentException("Invalid hyperbolic angles");
	this.hyperbolicAngles = hyperbolicAngles;
        insidenessChecks = null;
        generators = null;
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

    public List<Point2D> getEucTileCorners() {
        double[] coords = getEucCornerCoordinates();
        assert coords.length % 2 == 0;
        List<Point2D> corners = new ArrayList<Point2D>(coords.length / 2);
        for (int i = 0; i < coords.length; i += 2)
            corners.add(new Point2D.Double(coords[i], coords[i + 1]));
        return corners;
    }

    protected abstract double[] getEucCornerCoordinates();

    public abstract double getEuclideanCornerAngle(int index);

    @Override public Group clone() {
        try {
            Group that = (Group)super.clone();
            if (hyperbolicAngles != null)
                that.setHyperbolicAngles((int[])hyperbolicAngles.clone());
            return that;
        }
        catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

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

    public static Pmm pmm() {
        return new Pmm();
    }

    public enum EuclideanGroup {
        p6m, p6, p31m, p3m1, p3, p4g, p4m, p4,
        coloredLizards,
    }

    public static Group getInstance(EuclideanGroup g) {
        switch (g) {
        case p6m: return new P6m();
        case p6: return new P6();
        case p31m: return new P31m();
        case p3: return new P3();
        case p4g: return new P4g();
        case p4m: return new P4m();
        case p4: return new P4();
        case coloredLizards: return new ColoredLizards();
        default: throw new IllegalArgumentException("Invalid group: " + g);
        }
    }

}
