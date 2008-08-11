package net.von_gagern.martin.morenaments.conformal;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.von_gagern.martin.confoo.mesh.flat.Mesh2D;

public class SvgMeshWriter {

    private static final String PUBLIC =
        "-//W3C//DTD SVG 1.1//EN";

    private static final String SYSTEM =
        "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";

    private static final String DOCTYPE =
        "<!DOCTYPE svg PUBLIC \"" + PUBLIC + "\" \"" + SYSTEM + "\">"; 

    private static final String VERSION = "1.1";

    private static final String NS = "http://www.w3.org/2000/svg";

    private XMLStreamWriter svg;

    private String encoding = null;

    private double inset = 5;

    private double scale = 1;

    public SvgMeshWriter(OutputStream out) throws XMLStreamException {
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

    public double getInset() {
        return inset;
    }

    public void setInset(double inset) {
        this.inset = inset;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void writeMesh(Mesh2D mesh) throws XMLStreamException {
        Shape boundary = mesh.getBoundary();
        head(boundary.getBounds2D());
        interiorEdges(mesh.getInteriorEdges());
        boundary(boundary);
        tail();
    }

    private void head(Rectangle2D rect) throws XMLStreamException {
        String x = format(rect.getX() - inset);
        String y = format(rect.getY() - inset);
        String w = format(rect.getWidth() + 2*inset);
        String h = format(rect.getHeight() + 2*inset);
        String viewBox = x + " " + y + " " + w + " " + h;
        if (encoding == null) svg.writeStartDocument("1.0");
        else svg.writeStartDocument(encoding, "1.0");
        svg.writeDTD(DOCTYPE);
        svg.setDefaultNamespace(NS);
        svg.writeStartElement(NS, "svg");
        svg.writeDefaultNamespace(NS);
        svg.writeAttribute("version", VERSION);
        svg.writeAttribute("width", w);
        svg.writeAttribute("height", h);
        svg.writeAttribute("viewBox", viewBox);
        svg.writeAttribute("fill", "none");
    }

    private void interiorEdges(Collection<? extends Line2D> edges)
        throws XMLStreamException
    {
        StringBuilder buf = new StringBuilder();
        String moveto = "M ";
        for (Line2D e: edges) {
            buf.append(moveto).append(format(e.getX1()));
            buf.append(' ').append(format(e.getY1()));
            buf.append("L ").append(format(e.getX2()));
            buf.append(' ').append(format(e.getY2()));
            moveto = "\nM ";
        }
        svg.writeEmptyElement(NS, "path");
        svg.writeAttribute("id", "interior");
        svg.writeAttribute("stroke", "#060");
        svg.writeAttribute("stroke-width", "1");
        svg.writeAttribute("d", buf.toString());
    }

    private void boundary(Shape boundary) throws XMLStreamException {
        svg.writeEmptyElement(NS, "path");
        svg.writeAttribute("id", "boundary");
        svg.writeAttribute("stroke", "#006");
        svg.writeAttribute("stroke-width", "2");
        svg.writeAttribute("d", walkPath(boundary.getPathIterator(null)));
    }

    private void tail() throws XMLStreamException {
        svg.writeEndElement();
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
        return Double.toString(d*scale);
    }

}
