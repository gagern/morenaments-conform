package net.von_gagern.martin.morenaments.conformal.groups;

class P3m1 extends ReflectedTriangle {

    private static final int[] EUCLIDEAN_ANGLES = { 3, 3, 3 };

    private static final double[] EUCLIDEAN_CORNERS = {
        1, 0,
        2/3., 2/3.,
        1/3., 1/3.
    };

    public P3m1() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
