package net.von_gagern.martin.morenaments.conformal.groups;

class P31m extends ReflectedKite {

    private static final int[] EUCLIDEAN_ANGLES = { 3, 3 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, 0,
        .5, 0,
        1/3., 1/3.,
        0, .5
    };

    public P31m() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
