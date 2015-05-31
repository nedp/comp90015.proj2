package com.github.nedp.comp90015.proj2.job.worker;

import java.io.*;
import java.net.Socket;

import com.github.nedp.comp90015.proj2.job.*;
import com.github.nedp.comp90015.proj2.job.worker.master.Result;

public class JobHandlerThread implements Runnable {

	protected Job job;
	protected Socket socket;
	protected RemoteMaster master;
	
	public JobHandlerThread(Socket s, RemoteMaster m) {
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

		final PrintWriter socketOut;
		try {
			socketOut = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("couldn't return job result to master:");
			e.printStackTrace();
			return;
		}

		BufferedReader jobOutput;
		try {
			switch (job.currentStatus()) {
				case FINISHED:
					socketOut.println(Result.FAILED);
					jobOutput = new BufferedReader(new FileReader(job.files.out));
					break;

				case FAILED:
					socketOut.println(Result.FAILED);
					jobOutput = new BufferedReader(new FileReader(job.files.log));
					break;

				case WAITING:
				case RUNNING:
					throw new RuntimeException("Job#run returned before the Job terminated.");
			}
		} catch (FileNotFoundException e) {
			System.out.println("couldn't open job output:");
			e.printStackTrace();
			return;
		}

		// TODO send the jobOutput back down the socket
	}
}
