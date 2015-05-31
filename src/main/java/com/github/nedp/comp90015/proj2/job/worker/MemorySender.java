package com.github.nedp.comp90015.proj2.job.worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Sends the free memory of the Worker to the Master
 * 
 * @author Charles Talbot
 *
 */
public class MemorySender implements Runnable {
	
	private WorkerStatus workerStatus = new WorkerStatus();
	private final Socket socket;
	private final ServerSocket jobSocket;
	private boolean isConnected = true;

	public MemorySender(Socket socket, ServerSocket jobSocket){
		this.socket = socket;
		this.jobSocket = jobSocket;
	}

	synchronized void disconnect() {
		// Only disconnect once.
		if (!this.isConnected) {
			return;
		}
		this.isConnected = false;
		System.out.printf("Disconnecting from master at %s:%d\n",
			socket.getInetAddress(), socket.getPort());

		// Close the job listener socket.
		try {
			jobSocket.close();
			System.out.printf("CLOSED port %d for jobs from %s:%d\n",
				jobSocket.getLocalPort(), socket.getInetAddress(), socket.getPort());
		} catch (IOException e) {
			System.out.printf("FAILED to close socket for jobs from  %s:%d (port %d)\n",
				socket.getInetAddress(), socket.getPort(), jobSocket.getLocalPort(), e.getMessage());
		}
		// Close the status socket.
		try {
			socket.close();
			System.out.printf("CLOSED port %d for sending status to %s:%d\n",
				socket.getLocalPort(), socket.getInetAddress(), socket.getPort());
		} catch (IOException e) {
			System.out.printf("FAILED to close socket for sending status to %s:%d (port %d): %s\n",
				socket.getInetAddress(), socket.getPort(), socket.getLocalPort(), e.getMessage());
		}
	}

	synchronized boolean isConnected() {
		return isConnected;
	}

	@Override
	public void run() {
		final PrintWriter outToMaster;
		try {
			outToMaster = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.printf("couldn't access output stream: %s\n", e.getMessage());
			this.disconnect();
			return;
		}

		while (!outToMaster.checkError()) {
			// Send the memory status to the master
			outToMaster.println("Memory:" + workerStatus.getFreeMemory());
			// Sleep for a moment before sending again
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("interrupted while waiting to send load report");
				break;
			}
		}
		this.disconnect();
	}
}
