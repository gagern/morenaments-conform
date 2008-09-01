package net.von_gagern.martin.morenaments.conformal;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import de.tum.in.gagern.ornament.Hooks;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

class EucHooks extends Hooks {

    private GUI gui;

    public EucHooks(GUI gui) {
        this.gui = gui;
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
        AffineTransform tr = new AffineTransform(ax, ay, bx, by, 0, 0);
        gui.recognizedImage(group.getName(), g, tr, median);
    }

}
