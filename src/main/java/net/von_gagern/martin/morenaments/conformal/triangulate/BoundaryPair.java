package net.von_gagern.martin.morenaments.conformal.triangulate;

class BoundaryPair {

    private Boundary b1, b2;

    public BoundaryPair(Boundary b1, Boundary b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    public int hashCode() {
        return b1.hashCode() ^ b2.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof BoundaryPair)) return false;
        BoundaryPair that = (BoundaryPair)o;
        return (this.b1.equals(that.b1) && this.b2.equals(that.b2)) ||
               (this.b1.equals(that.b2) && this.b2.equals(that.b1));
    }

}
