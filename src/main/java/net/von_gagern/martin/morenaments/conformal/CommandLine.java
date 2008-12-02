package net.von_gagern.martin.morenaments.conformal;

import java.awt.Component;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.JFrame;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import net.von_gagern.martin.getopt.OptException;
import net.von_gagern.martin.getopt.OptPair;
import net.von_gagern.martin.getopt.OptParser;

import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class CommandLine {

    public static void main(String[] args) throws Exception {
        configureLog4j();
        CommandLine cl = new CommandLine(args);
        cl.run();
    }

    public static void configureLog4j() {
        DOMConfigurator.configure(CommandLine.class.getResource("log4j.xml"));
    }

    private Integer size;

    private Group group;

    private CommandLineOption action;

    private List<Job> jobs;

    private boolean terminateAfterJobs = true;

    public CommandLine(String[] args) throws OptException {
        jobs = new ArrayList<Job>();
        OptParser<CommandLineOption> parser =
            OptParser.getInstance(CommandLineOption.values());
        for (OptPair<CommandLineOption> pair: parser.parse(args)) {
            CommandLineOption key = pair.getKey();
            String value = pair.getValue();
            if (key == null) {
                argument(value);
                continue;
            }
            switch (key) {
            case size:
                size(pair);
                break;
            case group:
                group(pair);
                break;
            case tile:
            case opengl:
                action = key;
                break;
            case debug:
                debug(pair);
                break;
            case help:
                System.out.println("Usage: java " +
                                   CommandLine.class.getName() +
                                   " [options...]");
                System.out.println();
                System.out.println("Options:");
                parser.printHelp(System.out);
                System.exit(0);
                break;
            default:
                throw new OptException("Unsupported command line option", pair);
            }
        }
    }

    private void argument(String arg) throws OptException {
        if (action == null)
            throw new OptException("No arguments allowed", arg);
        switch (action) {
        case tile:
            tileArg(arg);
            break;
        case opengl:
            openglArg(arg);
            terminateAfterJobs = false;
            break;
        default:
            throw new IllegalStateException("Unexpected action");
        }
    }

    private void tileArg(String arg) throws OptException {
        if (group == null)
            throw new OptException("Group required first", arg);
        jobs.add(new TileJob(arg));
    }

    private void openglArg(String arg) throws OptException {
        jobs.add(new OpenglJob(arg));
    }

    private void size(OptPair<CommandLineOption> pair) throws OptException {
        this.size = intValue(pair, 2, (1 << 16) - 1);
    }

    private int intValue(OptPair<CommandLineOption> pair, int min, int max)
        throws OptException
    {
        try {
            int i = Integer.parseInt(pair.getValue());
            if (i < min)
                throw new OptException("Argument less than " + min, pair);
            if (i > max)
                throw new OptException("Argument greater than " + max, pair);
            return i;
        }
        catch (NumberFormatException e) {
            throw new OptException("Integer argument expected", pair);
        }
    }

    private void group(OptPair<CommandLineOption> pair) throws OptException {
        String value = pair.getValue();
        String[] parts = value.split(",");
        Group g;
        try {
            Group.EuclideanGroup eg = Group.EuclideanGroup.valueOf(parts[0]);
            g = Group.getInstance(eg);
        }
        catch (IllegalArgumentException e) {
            throw new OptException("No such group", pair);
        }
        try {
            int[] a = new int[parts.length - 1];
            for (int i = 0; i < a.length; ++i)
                a[i] = Integer.parseInt(parts[i + 1]);
            g.setHyperbolicAngles(a);
        }
        catch (NumberFormatException e) {
            throw new OptException("Integer angles expected", pair);
        }
        catch (IllegalArgumentException e) {
            throw new OptException("Invalid angles", pair);
        }
        group = g;
    }

    private void debug(OptPair<CommandLineOption> pair) throws OptException {
        String value = pair.getValue();
        String[] parts;
        if (value == null)
            parts = new String[0];
        else
            parts = value.split(":");
        if (parts.length > 2)
            throw new OptException("Illegal debug request", pair);
        String name = "", levelStr = "";
        if (parts.length > 1) levelStr = parts[1];
        if (parts.length > 0) name = parts[0];
        Logger logger;
        Level level;
        if (name.equals("")) logger = Logger.getRootLogger();
        else logger = Logger.getLogger(name);
        if (levelStr.equals("")) level = Level.DEBUG;
        else level = Level.toLevel(levelStr, null);
        if (level == null) throw new OptException("Illegal level", levelStr);
        logger.setLevel(level);
    }

    public void run() {
        if (action == null && jobs.isEmpty()) runGUI();
        else runJobs();
    }

    public void runJobs() {
        int errors = 0;
        for (Job job: jobs) {
            try {
                job.run();
            }
            catch (Exception e) {
                e.printStackTrace();
                errors = 1;
            }
        }
        if (terminateAfterJobs)
            System.exit(errors);
    }

    public void runGUI() {
        JFrame frm = new JFrame("morenaments conformal");
        GUI gui = new GUI(this, frm);
        frm.getContentPane().add(gui);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.pack();
        frm.setLocationByPlatform(true);
        frm.setVisible(true);
    }

    public int getSize(int defaultValue) {
        if (size == null) return defaultValue;
        return size;
    }

    private interface Job {
        public void run() throws Exception;
    }

    private class TileJob implements Job {

        private Integer size = CommandLine.this.size;
        private Group group = CommandLine.this.group;
        private File file;

        public TileJob(String arg) {
            file = new File(arg);
        }

        public void run() throws Exception {
            BufferedImage inImg = ImageIO.read(file);
            int inSize = inImg.getWidth();
            if (inImg.getHeight() != inSize)
                throw new Exception("Input image has to be square");
            int outSize = inSize;
            if (size != null) outSize = size.intValue();
            AffineTransform tr;
            tr = SimplePixelLookupSource.unitDiskTransform(inSize);
            PixelLookupSource pls = new SimplePixelLookupSource(inImg, tr);
            TilingRenderer t = new TilingRenderer(group);
            BufferedImage outImg = t.render(pls, outSize, null);
            String fileName = file.getName();
            int dotPos = fileName.lastIndexOf('.');
            String ext;
            if (dotPos > 0) {
                ext = fileName.substring(dotPos + 1);
            }
            else {
                file = new File(file.getParentFile(), fileName + ".png");
                ext = "png";
            }
            ImageIO.write(outImg, ext, file);
        }

    }

    private class OpenglJob implements Job {

        private Integer size = CommandLine.this.size;
        private Group group = CommandLine.this.group;
        private File file;

        public OpenglJob(String arg) {
            file = new File(arg);
        }

        private final String STD_METADATA_FORMAT = "javax_imageio_1.0";

        private String getComment(IIOMetadata meta) {
            final String XPATH;
            XPATH = "Text/TextEntry[@keyword='Comment']/@value";
            try {
                if (meta == null) return null;
                Node node = meta.getAsTree(STD_METADATA_FORMAT);
                if (node == null) return null;
                return XPathFactory.newInstance().newXPath().evaluate(XPATH, node);
            }
            catch (Exception e) {
                return null;
            }
        }

        public void run() throws Exception {
            ImageInputStream stream = new FileImageInputStream(file);
            Iterator<ImageReader> iri = ImageIO.getImageReaders(stream);
            if (!iri.hasNext())
                throw new IOException("Unsupported format: " + file.getName());
            ImageReader reader = iri.next();
            reader.setInput(stream);
            String comment;
            BufferedImage img;
            try {
                comment = getComment(reader.getImageMetadata(0));
                img = reader.read(0);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IOException("File contains no images", e);
            }
            Group g = Group.fromImageComment(comment);
            JFrame frm = new JFrame("OpenGlRpl");
            GLCapabilities capa = new GLCapabilities();
            //capa.setSampleBuffers(true);
            GLCanvas glad = new GLCanvas(capa);
            OpenGlRpl ogrpl = new OpenGlRpl(img, g);
            glad.addGLEventListener(ogrpl);
            frm.getContentPane().add((Component)glad);
            frm.setSize(500, 500);
            frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frm.setVisible(true);
        }

    }

}
