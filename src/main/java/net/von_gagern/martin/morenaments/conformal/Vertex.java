package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;

class Vertex extends Point2D.Double implements Comparable<Vertex> {

    private int index;

    private int type;

    public Vertex(Point2D p) {
        super(p.getX(), p.getY());
        this.index = 0;
        this.type = 0;
    }

    void setIndex(int index) {
        this.index = index;
    }

    int getIndex() {
        return index;
    }

    void setType(int type) {
        this.type = type;
    }

    int getType() {
        return type;
    }

    public int compareTo(Vertex that) {
        if (this.type != that.type)
            return that.type - this.type;
        return this.index - that.index;
    }

}
