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

import java.util.Comparator;

abstract class ReverseDoubleComparator<T> implements Comparator<T> {

    abstract protected double getValue(T o);

    public int compare(T o1, T o2) {
        double v1 = getValue(o1);
        double v2 = getValue(o2);
        if (v1 < v2) return 1;
        if (v1 > v2) return -1;
        return 0;
    }

    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

}
