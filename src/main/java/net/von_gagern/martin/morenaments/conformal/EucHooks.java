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
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.JMenuBar;
import javax.swing.AbstractAction;
import de.tum.in.gagern.ornament.Hooks;
import de.tum.in.gagern.ornament.Ornament;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

class EucHooks extends Hooks {

    private Ornament euc;

    private GUI gui;

    public EucHooks(Ornament euc, GUI gui) {
        this.euc = euc;
        this.gui = gui;
        try {
            JMenuBar menuBar = euc.getMenuBar();
            JMenu menu = (JMenu)menuBar.getSubElements()[0];
            Component[] comps = menu.getMenuComponents();
            int pos;
            for (pos = comps.length - 1; pos >= 0; --pos)
                if (comps[pos] instanceof JSeparator)
                    break;
            if (pos < 0) {
                menu.addSeparator();
                pos = comps.length + 1;
            }
            menu.insert(new RenderAnisotropic(), pos);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override public void recognizedImage(de.tum.in.gagern.ornament.Group group,
                                          int ax, int ay, int bx, int by,
                                          BufferedImage median) {
        Group g;
        try {
            g = (Group)Group.class.getMethod(group.getName()).invoke(null);
        }
        catch (Exception e) {
            System.err.println(e);
            return;
        }
        g.setEuclideanTransform(new AffineTransform(ax, ay, bx, by, 0, 0));
        gui.recognizedImage(group.getName(), g, median);
    }

    private class RenderAnisotropic extends AbstractAction {

        public RenderAnisotropic() {
            super("Render to conformal");
        }

        public void actionPerformed(ActionEvent evnt) {
            de.tum.in.gagern.ornament.Group group = euc.getGroup();
            Group g;
            try {
                g = (Group)Group.class.getMethod(group.getName()).invoke(null);
            }
            catch (Exception e) {
                System.err.println(e);
                return;
            }
            g.setEuclideanTransform(euc.getVectorTransform());
            int size = gui.getImageSize();
            BufferedImage img = euc.renderTileAnisotropic(size, size, false);
            gui.recognizedImage(group.getName(), g, img);
        }

    }

}
