package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.*;
import java.util.*;
import de.tum.in.gagern.hornamente.PoincareLine;

class HypTriangulation extends HexagonalMesh {

    public static void main(String[] args) {
	if (args.length < 6 || args.length % 2 != 0) {
	    System.err.println("Arguments must be at least three pairs of x and y coordinates,");
	    System.err.println("all numbers separated by spaces");
	    System.exit(1);
	}
	Point2D[] corners = new Point2D[args.length / 2];
	for (int i = 0; i < corners.length; ++i) {
	    double x = Double.parseDouble(args[2*i]);
	    double y = Double.parseDouble(args[2*i + 1]);
	    corners[i] = new Point2D.Double(x, y);
	}
	System.out.println("Initializing mesh");
	HypTriangulation t = new HypTriangulation(corners);
	System.out.println("Triangulating mesh");
	t.triangulate();
	System.out.println("Writing mesh to hyp.*");
	t.write("hyp");
    }

    public HypTriangulation(Point2D... corners) {
	super();
	addBoundary(new CircleBoundary(0, 0, 1.1)); // avoid overflows
	Point2D origin = new Point2D.Double(0, 0);
	CircleBoundary b0, b1, b2;
	PoincareLine l = new PoincareLine();
	Point2D p1 = corners[corners.length - 1], p2 = corners[0];
	double cx, cy, r2;
	l.setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	cx = l.circleCenterX();
	cy = l.circleCenterY();
	r2 = p1.distanceSq(cx, cy);
	b2 = new CircleBoundary(cx, cy, r2);
	if (!b2.contains(origin)) b2.invert();
	addBoundary(b2);
	b0 = b2;
	for (int i = 1; i < corners.length; ++i) {
	    p1 = p2;
	    p2 = corners[i];
	    b1 = b2;
	    l.setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	    cx = l.circleCenterX();
	    cy = l.circleCenterY();
	    r2 = p1.distanceSq(cx, cy);
	    b2 = new CircleBoundary(cx, cy, r2);
	    if (!b2.contains(origin)) b2.invert();
	    addBoundary(b2);
	    corner(b1, b2, p1);
	}
	corner(b2, b0, p2);
    }

}
