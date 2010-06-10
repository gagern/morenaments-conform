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

    @Override public String toString() {
        return "Vertex[" + x + ", " + y + "]";
    }

}
