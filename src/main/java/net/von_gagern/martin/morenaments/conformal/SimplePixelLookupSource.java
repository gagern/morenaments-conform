/*
 * morenaments conformal - Hyperbolization of ornaments
 *                         via discrete conformal maps
 * Copyright (C) 2009-2010 Martin von Gagern
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
