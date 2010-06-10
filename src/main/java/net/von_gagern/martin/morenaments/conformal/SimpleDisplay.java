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
        g.setHyperbolicAngles(ha);
        tr = new TilingRenderer(g);
    }

    private BufferedImage osi;

    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = Math.min(getWidth(), getHeight());
        size -= size & 1;
        if (osi == null || osi.getWidth() != size || osi.getHeight() != size) {
            osi = acceleratedImage(size, size, g);
            tr.render(null, size, osi);
        }
        g.drawImage(osi, (getWidth() - size)/2, (getHeight() - size)/2, this);
    }

    private BufferedImage acceleratedImage(int width, int height, Graphics g) {
        if (!(g instanceof Graphics2D)) return null;
        Graphics2D g2d = (Graphics2D)g;
        GraphicsConfiguration gc = g2d.getDeviceConfiguration();
        return gc.createCompatibleImage(width, height, Transparency.BITMASK);
    }

}
