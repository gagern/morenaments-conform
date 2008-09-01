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
