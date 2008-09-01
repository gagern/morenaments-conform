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
import java.util.List;
import javax.imageio.ImageIO;
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

import de.tum.in.gagern.hornamente.BusyFeedback;
import de.tum.in.gagern.ornament.Ornament;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

public class GUI extends JDesktopPane {

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
        size = 800;
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
            euc.addHooks(new EucHooks(this));
            eucFrame = new JFrame("Euclidean (for transformation)");
            eucFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            eucFrame.getContentPane().add(euc);
            eucFrame.pack();
            eucFrame.setLocationByPlatform(true);
            eucFrame.setVisible(true);
        }
    }

    public void recognizedImage(String name, Group group, AffineTransform tr,
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
        File file = fc.getSelectedFile();
        try {
            BufferedImage img = ImageIO.read(file);
            addImage(file.getName(), img, null);
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
            ImageIO.write(img, ext, file);
            currentImageDisplay.setTitle(fileName);
        }
        catch (IOException e) {
            error(e);
        }
    }


    private void hypTile() {
        Group g = currentImageDisplay.getGroup();
        if (g == null) {
            Group.EuclideanGroup eg = (Group.EuclideanGroup)
                JOptionPane.showInputDialog(this,
                    "Select euclidean group",
                    "Select Group",
                    JOptionPane.QUESTION_MESSAGE, null,
                    Group.EuclideanGroup.values(), null);
            if (eg == null) return;
            g = Group.getInstance(eg);
        }
        else {
            g = g.clone();
        }
        AngleCountsDlg acdlg = new AngleCountsDlg(this);
        acdlg.setGroup(g);
        if (!acdlg.showModal()) return;
        g.setHyperbolicAngles(acdlg.getAngles());
        String newTitle = "HypTile of " + currentImageDisplay.getTitle();
        BufferedImage img = currentImageDisplay.getImage();
        start(new HypTileTask(img, g, size, newTitle));
    }

    private void tiling() {
        Group g = currentImageDisplay.getGroup();
        if (g == null) {
            Group.EuclideanGroup eg = (Group.EuclideanGroup)
                JOptionPane.showInputDialog(this,
                    "Select euclidean group",
                    "Select Group",
                    JOptionPane.QUESTION_MESSAGE, null,
                    Group.EuclideanGroup.values(), null);
            if (eg == null) return;
            g = Group.getInstance(eg);
            AngleCountsDlg acdlg = new AngleCountsDlg(this);
            acdlg.setGroup(g);
            if (!acdlg.showModal()) return;
            g.setHyperbolicAngles(acdlg.getAngles());
        }
        String newTitle = "Tiling of " + currentImageDisplay.getTitle();
        BufferedImage img = currentImageDisplay.getImage();
        start(new TilingTask(img, g, size, newTitle));
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
            int size = inImg.getWidth();
            if (inImg.getHeight() != size)
                throw new Exception("Input image has to be square");
            AffineTransform tr;
            tr = SimplePixelLookupSource.unitDiskTransform(size);
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

    public void error(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.toString(),
                                      e.getClass().getName(),
                                      JOptionPane.ERROR_MESSAGE);
    }

}
