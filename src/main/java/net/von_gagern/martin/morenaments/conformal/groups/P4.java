package net.von_gagern.martin.morenaments.conformal.groups;

class P4 extends RotatedKite {

    private static final int[] EUCLIDEAN_ANGLES = { 4, 4, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        .5, 0,
        .5, .5,
        0, .5
    };

    public P4() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
