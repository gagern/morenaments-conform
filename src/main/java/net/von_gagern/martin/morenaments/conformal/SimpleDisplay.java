package net.von_gagern.martin.morenaments.conformal;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

class SimpleDisplay extends JPanel {

    private Group g;

    private TilingRenderer tr;

    public static void main(String[] args) throws Exception {
        JFrame frm = new JFrame("Simple Display");
        frm.getContentPane().add(new SimpleDisplay(args));
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.pack();
        frm.setLocationByPlatform(true);
        frm.setVisible(true);
    }

    public SimpleDisplay(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java " + SimpleDisplay.class.getName() +
                               " group rotationCounts...");
            System.exit(0);
        }
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.LIGHT_GRAY);
        g = (Group)Group.class.getMethod(args[0]).invoke(null);
        int[] ea = g.getEuclideanAngles();
        if (args.length - 1 != ea.length) {
            System.err.println("Needed " + ea.length + " angle arguments.");
            System.exit(1);
        }
        int[] ha = new int[ea.length];
        for (int i = 0; i < ea.length; ++i) {
            ha[i] = Integer.parseInt(args[i + 1]);
            System.out.println(ea[i] + " -> " + ha[i]);
        }
        g.setHyperbolidAngles(ha);
        tr = new TilingRenderer(g);
    }

    private BufferedImage osi;

    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int r = Math.min(getWidth(), getHeight())/2;
        if (osi == null || osi.getWidth() != 2*r || osi.getHeight() != 2*r) {
            osi = acceleratedImage(2*r, 2*r, g);
            tr.render(r, osi);
        }
        g.drawImage(osi, getWidth()/2 - r, getHeight()/2 - r, this);
    }

    private BufferedImage acceleratedImage(int width, int height, Graphics g) {
        if (!(g instanceof Graphics2D)) return null;
        Graphics2D g2d = (Graphics2D)g;
        GraphicsConfiguration gc = g2d.getDeviceConfiguration();
        return gc.createCompatibleImage(width, height, Transparency.BITMASK);
    }

}
