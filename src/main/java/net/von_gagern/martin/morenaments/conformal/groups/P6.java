package net.von_gagern.martin.morenaments.conformal.groups;

class P6 extends RotatedKite {

    private static final int[] EUCLIDEAN_ANGLES = { 6, 3, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        .5, 0,
        1/3., 1/3.,
        0, .5
    };

    public P6() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
