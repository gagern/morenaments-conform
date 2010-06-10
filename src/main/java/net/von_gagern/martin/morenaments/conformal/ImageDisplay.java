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

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import net.von_gagern.martin.morenaments.conformal.groups.Group;

class ImageDisplay extends JInternalFrame {

    private final ImagePane ip;

    private final JScrollPane sp;

    private Group group;

    public ImageDisplay(String title, BufferedImage img) {
        super(title, true, true, true, true);
        getContentPane().setLayout(new BorderLayout());
        ip = new ImagePane(img);
        sp = new JScrollPane(ip);
        getContentPane().add(sp);
    }

    public BufferedImage getImage() {
        return ip.getImage();
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

}
