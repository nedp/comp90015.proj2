package com.github.nedp.comp90015.proj2.job.worker;


import java.io.IOException;
import java.net.BindException;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class WorkerMain {

	
	static final int defaultPort = 44444;
	// TODO remove if not used - static ArrayList<RemoteMaster> masterList;
	private static final String keyDir = "/src/main/resources/assignment2KeyStr";

	public static void main(String[] args) throws InterruptedException, IOException {
		// TODO remove if not used - masterList = new ArrayList<RemoteMaster>();
		boolean keepRunning= true;
		int portNumber = defaultPort;
		
		for(int i = 0; i +1 < args.length; i++ ){
			if(args[i].equals("-p")){
				portNumber = Integer.parseInt(args[i+1]);
			}
		}
		System.out.println("portnum is " + portNumber);

		// TODO swap to JVM args from hard coding for the SSL key store.
		System.setProperty("javax.net.ssl.keyStore",System.getProperty("user.dir") + keyDir);
		System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
		
		// open listening port
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket ss = null;
		try{
			ss = (SSLServerSocket) ssf.createServerSocket(portNumber);
		}catch(BindException e){
			System.out.println("cannot bind to port " + portNumber );
			e.printStackTrace();
			return;
		}
		// accept connections and create new 'remote master' for each
		while(keepRunning){
			try{
				Socket sock = ss.accept();

				RemoteMaster master = new RemoteMaster(sock, ssf);
				System.out.printf("OPENED port %d - connected to Master at %s:%d\n",
					sock.getLocalPort(), sock.getInetAddress(), sock.getPort());
				// TODO remove if not used - masterList.add(master);

				Thread t = new Thread(master);
				t.setDaemon(true);
				t.start();
			} catch(Exception e){
				//decide whether to keep running or not
				keepRunning = false;
			}
		}
	}

	// TODO remove if not used -
	// protected static boolean masterDisconnected(RemoteMaster r){
	// 	return masterList.remove(r);
	// }
}
