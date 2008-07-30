import java.awt.geom.Point2D;

interface Boundary {

    boolean contains(Point2D p);

    Point2D intersect(Point2D inside, Point2D outside);

}
