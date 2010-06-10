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

class BoundaryPair {

    private Boundary b1, b2;

    public BoundaryPair(Boundary b1, Boundary b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    public int hashCode() {
        return b1.hashCode() ^ b2.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof BoundaryPair)) return false;
        BoundaryPair that = (BoundaryPair)o;
        return (this.b1.equals(that.b1) && this.b2.equals(that.b2)) ||
               (this.b1.equals(that.b2) && this.b2.equals(that.b1));
    }

}
