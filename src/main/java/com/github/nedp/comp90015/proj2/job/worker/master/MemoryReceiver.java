package com.github.nedp.comp90015.proj2.job.worker.master;

/**
 * Receives constant memory updates from each
 * connected Worker
 * @author Charles Talbot
 *
 */
public class MemoryReceiver implements Runnable {
	
	RemoteWorker worker;
	
	public MemoryReceiver(RemoteWorker worker){
		this.worker = worker;
	}

	@Override
	public void run() {
		// TODO Wait for responses from the worker and update the Remote Worker accordingly
		
	}
	
}
