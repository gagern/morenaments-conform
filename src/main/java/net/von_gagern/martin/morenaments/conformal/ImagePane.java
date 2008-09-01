package net.von_gagern.martin.morenaments.conformal;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ImagePane extends JPanel implements Scrollable {

    private final BufferedImage img;

    private int maxWidth = 1024;

    private int maxHeight = 700;

    public ImagePane(BufferedImage img) {
        this.img = img;
        setSize(img.getWidth(), img.getHeight());
    }

    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, (getWidth() - img.getWidth())/2,
                    (getHeight() - img.getHeight())/2, this);
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(img.getWidth(), img.getHeight());
    }

    public Dimension getPreferredScrollableViewportSize() {
        int w = Math.min(maxWidth, img.getWidth());
        int h = Math.min(maxHeight, img.getHeight());
        return new Dimension(w, h);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        return 16;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL)
            return visibleRect.width / 2;
        else
            return visibleRect.height / 2;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public BufferedImage getImage() {
        return img;
    }

}
