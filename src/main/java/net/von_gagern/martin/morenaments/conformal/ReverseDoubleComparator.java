package net.von_gagern.martin.morenaments.conformal;

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
