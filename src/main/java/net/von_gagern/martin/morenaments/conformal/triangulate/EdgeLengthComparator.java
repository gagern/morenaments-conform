package net.von_gagern.martin.morenaments.conformal.triangulate;

class EdgeLengthComparator extends ReverseDoubleComparator<Edge> {

    protected double getValue(Edge e) {
        return e.lengthSq();
    }

    private static EdgeLengthComparator instance;

    public static synchronized EdgeLengthComparator getInstance() {
        if (instance == null)
            instance = new EdgeLengthComparator();
        return instance;
    }

}
