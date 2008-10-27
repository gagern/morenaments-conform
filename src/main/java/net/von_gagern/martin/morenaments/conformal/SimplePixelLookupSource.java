package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class SimplePixelLookupSource implements PixelLookupSource {

    private final Point2D tmpPt = new Point2D.Double();

    private final BufferedImage img;

    private final AffineTransform tr;

    public SimplePixelLookupSource(BufferedImage img, AffineTransform tr) {
        this.img = img;
        this.tr = tr;
    }

    public int getRGB(double x, double y) {
        tmpPt.setLocation(x, y);
        return getRGB(tmpPt);
    }

    public int getRGB(Point2D p) {
        tr.transform(p, tmpPt);
        int x = (int)Math.floor(tmpPt.getX());
        int y = (int)Math.floor(tmpPt.getY());
        int w = img.getWidth(), h = img.getHeight();
        while (x < 0) x += w;
        while (x >= w) x -= w;
        while (y < 0) y += h;
        while (y >= h) y -= h;
        return img.getRGB(x, y);
    }

    public static AffineTransform unitDiskTransform(double size) {
        double r = size/2.;
        return new AffineTransform(r, 0, 0, r, r, r);
    }

}
