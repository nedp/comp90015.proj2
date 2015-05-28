package com.github.nedp.comp90015.proj2.job.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.github.nedp.comp90015.proj2.job.Job;
import com.github.nedp.comp90015.proj2.job.Status;
import com.github.nedp.comp90015.proj2.job.StatusTracker;


public class Worker {

	
	static final int defaultPort = 80;

	public static void main(String[] args) throws InterruptedException, IOException {
		
		int portNumber = defaultPort;
		
		for(int i = 0; i +1 < args.length; i++ ){
			if(args.equals("-p")){
				portNumber = Integer.parseInt(args[i+1]);
				
			}
		}
		System.out.println("portnum is " + portNumber);
		
		// open listening port
		
		
		// accept connections and create new 'remote master' for each
		
		// 
		
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
	

}
