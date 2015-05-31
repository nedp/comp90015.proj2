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
		
		BufferedReader socketIn;
		try {
			socketIn = new BufferedReader( new InputStreamReader(socket.getInputStream()));
			
		} catch (IOException e1) {
			System.out.printf("could not get streamReader on socket: %s\n", e1.getMessage());
			return;
		}
		
		this.job = Job.fromJSON(socketIn);
		
		if(job == null){
			
			//TODO check if this is the best way to do this...
			System.out.println("Job received but it could not be parsed");
			try {
				new PrintWriter(socket.getOutputStream()).println("Bad Job");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return;
		}
		
		
		
		job.run();

		final PrintWriter socketOut;
		try {
			socketOut = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("couldn't return job result to master:");
			e.printStackTrace();
			return;
		}

		BufferedReader jobOutput = null;
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

		// write the whole result back to the master
		try {
			String line;
			while(null != ( line = jobOutput.readLine())){
				socketOut.println(line);
			};
		} catch (IOException e) {
			System.out.printf("Couldn't read output back to the master: %s\n", e.getMessage());
		}
		
		
		File parentDir = job.files.jar.getParentFile();
		job.files.jar.delete();
		job.files.in.delete();
		job.files.out.delete();
		job.files.log.delete();
		parentDir.delete();
		
		socketOut.close();
		try {
			socketIn.close();
			socket.close();
			
			jobOutput.close();
			
		} catch (IOException e) {
			System.out.println("couldn't close sockets & streams");
			e.printStackTrace();
		}
		
	
	}
}
