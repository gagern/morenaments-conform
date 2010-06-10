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

package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;

class ArcEdge extends Edge {

    private Point2D c;

    private double r;

    public ArcEdge(Vertex p1, Vertex p2, Point2D center, double radius) {
        super(p1, p2);
        c = center;
        r = radius;
    }

    @Override protected Vertex createCenter() {
        Vertex p1 = getP1(), p2 = getP2();
        double x = (p1.getX() + p2.getX())/2. - c.getX();
        double y = (p1.getY() + p2.getY())/2. - c.getY();
        double f = r/Math.hypot(x, y);
        return new Vertex(c.getX() + f*x, c.getY() + f*y);
    }

    @Override protected ArcEdge createFirstPart() {
        return new ArcEdge(getP1(), getCenter(), c, r);
    }

    @Override protected ArcEdge createSecondPart() {
        return new ArcEdge(getCenter(), getP2(), c, r);
    }

}
