package com.github.nedp.comp90015.proj2.job.worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class RemoteMaster implements Runnable{

	protected ArrayList<JobHandlerThread> jobThreadList;
	private SSLSocket socket;
	private SSLServerSocketFactory socketFactory;
	private static final String keyDir = "/src/main/resources/assignment2KeyStr";
	
	public RemoteMaster(SSLSocket s, SSLServerSocketFactory ssf) {
		socket = s;
		socketFactory = ssf;
		jobThreadList = new ArrayList<JobHandlerThread>();
		
		
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		System.setProperty("javax.net.ssl.keyStore",System.getProperty("user.dir") + keyDir);
    	System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
		
		// open listening port
		
		SSLServerSocket serverSocketForJobs;
		try {
			serverSocketForJobs = (SSLServerSocket) socketFactory.createServerSocket();
			serverSocketForJobs.bind(null);
			
			PrintWriter outToMaster = new PrintWriter(socket.getOutputStream(), true);
			outToMaster.println("send jobs to me at this port:"+ serverSocketForJobs.getLocalPort());

			// Begin a thread for sending the memory constantly to the Master
			Thread memoryThread = new Thread(new MemorySender(socket));
			memoryThread.setDaemon(true);
			memoryThread.start();
			// accept connections and create new 'remote master' for each
			SSLSocket jobSocket;
	
			while(keepRunning ){
				jobSocket = (SSLSocket) serverSocketForJobs.accept();
				JobHandlerThread jht = new JobHandlerThread(jobSocket, this);
			
				jobThreadList.add(jht);
				(new Thread(jht)).start();
		
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			keepRunning = false;
			
		}
			
		
		
		
	}
	/*** in case you want to check if the same address is connecting somehow
	 * 
	 * @return the inet address of the connected Master
	 */
	protected InetAddress getAddress(){
		return socket.getInetAddress();
	}

}
