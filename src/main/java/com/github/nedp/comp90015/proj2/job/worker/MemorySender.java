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
	
	WorkerStatus workerStatus = new WorkerStatus();
	SSLSocket socket;
	
	public MemorySender(SSLSocket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			PrintWriter outToMaster = new PrintWriter( socket.getOutputStream());
			while(true){
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
