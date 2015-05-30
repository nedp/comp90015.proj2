package com.github.nedp.comp90015.proj2.job.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.github.nedp.comp90015.proj2.job.*;


public class WorkerMain {

	
	static final int defaultPort = 80;
	static ArrayList<RemoteMaster> masterList;

	public static void main(String[] args) throws InterruptedException, IOException {
		masterList = new ArrayList<RemoteMaster>();
		boolean keepRunning= true;
		int portNumber = defaultPort;
		
		for(int i = 0; i +1 < args.length; i++ ){
			if(args.equals("-p")){
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
				break;
			}
			
		}
		
		// accept jobs from each master
		//dummyjob
		/*specify the job specifically*/

/*
		Job jobToRun = new Job(new Job.Files(
				new File("src/test/resources/test_jobs/do_nothing.jar"),
				new File("src/test/resources/test_jobs/do_nothing.in"), 
				new File("src/test/resources/test_jobs/do_nothing.out"),
				new File("src/test/resources/test_jobs/do_nothing.log"))
			, new StatusTracker()); 
		
*/
		Job jobToRun = new Job(
				new Job.Files("src/test/resources/test_jobs/do_nothing"),
				new StatusTracker()); 
		
		jobToRun.currentStatus();
		Thread jobThread = new Thread(jobToRun);
		jobThread.start();
		
		boolean failed = false;
		while (!jobToRun.currentStatus().equals(Status.FINISHED)){
			Thread.sleep(1000);
			if(jobToRun.currentStatus().equals(Status.FAILED)){
				failed = true;
				break;
			}
			
		}
		jobThread.join();
		
		File result = jobToRun.outFile();
		if(failed){
			result = jobToRun.logFile();
		}
		
		FileReader fr = new FileReader(result);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
	
		while(null !=(line = br.readLine())){
			System.out.println(line);
		}
		
		br.close();
		fr.close();
		

	}
	
	protected static boolean masterDisconnected(RemoteMaster r){
		return masterList.remove(r);
	}
	

}
