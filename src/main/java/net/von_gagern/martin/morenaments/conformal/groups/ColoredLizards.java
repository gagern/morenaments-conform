package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.Point2D;
import java.util.List;
import de.tum.in.gagern.hornamente.HypTrafo;

class ColoredLizards extends ReflectionBasedGroup {

    protected ColoredLizards() {
        super(new int[0]);
    }

    // Generators are point reflections in the centers of the six
    // boundary edges of the fundamental domain.
    private static final String[] GENERATOR_STRINGS = {
        "acababca",
        "abab",
        "bacababcab",
        "bcababcb",
        "bcbacbabcabc",
        "cababc",
    };

    private static final String[] INSIDENESS_STRINGS = {
        "acba",
        "ab",
        "bacba",
        "bcab",
        "cbacba",
        "cab",
    };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    protected HypTrafo[] constructInsidenessChecks() {
        ensureFundamentalTriangle();
        HypTrafo[] ts = new HypTrafo[INSIDENESS_STRINGS.length];
        for (int i = 0; i < INSIDENESS_STRINGS.length; ++i) {
            String s = INSIDENESS_STRINGS[i];
            int len = s.length() - 1;
            int lastEdge = s.charAt(len) - 'a';
            HypTrafo t = s2t(s.subSequence(0, len));
            t.invert();
            t.preConcatenate(fundamentalTriangle.getEdgeInsideness(lastEdge));
            ts[i] = t;
        }
        return ts;
    }

    @Override protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(3, 3, 4);
    }

    private UnsupportedOperationException noMesh() {
        return new UnsupportedOperationException("Lizards can't be meshed");
    }

    public List<Point2D> getHypTileCorners() {
        throw noMesh();
    }

    public double getEuclideanCornerAngle(int index) {
        throw noMesh();
    }

    public double[] getEucCornerCoordinates() {
        throw noMesh();
    }

}
