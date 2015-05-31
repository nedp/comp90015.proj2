package com.github.nedp.comp90015.proj2.job.worker.master;

/**
 * Indicates that there are no Worker available.
 *
 * @author nedp
 */
public class WorkerUnavailableException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2846743255103066280L;

	WorkerUnavailableException() {
        super("no suitable Workers present");
    }
}
