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

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.flat.Mesh2D;

class SvgWriter {

    private static final String PUBLIC =
        "-//W3C//DTD SVG 1.1//EN";

    private static final String SYSTEM =
        "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";

    private static final String DOCTYPE =
        "<!DOCTYPE svg PUBLIC \"" + PUBLIC + "\" \"" + SYSTEM + "\">"; 

    private static final String VERSION = "1.1";

    private static final String NS = "http://www.w3.org/2000/svg";

    private static final String INKSCAPE =
        "http://www.inkscape.org/namespaces/inkscape";

    private XMLStreamWriter svg;

    private String encoding = null;

    private double defaultStrokeWidth = 0.001;

    private Set<String> uniqueIds = new HashSet<String>();

    private int layers = 1;

    public SvgWriter(OutputStream out) throws XMLStreamException {
        encoding = "UTF-8";
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        svg = factory.createXMLStreamWriter(out, encoding);
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private void writeIdIfUnique(String id) throws XMLStreamException {
        if (id != null && uniqueIds.add(id))
            svg.writeAttribute("id", id);
    }

    public <V> void writeTriangles(LocatedMesh<V> mesh)
        throws XMLStreamException
    {
        svg.writeStartElement(NS, "g");
        writeIdIfUnique("triangles");
        svg.writeAttribute("stroke", "#006");
        svg.writeAttribute("stroke-width", format(defaultStrokeWidth));
        svg.writeAttribute("fill", "none");
        svg.writeAttribute("fill-opacity", "30%");
        svg.writeCharacters("\n");
        for (Iterator<? extends CorneredTriangle<? extends V>> i =
             mesh.iterator(); i.hasNext(); ){
            CorneredTriangle<? extends V> t = i.next();
            StringBuilder d = new StringBuilder();
            String mode = "M ";
            for (int j = 0; j < 3; ++j) {
                V v = t.getCorner(j);
                double x = mesh.getX(v);
                double y = mesh.getY(v);
                d.append(mode).append(x).append(' ').append(y);
                mode = " L ";
            }
            d.append(" Z");
            svg.writeEmptyElement(NS, "path");
            svg.writeAttribute("d", d.toString());
            svg.writeCharacters("\n");
        }
        svg.writeEndElement();
        svg.writeCharacters("\n");
    }

    public void writeMesh(Mesh2D mesh) throws XMLStreamException {
        interiorEdges(mesh.getInteriorEdges());
        boundary(mesh.getBoundary());
    }

    public void head(Rectangle2D rect, double width, double height)
        throws XMLStreamException
    {
        String x = format(rect.getX());
        String y = format(rect.getY());
        String w = format(rect.getWidth());
        String h = format(rect.getHeight());
        String viewBox = x + " " + y + " " + w + " " + h;
        if (encoding == null) svg.writeStartDocument("1.0");
        else svg.writeStartDocument(encoding, "1.0");
        svg.writeDTD(DOCTYPE);
        svg.writeCharacters("\n");
        svg.setDefaultNamespace(NS);
        svg.setPrefix("inkscape", INKSCAPE);
        svg.writeStartElement(NS, "svg");
        svg.writeDefaultNamespace(NS);
        svg.writeNamespace("inkscape", INKSCAPE);
        svg.writeAttribute("version", VERSION);
        svg.writeAttribute("width", format(width));
        svg.writeAttribute("height", format(height));
        svg.writeAttribute("viewBox", viewBox);
        svg.writeAttribute("fill", "none");
        svg.writeCharacters("\n");
        svg.writeStartElement(NS, "g");
        svg.writeAttribute(INKSCAPE, "groupmode", "layer");
        svg.writeAttribute(INKSCAPE, "label", "Layer 1");
        svg.writeCharacters("\n");
    }

    public void newLayer() throws XMLStreamException {
        svg.writeEndElement();
        svg.writeCharacters("\n");
        svg.writeStartElement(NS, "g");
        svg.writeAttribute(INKSCAPE, "groupmode", "layer");
        svg.writeAttribute(INKSCAPE, "label", "Layer " + (++layers));
        svg.writeCharacters("\n");
    }

    private void interiorEdges(Collection<? extends Line2D> edges)
        throws XMLStreamException
    {
        StringBuilder buf = new StringBuilder();
        String moveto = "M ";
        for (Line2D e: edges) {
            buf.append(moveto).append(format(e.getX1()));
            buf.append(' ').append(format(e.getY1()));
            buf.append(" L ").append(format(e.getX2()));
            buf.append(' ').append(format(e.getY2()));
            moveto = "\nM ";
        }
        svg.writeEmptyElement(NS, "path");
        writeIdIfUnique("interior");
        svg.writeAttribute("stroke", "#060");
        svg.writeAttribute("stroke-width", format(defaultStrokeWidth));
        svg.writeAttribute("d", buf.toString());
        svg.writeCharacters("\n");
    }

    private void boundary(Shape boundary) throws XMLStreamException {
        svg.writeEmptyElement(NS, "path");
        writeIdIfUnique("boundary");
        svg.writeAttribute("stroke", "#006");
        svg.writeAttribute("stroke-width", format(2*defaultStrokeWidth));
        svg.writeAttribute("d", walkPath(boundary.getPathIterator(null)));
        svg.writeCharacters("\n");
    }

    public void tail() throws XMLStreamException {
        svg.writeEndElement();
        svg.writeCharacters("\n");
        svg.writeEndElement();
        svg.writeCharacters("\n");
        svg.writeEndDocument();
        svg.close();
    }

    private String walkPath(PathIterator path) {
        StringBuilder buf = new StringBuilder();
        double[] c = new double[6];
        while(!path.isDone()) {
            int n;
            char t;
            switch (path.currentSegment(c)) {
            case PathIterator.SEG_MOVETO:
                t = 'M';
                n = 2;
                break;
            case PathIterator.SEG_LINETO:
                t = 'L';
                n = 2;
                break;
            case PathIterator.SEG_QUADTO:
                t = 'Q';
                n = 4;
                break;
            case PathIterator.SEG_CUBICTO:
                t = 'C';
                n = 6;
                break;
            case PathIterator.SEG_CLOSE:
                t = 'Z';
                n = 0;
                break;
            default:
                throw new AssertionError("Unknown path segment type");
            }
            buf.append(t);
            for (int i = 0; i < n; ++i)
                buf.append(' ').append(format(c[i]));
            path.next();
            if (!path.isDone())
                buf.append('\n');
        }
        return buf.toString();
    }

    private String format(double d) {
        return Double.toString(d);
    }

}
