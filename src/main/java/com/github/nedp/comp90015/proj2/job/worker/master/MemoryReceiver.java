package com.github.nedp.comp90015.proj2.job.worker.master;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

/**
 * Receives constant memory updates from each
 * connected Worker
 * @author Charles Talbot
 *
 */
public class MemoryReceiver implements Runnable {
	
	RemoteWorker worker;
	SSLSocket socket;
	
	public MemoryReceiver(RemoteWorker worker, SSLSocket socket){
		this.worker = worker;
		this.socket = socket;
	}

	@Override
	public void run() {
		// TODO Wait for responses from the worker and update the Remote Worker accordingly
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream());
		while(true){
			worker.setFreeMemory(Long.parseLong(input.readLine().split(":")[1]));
		}
	}
	
}
