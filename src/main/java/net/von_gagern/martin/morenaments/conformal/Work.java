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

import javax.swing.SwingUtilities;

import de.tum.in.gagern.hornamente.BusyFeedback;

abstract class Work implements Runnable {

    private final BusyFeedback busy;

    public Work(BusyFeedback busy) {
	this.busy = busy;
    }

    public Work() {
	this(null);
    }

    protected abstract void doInBackground() throws Exception;

    protected void done() { }

    protected void exception(Exception e) {
	e.printStackTrace();
    }

    public void run() {
	if (busy != null) busy.start();
	try {
	    doInBackground();
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			done();
		    }
		});
	}
	catch (final Exception e) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			exception(e);
		    }
		});
	}
	finally {
	    if (busy != null) busy.finish();
	}
    }

}
