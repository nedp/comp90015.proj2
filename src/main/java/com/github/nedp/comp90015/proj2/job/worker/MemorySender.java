package com.github.nedp.comp90015.proj2.job.worker;

import java.io.IOException;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

/**
 * Sends the free memory of the Worker to the Master
 * 
 * @author Charles Talbot
 *
 */
public class MemorySender implements Runnable {
	
	private WorkerStatus workerStatus = new WorkerStatus();
	private final SSLSocket socket;
	
	public MemorySender(SSLSocket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			PrintWriter outToMaster = new PrintWriter( socket.getOutputStream(), true);
			while(true){ //TODO This needs to be based on the status of the master
				// Send the memory status to the master
				outToMaster.println("Memory:" + workerStatus.getFreeMemory());
				// Sleep for a moment before sending again
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
