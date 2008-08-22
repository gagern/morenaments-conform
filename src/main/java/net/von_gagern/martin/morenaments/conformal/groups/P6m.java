package net.von_gagern.martin.morenaments.conformal.groups;

class P6m extends ReflectedTriangle {

    private static final int[] EUCLIDEAN_ANGLES = { 6, 3, 2 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0., 0.,
        1/3., 1/3.,
        0, 0.5
    };

    public P6m() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
