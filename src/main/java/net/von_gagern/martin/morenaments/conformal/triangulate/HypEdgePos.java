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

class HypEdgePos {

    private Vertex v;

    private double a;

    private double b;

    private double c;

    private double d;

    public HypEdgePos() {
        assign(0, 0, 1, 0);
    }

    public HypEdgePos(double a, double b, double c, double d) {
        assign(a, b, c, d);
    }

    public Vertex getVertex() {
        return v;
    }

    public void setVertex(Vertex v) {
        this.v = v;
    }

    public HypEdgePos assign(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        return this;
    }

    public HypEdgePos assignProduct(HypEdgePos h1, HypEdgePos h2) {
        assign(h1.a*h2.c - h1.b*h2.d + h1.c*h2.a + h1.d*h2.b,
               h1.a*h2.d + h1.b*h2.c + h1.c*h2.b - h1.d*h2.a,
               h1.a*h2.a + h1.b*h2.b + h1.c*h2.c - h1.d*h2.d,
               h1.a*h2.b - h1.b*h2.a + h1.c*h2.d + h1.d*h2.c);
        return this;
    }

    public HypEdgePos concatenate(HypEdgePos that) {
        return assignProduct(this, that);
    }

    public HypEdgePos preConcatenate(HypEdgePos that) {
        return assignProduct(that, this);
    }

    public Point2D dehomogenize() {
        double denom = c*c + d*d;
        return new Point2D.Double((a*c + b*d)/denom, (b*c - a*d)/denom);
    }

    private static final double EPS_NORMALIZE = 1e-10;

    public void normalize() {
        double det = c*c + d*d - a*a - b*b;
        if (Math.abs(det - 1.) > EPS_NORMALIZE) {
            double denom = Math.sqrt(det);
            a /= denom;
            b /= denom;
            c /= denom;
            d /= denom;
        }
    }

    public HypEdgePos derive(Vertex v, double len) {
        if (this.v == null)
            throw new IllegalStateException("Vertex needs to be set");
        if (this.v == v)
            return this;
        HypEdgePos res = new HypEdgePos();
        res.assignTransRot(len);
        res.preConcatenate(this);
        res.setVertex(v);
        return res;
    }

    public HypEdgePos derive(Vertex v, double len, double angle) {
        HypEdgePos res = new HypEdgePos();
        res.assignRotation(angle);
        res.preConcatenate(derive(v, len));
        res.setVertex(v);
        return res;
    }

    /**
     * Makes this transformation a translation transformation.
     * This transformation is set to the following transformation:
     * <pre>
     *      / exp(d)+1  exp(d)-1 \
     * Td = |                    |
     *      \ exp(d)-1  exp(d)+1 /
     * </pre>
     * which describes a translation along the real axis by the distance d
     * @param d the translation distance
     * @return a reference to this transformation
     */
    public HypEdgePos assignTranslation(double d) {
        if (Double.isInfinite(d))
            assign(d > 0 ? 1. : -1., 0., 1., 0.);
        else
            assign(Math.expm1(d), 0., Math.exp(d) + 1., 0.);
        normalize();
        return this;
    }

    /**
     * Makes this transformation a rotation transformation.
     * This transformation is set to the following transformation:
     * <pre>
     *      / exp(i*&phi;/2)     0        \
     * R&phi; = |                         |
     *      \    0        exp(-i*&phi;/2) /
     * </pre>
     * @param phi the angle of rotation
     * @return a reference to this transformation
     */
    public HypEdgePos assignRotation(double phi) {
        double arg = phi/-2.;
        assign(0., 0., Math.cos(arg), Math.sin(arg));
        return this;
    }

    public HypEdgePos assignTransRot(double d) {
        if (Double.isInfinite(d))
            assign(0., d > 0 ? 1. : -1., 0., 1.);
        else
            assign(0., Math.expm1(d), 0., Math.exp(d) + 1.);
        normalize();
        return this;
    }

    @Override public String toString() {
        return "HypEdgePos[" + a + " + " + b + "i, " + c + " + " + d + "i]";
    }

}
