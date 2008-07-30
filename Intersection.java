import java.awt.geom.Point2D;

class Intersection {

    private Boundary b;
    private Point2D p;

    public Intersection(Boundary b, Point2D p) {
	this.b = b;
	this.p = p;
    }

    public Boundary getBoundary() {
	return b;
    }

    public Point2D getPoint() {
	return p;
    }

}
