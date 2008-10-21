package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

public class Vertex extends Point2D.Double {

    public Vertex orbifoldElement;

    public Vertex(double x, double y) {
        super(x, y);
    }

    public Vertex(Point2D p) {
        super(p.getX(), p.getY());
    }

    public Vertex() {
        super();
    }

    public Vertex getOrbifoldElement() {
        return orbifoldElement;
    }

    public void setOrbifoldElement(Vertex orbifoldElement) {
        this.orbifoldElement = orbifoldElement;
    }

}
