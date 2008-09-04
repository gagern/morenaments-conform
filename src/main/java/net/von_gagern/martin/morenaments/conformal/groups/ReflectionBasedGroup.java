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
        for (int i = 0; i < gs.length; ++i)
            ts[i] = s2t(gs[i]);
        return ts;
    }

    protected HypTrafo s2t(CharSequence s) {
        HypTrafo t = new HypTrafo();
        for (int i = 0; i < s.length(); ++i)
            t.concatenate(fundamentalTriangle.getReflection(s.charAt(i) - 'a'));
        return t.normalize();
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
