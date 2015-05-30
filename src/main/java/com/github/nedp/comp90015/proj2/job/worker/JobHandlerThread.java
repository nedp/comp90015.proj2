package com.github.nedp.comp90015.proj2.job.worker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

import com.github.nedp.comp90015.proj2.job.*;

public class JobHandlerThread implements Runnable {

	protected Job job;
	protected SSLSocket socket;
	protected RemoteMaster master;
	
	public JobHandlerThread( SSLSocket s, RemoteMaster m) {
		this.master = m;
		this.socket = s;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		//TODO this is just a stub, so the details will need to be done
		//socket is already running
		//recv job 
		// unmarshall the files 
		// write the files to a new directory
		//TODO change the strings to whichever points to the new dir
		
		this.job = new Job(new Job.Files("fileHandle"), new StatusTracker());
		//else if there are time/space limits, use the other constructor:
		//this.job = new Job(new Job.Files("jar", "in", "out", "log"),stattracker, null, null);
		
		job.run();
		
		try{
			PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
			BufferedReader jobOutput;
			if(job.currentStatus().equals(Status.FINISHED)){
				socketOut.println("Finished");
				jobOutput = new BufferedReader(new FileReader(job.files.out));
				
				
			}else if(job.currentStatus().equals(Status.FINISHED)){
				socketOut.println("Failed");
				jobOutput = new BufferedReader(new FileReader(job.files.log));
				
				//otherwise something is wrong
			} else throw new Exception();
			
			
			/* TODO in this loop you send the jobOutput back down the socket
			while(){
				//output file back down socket
			}
			*/
		}catch (Exception e){
			//TODO stub
			return;
		}

	}

}
