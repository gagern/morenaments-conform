package net.von_gagern.martin.morenaments.conformal.groups;

import de.tum.in.gagern.hornamente.HypTrafo;

abstract class ReflectionBasedGroup extends Group {

    protected FundamentalTriangle fundamentalTriangle;

    protected ReflectionBasedGroup(int... euclideanAngles) {
        super(euclideanAngles);
    }

    public abstract String[] getGeneratorStrings();

    protected HypTrafo[] constructGenerators() {
        ensureFundamentalTriangle();
        String[] gs = getGeneratorStrings();
        HypTrafo[] ts = new HypTrafo[gs.length];
        HypTrafo[] rs = new HypTrafo[3];
        for (int i = 0; i < 3; ++i)
            rs[i] = fundamentalTriangle.getReflection(i);
        for (int i = 0; i < gs.length; ++i) {
            String g = gs[i];
            HypTrafo t = new HypTrafo();
            for (int j = 0; j < g.length(); ++j)
                t.concatenate(rs[g.charAt(j) - 'a']);
            t.normalize();
            ts[i] = t;
            System.out.println(g + " -> " + t);
        }
        return ts;
    }

    @Override public void setHyperbolicAngles(int[] hyperbolicAngles) {
        fundamentalTriangle = null;
        super.setHyperbolicAngles(hyperbolicAngles);
    }

    protected void ensureFundamentalTriangle() {
        if (fundamentalTriangle != null) return;
        fundamentalTriangle = constructTriangle();
    }

    protected FundamentalTriangle constructTriangle() {
        return new FundamentalTriangle(hyperbolicAngles);
    }

}
