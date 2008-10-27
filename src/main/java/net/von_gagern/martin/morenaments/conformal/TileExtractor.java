package net.von_gagern.martin.morenaments.conformal;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class TileExtractor {

    private final Logger logger =
        Logger.getLogger(TileExtractor.class);

    private Group g;

    public TileExtractor(Group group) {
        this.g = group;
    }

    public BufferedImage render(PixelLookupSource source,
                                int size, BufferedImage target) {
        logger.debug("rendering image at size " + size);
        int r = size/2;
        size = r*2;
        if (target == null ||
            target.getWidth() != size || target.getHeight() != size) {
            target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }
        AffineTransform tr = SimplePixelLookupSource.unitDiskTransform(size);
        AffineTransform tri;
        try { tri = tr.createInverse(); }
        catch (NoninvertibleTransformException e) { throw new Error(e); }
        Point2D p = new Point2D.Double();
        Vec2C v1 = new Vec2C(0, 0, 1, 0), v2 = new Vec2C();
        for (int y = 0; y < size; ++y) {
            PIXELS: for (int x = 0; x < size; ++x) {
                // Logical integral coordinates are at pixel corners.
                // Therefore pixel centers are between integral coordinates.
                p.setLocation(x + .5, y + .5);
                tri.transform(p, p);
                v1.x.assign(p.getX(), p.getY());
                if (p.distance(0, 0) >= 1) continue;
                for (HypTrafo ic: g.getInsidenessChecks()) {
                    ic.transform(v1, v2);
                    if (v2.signImag() < 0)
                        continue PIXELS;
                }
                // Logical integral coordinates are at pixel corners.
                // Therefore pixel centers are between integral coordinates.
                int rgb = source.getRGB(p);
                target.setRGB(x, y, rgb);
            }
        }
        logger.debug("rendered image at size " + size + " complete");
        return target;
    }

}
