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

package net.von_gagern.martin.morenaments.conformal.groups;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import net.von_gagern.martin.morenaments.conformal.triangulate.Triangulation;

public abstract class Group implements Cloneable {

    private static final Logger logger = Logger.getLogger(Group.class);

    protected final int[] euclideanAngles;

    protected int[] hyperbolicAngles;

    private HypTrafo[] insidenessChecks;

    private HypTrafo[] generators;

    private AffineTransform euclideanTransform;

    protected Group(int[] euclideanAngles) {
	this.euclideanAngles = euclideanAngles;
    }

    public int[] getEuclideanAngles() {
	return euclideanAngles;
    }

    public void setEuclideanTransform(AffineTransform tr) {
        euclideanTransform = tr;
    }

    public AffineTransform getEuclideanTransform() {
        return euclideanTransform;
    }

    public boolean checkHyperbolicAngles(int[] hyperbolicAngles) {
	return true;
    }

    public void setHyperbolicAngles(int[] hyperbolicAngles) {
	if (!checkHyperbolicAngles(hyperbolicAngles))
	    throw new IllegalArgumentException("Invalid hyperbolic angles");
	this.hyperbolicAngles = hyperbolicAngles;
        insidenessChecks = null;
        generators = null;
    }

    public HypTrafo[] getGenerators() {
        if (generators == null)
            generators = constructGenerators();
        return generators;
    }

    protected abstract HypTrafo[] constructGenerators();

    public HypTrafo[] getInsidenessChecks() {
        if (insidenessChecks == null)
            insidenessChecks = constructInsidenessChecks();
        return insidenessChecks;
    }

    protected abstract HypTrafo[] constructInsidenessChecks();

    public abstract List<Point2D> getHypTileCorners();

    public List<Point2D> getEucTileCorners() {
        double[] coords = getEucCornerCoordinates();
        assert coords.length % 2 == 0;
        List<Point2D> corners = new ArrayList<Point2D>(coords.length / 2);
        for (int i = 0; i < coords.length; i += 2)
            corners.add(new Point2D.Double(coords[i], coords[i + 1]));
        return corners;
    }

    protected abstract double[] getEucCornerCoordinates();

    public abstract double getEuclideanCornerAngle(int index);

    @Override public Group clone() {
        try {
            Group that = (Group)super.clone();
            if (hyperbolicAngles != null)
                that.setHyperbolicAngles(hyperbolicAngles.clone());
            return that;
        }
        catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public Triangulation getTriangulation() {
        return null;
    }

    public String toImageComment() {
        StringBuilder buf = new StringBuilder();
        buf.append("class=").append(getClass().getName()).append(";");
        AffineTransform eucTrans = getEuclideanTransform();
        if (eucTrans != null) {
            buf.append("eucTrans=[");
            String delim = "";
            double[] matrix = new double[6];
            eucTrans.getMatrix(matrix);
            for (int i = 0; i < 6; ++i) {
                buf.append(delim).append(matrix[i]);
                delim=",";
            }
            buf.append("];");
        }
        if (hyperbolicAngles != null) {
            buf.append("hypAngles=[");
            String delim = "";
            for (int i = 0; i < hyperbolicAngles.length; ++i) {
                buf.append(delim).append(hyperbolicAngles[i]);
                delim=",";
            }
            buf.append("];");
        }
        return buf.toString();
    }

    public static Group fromImageComment(String comment) throws IOException {
        if (comment == null) return null;
        if (!comment.startsWith("class=")) return null;
        int semiColon = comment.indexOf(";");
        if (semiColon == -1) semiColon = comment.length();
        String className = comment.substring("class=".length(), semiColon);
        try {
            Class<? extends Group> clazz;
            clazz = Class.forName(className).asSubclass(Group.class);
            Group g = clazz.newInstance();
            g.initFromImageComment(comment.substring(semiColon + 1));
            logger.info("Group information restored from image comment.");
            return g;
        }
        catch (Exception e) {
            IOException e2 =
                new IOException("Illegal image comment: " + comment);
            e2.initCause(e);
            throw e2;
        }
    }

    public void initFromImageComment(String comment) {
        for (String pair: comment.split(";")) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) continue;
            String key = parts[0], value = parts[1];
            if ("eucTrans".equals(key)) {
                String list = value.substring(1, value.length()-1);
                String[] elements = list.split(",");
                double[] numbers = new double[elements.length];
                for (int i = 0; i < elements.length; ++i)
                    numbers[i] = Double.parseDouble(elements[i]);
                setEuclideanTransform(new AffineTransform(numbers));
            }
            else if ("hypAngles".equals(key)) {
                String list = value.substring(1, value.length()-1);
                String[] elements = list.split(",");
                int[] numbers = new int[elements.length];
                for (int i = 0; i < elements.length; ++i)
                    numbers[i] = Integer.parseInt(elements[i]);
                setHyperbolicAngles(numbers);
            }
            else {
                logger.warn("Unknown key: " + key);
            }
        }
    }

    public static P6m p6m() {
        return new P6m();
    }

    public static P6 p6() {
        return new P6();
    }

    public static P31m p31m() {
        return new P31m();
    }

    public static P3m1 p3m1() {
        return new P3m1();
    }

    public static P3 p3() {
        return new P3();
    }

    public static P4g p4g() {
        return new P4g();
    }

    public static P4m p4m() {
        return new P4m();
    }

    public static P4 p4() {
        return new P4();
    }

    public static Pmm pmm() {
        return new Pmm();
    }

    public static P2 p2() {
        return new P2();
    }

    public static Pgg pgg() {
        return new Pgg();
    }

    public static P1 p1() {
        return new P1();
    }

    public enum EuclideanGroup {
        p6m, p6, p31m, p3m1, p3, p4g, p4m, p4,
        coloredLizards,
    }

    public static Group getInstance(EuclideanGroup g) {
        switch (g) {
        case p6m: return new P6m();
        case p6: return new P6();
        case p31m: return new P31m();
        case p3: return new P3();
        case p4g: return new P4g();
        case p4m: return new P4m();
        case p4: return new P4();
        case coloredLizards: return new ColoredLizards();
        default: throw new IllegalArgumentException("Invalid group: " + g);
        }
    }

}
