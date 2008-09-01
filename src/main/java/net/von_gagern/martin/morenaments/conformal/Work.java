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
