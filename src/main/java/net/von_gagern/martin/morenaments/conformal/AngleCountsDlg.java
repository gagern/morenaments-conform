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
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

import static java.awt.GridBagConstraints.REMAINDER;

class AngleCountsDlg extends JDialog {

    private JPanel anglePanel;

    private GridBagConstraints gbcLabel, gbcSpinner;

    private SpinnerNumberModel[] spinnerModels;

    private int[] angles;

    public AngleCountsDlg(Component owner) {
        super(JOptionPane.getFrameForComponent(owner), "Angle counts", true);
        Container cp = getContentPane();
        anglePanel = new JPanel();
        cp.add(anglePanel);
        anglePanel.setLayout(new GridBagLayout());
        gbcLabel = new GridBagConstraints();
        gbcSpinner = new GridBagConstraints();
        gbcSpinner.gridwidth = REMAINDER;
        JPanel buttonPanel = new JPanel();
        cp.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(new JButton(new ActionOK()));
        buttonPanel.add(new JButton(new ActionCancel()));
    }

    public void setGroup(Group g) {
        int[] eucCounts = g.getEuclideanAngles();
        spinnerModels = new SpinnerNumberModel[eucCounts.length];
        anglePanel.removeAll();
        for (int i = 0; i < eucCounts.length; ++i) {
            String text = MessageFormat.format("Angle {0}: {1} -> ",
                                               i + 1, eucCounts[i]);
            JLabel label = new JLabel(text);
            SpinnerNumberModel model =
                new SpinnerNumberModel(eucCounts[i], 0, Integer.MAX_VALUE, 1);
            spinnerModels[i] = model;
            JSpinner spinner = new JSpinner(model);
            label.setLabelFor(spinner);
            anglePanel.add(label, gbcLabel);
            anglePanel.add(spinner, gbcSpinner);
        }
    }

    public boolean showModal() {
        angles = null;
        pack();
        setVisible(true);
        return angles != null;
    }

    public int[] getAngles() {
        return angles;
    }

    private class ActionOK extends AbstractAction {
        public ActionOK() {
            super("OK");
        }
        public void actionPerformed(ActionEvent evnt) {
            angles = new int[spinnerModels.length];
            for (int i = 0; i < spinnerModels.length; ++i)
                angles[i] = spinnerModels[i].getNumber().intValue();
            setVisible(false);
        }
    }

    private class ActionCancel extends AbstractAction {
        public ActionCancel() {
            super("Cancel");
        }
        public void actionPerformed(ActionEvent evnt) {
            angles = null;
            setVisible(false);
        }
    }

}
