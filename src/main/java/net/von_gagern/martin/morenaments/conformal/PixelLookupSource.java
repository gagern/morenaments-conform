package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.Point2D;

public interface PixelLookupSource {

    public int getRGB(double x, double y);

    public int getRGB(Point2D p);

}
