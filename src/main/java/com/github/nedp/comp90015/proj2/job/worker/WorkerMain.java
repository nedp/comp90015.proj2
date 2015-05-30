package com.github.nedp.comp90015.proj2.job.worker;


import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class WorkerMain {

	
	static final int defaultPort = 80;
	static ArrayList<RemoteMaster> masterList;

	public static void main(String[] args) throws InterruptedException, IOException {
		masterList = new ArrayList<RemoteMaster>();
		boolean keepRunning= true;
		int portNumber = defaultPort;
		
		for(int i = 0; i +1 < args.length; i++ ){
			if(args[i].equals("-p")){
				portNumber = Integer.parseInt(args[i+1]);
				
			}
		}
		System.out.println("portnum is " + portNumber);
		
		// open listening port
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();	
		SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(portNumber);
		// accept connections and create new 'remote master' for each
		while(keepRunning){
			try{
				SSLSocket sock = (SSLSocket) ss.accept();
		
				RemoteMaster master = new RemoteMaster(sock, ssf);
				masterList.add(master);
				Thread t = new Thread(master);
				t.start();
			
			} catch(Exception e){
				//decide whether to keep running or not
				keepRunning = false;
			}
			
		}
		
		
		
	}
	
	protected static boolean masterDisconnected(RemoteMaster r){
		return masterList.remove(r);
	}
	

}
