package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import net.von_gagern.martin.confoo.conformal.Conformal;
import net.von_gagern.martin.confoo.mesh.flat.Mesh2D;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.flat.Vertex2D;
import net.von_gagern.martin.getopt.OptException;
import net.von_gagern.martin.getopt.OptPair;
import net.von_gagern.martin.getopt.OptParser;
import org.apache.log4j.xml.DOMConfigurator;

public class CommandLine {

    private static final double DEG = Math.PI/180.;

    private static final Double[] sampleCoords = {
        -0.13350561261177063, 0.3365321755409241, 60.,
        -0.1250976175069809, -0.21022525429725647, 60.,
        0.3598127067089081, 0.1868506819009781, 60.,
    };

    public static void main(String[] args)
        throws IOException, OptException, XMLStreamException, MeshException
    {
        configureLog4j();
        new CommandLine(args);
    }

    public CommandLine(String[] args)
        throws IOException, OptException, XMLStreamException, MeshException
    {
        List<Double> coords = new ArrayList<Double>(6);
        MeshCreator creator = new TriangleSubdivision();
        OutputStream hypOut = null, eucOut = null;
        Integer tc = null;

        OptParser<CommandLineOption> parser =
            OptParser.getInstance(CommandLineOption.values());
        for (OptPair<CommandLineOption> pair: parser.parse(args)) {
            CommandLineOption key = pair.getKey();
            String value = pair.getValue();
            if (key == null) {
                coords.add(Double.parseDouble(value));
                continue;
            }
            switch (key) {
            case help:
                System.out.println("Usage: java " +
                                   CommandLine.class.getName() +
                                   " [options...]" +
                                   " [x1 y1 a1 x2 y2 a2 x3 y3 a3]");
                System.out.println("      where xi/yi are corner coordinates");
                System.out.println("      and ai are corner angles in degrees.");
                System.out.println();
                System.out.println("Options:");
                parser.printHelp(System.out);
                break;
            case hypOut:
                if ("-".equals(value)) hypOut = System.out;
                else hypOut = new FileOutputStream(new File(value));
                break;
            case eucOut:
                if ("-".equals(value)) eucOut = System.out;
                else eucOut = new FileOutputStream(new File(value));
                break;
            case aes:
                creator = new AdaptiveEdgeSubdivision();
                break;
            case tc:
                tc = Integer.valueOf(value);
                break;
            default:
                throw new Error("Unsupported command line option: " + key);
            }
        }
        if (coords.isEmpty()) coords = Arrays.asList(sampleCoords);
        if (coords.size() != 9) {
            System.err.println("Expected exactly 9 numbers");
            System.exit(1);
        }

        if (tc != null) creator.setTargetTriangleCount(tc);
        Point2D[] ps = new Point2D[3];
        Map<Vertex2D, Double> angles = new HashMap<Vertex2D, Double>(3);
        for (int i = 0; i < 3; ++i) {
            ps[i] = new Point2D.Double(coords.get(3*i), coords.get(3*i + 1));
            angles.put(new Vertex2D(ps[i]), coords.get(3*i + 2)*DEG);
        }
        Mesh2D mesh = creator.createHypMesh(ps[0], ps[1], ps[2]);
        writeOut(mesh, hypOut);
        Conformal conformal = Conformal.getInstance(mesh);
        conformal.fixedBoundaryCurvature(angles);
        mesh = new Mesh2D(conformal.transform());
        writeOut(mesh, eucOut);
    }

    private void writeOut(Mesh2D mesh, OutputStream stream)
        throws XMLStreamException, IOException
    {
        if (stream == null) return;
        stream = new BufferedOutputStream(stream);
        SvgMeshWriter writer = new SvgMeshWriter(stream);
        writer.setScale(1000);
        writer.setInset(0.05);
        writer.writeMesh(mesh);
    }

    public static void configureLog4j() {
        DOMConfigurator.configure(CommandLine.class.getResource("log4j.xml"));
    }

}
