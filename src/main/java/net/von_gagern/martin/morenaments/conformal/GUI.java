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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import javax.media.opengl.GLCapabilities;
import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.BusyFeedback;
import de.tum.in.gagern.ornament.Ornament;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class GUI extends JDesktopPane {

    private final Logger logger = Logger.getLogger(GUI.class);

    private CommandLine cl;

    private final BusyFeedback busy;

    private Ornament euc;

    private JFrame eucFrame;

    private ImageDisplay currentImageDisplay;

    private List<Action> imageActions;

    private File currentDir;

    private int size;

    public GUI(CommandLine cl, JFrame frame) {
        this.cl = cl;
        busy = new BusyFeedback(this);
        setPreferredSize(new Dimension(600, 600));
        currentDir = new File(".");
        size = cl.getSize(1024);
        JMenuBar menuBar = createMenuBar();
        if (frame != null) {
            frame.setJMenuBar(menuBar);
        }
        else {
            JInternalFrame menuFrame;
            menuFrame = new JInternalFrame("Menu", false, false, false, true);
            menuFrame.getContentPane().add(menuBar);
            menuFrame.pack();
            add(menuFrame);
            menuFrame.setVisible(true);
        }
        for (String arg: cl.getArgs()) {
            loadImage(new File(arg));
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu;
        imageActions = new ArrayList<Action>();

        menu = new JMenu("File");
        bar.add(menu);
        menu.add(new JMenuItem(new AbstractAction("Start euc") {
                public void actionPerformed(ActionEvent e) {
                    startEuc();
                }}));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Load image..."){
                public void actionPerformed(ActionEvent e) {
                    loadImage();
                }}));
        menu.add(new JMenuItem(aal(new AbstractAction("Save image...") {
                public void actionPerformed(ActionEvent e) {
                    saveImage();
                }}, imageActions)));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Quit") {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }}));

        menu = new JMenu("Transform");
        bar.add(menu);
        menu.add(new JMenuItem(aal(new AbstractAction("Hyperbolic Tile") {
                public void actionPerformed(ActionEvent e) {
                    hypTile();
                }}, imageActions)));
        menu.add(new JMenuItem(aal(new AbstractAction("Render Tiling") {
                public void actionPerformed(ActionEvent e) {
                    tiling();
                }}, imageActions)));
        menu.add(new JMenuItem(aal(new AbstractAction("Render Grid") {
                public void actionPerformed(ActionEvent e) {
                    grid();
                }}, imageActions)));
        menu.add(new JMenuItem(aal(new AbstractAction("Extract Tile") {
                public void actionPerformed(ActionEvent e) {
                    tileExtract();
                }}, imageActions)));

        menu = new JMenu("View");
        bar.add(menu);
        menu.add(new JMenuItem(aal(new AbstractAction("OpenGL RPL") {
                public void actionPerformed(ActionEvent e) {
                    openGlRpl();
                }}, imageActions)));

        for (Action a: imageActions)
            a.setEnabled(false);
        return bar;
    }

    private static Action aal(Action action, List<Action> list) {
        list.add(action);
        return action;
    }

    public void startEuc() {
        if (eucFrame != null) {
            eucFrame.setVisible(true);
            eucFrame.toFront();
        }
        else {
            euc = new Ornament(null, false);
            euc.addHooks(new EucHooks(euc, this));
            eucFrame = new JFrame("Euclidean (for transformation)");
            eucFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            eucFrame.getContentPane().add(euc);
            eucFrame.pack();
            eucFrame.setLocationByPlatform(true);
            eucFrame.setVisible(true);
        }
    }

    public void recognizedImage(String name, Group group,
                                BufferedImage median) {
        addImage(name, median, group);
    }

    public Component getAngleDialogOwner() {
        return this;
    }

    public ImageDisplay addImage(String title, BufferedImage img, Group group) {
        ImageDisplay display = new ImageDisplay(title, img);
        display.setGroup(group);
        display.addInternalFrameListener(new ImageFrameListener());
        display.pack();
        add(display);
        display.setVisible(true);
        try {
            display.setSelected(true);
        }
        catch (PropertyVetoException e) {
            // Not much we can do about this. Shouldn't happen, though.
            e.printStackTrace();
        }
        return display;
    }

    private class ImageFrameListener extends InternalFrameAdapter {
        @Override public void internalFrameActivated(InternalFrameEvent e) {
            setCurrentDisplay((ImageDisplay)e.getInternalFrame());
        }
        @Override public void internalFrameDeactivated(InternalFrameEvent e) {
            clearCurrentDisplay((ImageDisplay)e.getInternalFrame());
        }
    }

    private void setCurrentDisplay(ImageDisplay display) {
        currentImageDisplay = display;
        for (Action a: imageActions)
            a.setEnabled(true);
    }

    private void clearCurrentDisplay(ImageDisplay display) {
        if (currentImageDisplay != display) return;
        currentImageDisplay = null;
        for (Action a: imageActions)
            a.setEnabled(false);
    }

    private void loadImage() {
        JFileChooser fc = new JFileChooser(currentDir);
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        currentDir = fc.getCurrentDirectory();
        loadImage(fc.getSelectedFile());
    }

    private void loadImage(File file) {
        try {
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
                logger.info("Comment = " + comment);
                img = reader.read(0);
            }
            catch (IndexOutOfBoundsException e) {
                IOException e2 = new IOException("File contains no images");
                e2.initCause(e);
                throw e2;
            }
            // BufferedImage img = ImageIO.read(file);
            Group group = Group.fromImageComment(comment);
            addImage(file.getName(), img, group);
        }
        catch (IOException e) {
            error(e);
        }
    }

    private void saveImage() {
        JFileChooser fc = new JFileChooser(currentDir);
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        currentDir = fc.getCurrentDirectory();
        File file = fc.getSelectedFile();
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
        BufferedImage img = currentImageDisplay.getImage();
        try {
            Iterator<ImageWriter> iwi = ImageIO.getImageWritersBySuffix(ext);
            if (!iwi.hasNext())
                throw new IOException("Unsupported format: " + ext);
            ImageWriter writer = iwi.next();
            IIOMetadata meta = writer.getDefaultImageMetadata
                                   (new ImageTypeSpecifier(img), null);
            Group g = currentImageDisplay.getGroup();
            if (g != null) setComment(meta, g.toImageComment());
            writer.setOutput(new FileImageOutputStream(file));
            writer.write(new IIOImage(img, null, meta));
            writer.dispose();
            // ImageIO.write(img, ext, file);
            currentImageDisplay.setTitle(fileName);
        }
        catch (IOException e) {
            error(e);
        }
    }

    private final String STD_METADATA_FORMAT = "javax_imageio_1.0";

    private void setComment(IIOMetadata meta, String comment) {
        try {
            DOMImplementationRegistry registry;
            registry = DOMImplementationRegistry.newInstance();
            DOMImplementation impl = registry.getDOMImplementation("XML 3.0");
            Document doc = impl.createDocument(null, STD_METADATA_FORMAT, null);
            Element root, text, entry;
            root = doc.getDocumentElement();
            root.appendChild(text = doc.createElement("Text"));
            text.appendChild(entry = doc.createElement("TextEntry"));
            entry.setAttribute("keyword", "Comment");
            entry.setAttribute("value", comment);
            // The following three are necessary to avoid a Java VM bug:
            // http://bugs.sun.com/view_bug.do?bug_id=5106550
            entry.setAttribute("encoding", "US-ASCII");
            entry.setAttribute("compression", "none");
            entry.setAttribute("language", "");
            meta.mergeTree(STD_METADATA_FORMAT, root);
        }
        catch (Exception e) {
            logger.error(e);
        }
    }

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
            logger.error(e);
            return null;
        }
    }

    private Group queryGroup(boolean newAngles) {
        Group g = currentImageDisplay.getGroup();
        if (g == null) {
            Group.EuclideanGroup eg = (Group.EuclideanGroup)
                JOptionPane.showInputDialog(this,
                    "Select euclidean group",
                    "Select Group",
                    JOptionPane.QUESTION_MESSAGE, null,
                    Group.EuclideanGroup.values(), null);
            if (eg == null) return null;
            g = Group.getInstance(eg);
            newAngles = true;
        }
        else if (newAngles) {
            g = g.clone();
        }
        if (newAngles) {
            AngleCountsDlg acdlg = new AngleCountsDlg(this);
            acdlg.setGroup(g);
            if (!acdlg.showModal()) return null;
            g.setHyperbolicAngles(acdlg.getAngles());
        }
        return g;
    }

    private void hypTile() {
        Group g = queryGroup(true);
        if (g == null) return;
        String newTitle = "HypTile of " + currentImageDisplay.getTitle();
        BufferedImage img = currentImageDisplay.getImage();
        start(new HypTileTask(img, g, size, newTitle));
    }

    private void tiling() {
        Group g = queryGroup(false);
        if (g == null) return;
        String newTitle = "Tiling of " + currentImageDisplay.getTitle();
        BufferedImage img = currentImageDisplay.getImage();
        start(new TilingTask(img, g, size, newTitle));
    }

    private void grid() {
        Group g = queryGroup(false);
        if (g == null) return;
        String newTitle = "Grid of " + currentImageDisplay.getTitle();
        start(new GridTask(g, size, newTitle));
    }

    private void tileExtract() {
        Group g = queryGroup(false);
        if (g == null) return;
        String newTitle = "HypTile of " + currentImageDisplay.getTitle();
        BufferedImage img = currentImageDisplay.getImage();
        start(new TileExtractTask(img, g, size, newTitle));
    }

    private void openGlRpl() {
        Group g = queryGroup(false);
        BufferedImage img = currentImageDisplay.getImage();
	JFrame frm = new JFrame("OpenGlRpl");
	GLCapabilities capa = new GLCapabilities();
        OpenGlRpl ogrpl = new OpenGlRpl(img, g);
	frm.getContentPane().add(ogrpl.getComponent());
	frm.setSize(500, 500);
	frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frm.setVisible(true);
    }

    public int getImageSize() {
        return size;
    }

    private void start(Work work) {
        Thread t = new Thread(work);
        t.setDaemon(true);
        t.start();
    }

    private class HypTileTask extends Work {
        
        private final String title;
        private final BufferedImage inImg;
        private final Group g;
        private final int size;

        private BufferedImage outImg;

        public HypTileTask(BufferedImage img, Group g, int size, String title) {
            super(busy);
            this.inImg = img;
            this.g = g;
            this.size = size;
            this.title = title;
        }

        protected void doInBackground() throws Exception {
            int w = inImg.getWidth(), h = inImg.getHeight();
            AffineTransform tr = AffineTransform.getScaleInstance(w, h);
            PixelLookupSource pls = new SimplePixelLookupSource(inImg, tr);
            TileTransformer tt = new TileTransformer(g);
            tt.transform();
            outImg = tt.render(pls, size, outImg);
        }

        protected void done() {
            addImage(title, outImg, g);
        }

        protected void exception(Exception e) {
            error(e);
        }

    }

    private class TilingTask extends Work {
        
        private final String title;
        private final BufferedImage inImg;
        private final Group g;
        private final int size;

        private BufferedImage outImg;

        public TilingTask(BufferedImage img, Group g, int size, String title) {
            super(busy);
            this.inImg = img;
            this.g = g;
            this.size = size;
            this.title = title;
        }

        protected void doInBackground() throws Exception {
            int inSize = inImg.getWidth();
            if (inImg.getHeight() != inSize)
                throw new Exception("Input image has to be square");
            AffineTransform tr;
            tr = SimplePixelLookupSource.unitDiskTransform(inSize);
            PixelLookupSource pls = new SimplePixelLookupSource(inImg, tr);
            TilingRenderer t = new TilingRenderer(g);
            outImg = t.render(pls, size, outImg);
        }

        protected void done() {
            addImage(title, outImg, g);
        }

        protected void exception(Exception e) {
            error(e);
        }

    }

    private class GridTask extends Work {
        
        private final String title;
        private final Group g;
        private final int size;

        private BufferedImage outImg;

        public GridTask(Group g, int size, String title) {
            super(busy);
            this.g = g;
            this.size = size;
            this.title = title;
        }

        protected void doInBackground() throws Exception {
            GridRenderer t = new GridRenderer(g);
            outImg = t.render(size, outImg);
        }

        protected void done() {
            addImage(title, outImg, g);
        }

        protected void exception(Exception e) {
            error(e);
        }

    }

    private class TileExtractTask extends Work {
        
        private final String title;
        private final BufferedImage inImg;
        private final Group g;
        private final int size;

        private BufferedImage outImg;

        public TileExtractTask(BufferedImage img, Group g, int size, String title) {
            super(busy);
            this.inImg = img;
            this.g = g;
            this.size = size;
            this.title = title;
        }

        protected void doInBackground() throws Exception {
            int inSize = inImg.getWidth();
            if (inImg.getHeight() != inSize)
                throw new Exception("Input image has to be square");
            AffineTransform tr;
            tr = SimplePixelLookupSource.unitDiskTransform(inSize);
            PixelLookupSource pls = new SimplePixelLookupSource(inImg, tr);
            TileExtractor te = new TileExtractor(g);
            outImg = te.render(pls, size, outImg);
        }

        protected void done() {
            addImage(title, outImg, g);
        }

        protected void exception(Exception e) {
            error(e);
        }

    }

    public void error(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.toString(),
                                      e.getClass().getName(),
                                      JOptionPane.ERROR_MESSAGE);
    }

}
