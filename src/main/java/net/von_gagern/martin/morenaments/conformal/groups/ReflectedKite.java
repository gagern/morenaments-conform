package net.von_gagern.martin.morenaments.conformal.groups;

abstract class ReflectedKite extends Kite {

    protected ReflectedKite(int[] euclideanAngles) {
        super(euclideanAngles);
    }

    private static final String[] GENERATOR_STRINGS =
        { "ac", "b", "ca", "cbc" };

    public String[] getGeneratorStrings() {
        return GENERATOR_STRINGS;
    }

    @Override protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(hyperbolicAngles[0]*2,
                                       hyperbolicAngles[1], 2);
    }

    public double getEuclideanCornerAngle(int index) {
        switch(index) {
        case 0: // corner A of the triangle
            return Math.PI/euclideanAngles[0];
        case 2: // corner B of the triangle
            return 2.*Math.PI/euclideanAngles[1];
        case 3: // corner C of the triangle
        case 1: // corner C of the reflected triangle
            return Math.PI/2;
        default:
            throw new IndexOutOfBoundsException();
        }
    }

}
