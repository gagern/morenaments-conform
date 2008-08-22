package net.von_gagern.martin.morenaments.conformal.groups;

class P3 extends RotatedKite {

    private static final int[] EUCLIDEAN_ANGLES = { 3, 3, 3 };

    private static final double[] EUCLIDEAN_CORNERS = {
        1/3., 1/3.,
        1, 0,
        2/3., 2/3.,
        0, 1
    };

    public P3() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
