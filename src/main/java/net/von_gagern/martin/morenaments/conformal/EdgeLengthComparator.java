package net.von_gagern.martin.morenaments.conformal;

class EdgeLengthComparator extends ReverseDoubleComparator<Edge> {

    protected double getValue(Edge e) {
        return e.lengthSq();
    }

}
