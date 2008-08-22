package net.von_gagern.martin.morenaments.conformal.groups;

class P4g extends ReflectedKite {

    private static final int[] EUCLIDEAN_ANGLES = { 2, 4 };

    private static final double[] EUCLIDEAN_CORNERS = {
        0, .5,
        .5, 0,
        .5, .5
    };

    public P4g() {
        super(EUCLIDEAN_ANGLES);
    }

    protected double[] getEucCornerCoordinates() {
        return EUCLIDEAN_CORNERS;
    }

}
