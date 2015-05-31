package com.github.nedp.comp90015.proj2.job.worker;

import java.io.*;
import java.net.Socket;

import com.github.nedp.comp90015.proj2.job.*;

public class JobHandlerThread implements Runnable {

	private Socket socket;

	public JobHandlerThread(Socket s, RemoteMaster m) {
		this.socket = s;
	}

	@Override
	public void run() {
		// Establish the socket streams.
		final PrintWriter socketOut;
		try {
			socketOut = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("couldn't return job result to master:");
			e.printStackTrace();
			return;
		}
		final BufferedReader socketIn;
		try {
			socketIn = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.out.printf("could not get streamReader on socket: %s\n", e.getMessage());
			return;
		}

		// Parse and run the job.
		String JSONStringJob = "";
		try {
			JSONStringJob = socketIn.readLine();
		} catch (IOException e) {
			System.out.println("IO Error reading from job socket");
			e.printStackTrace();
		}
		final Job job = Job.fromJSON(JSONStringJob);
		if(job == null){
			System.out.println("Job received but it could not be parsed");
			try {
				PrintWriter pw = new PrintWriter(socket.getOutputStream());
				pw.println(Job.PARSE_ERROR);
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		job.run();

		// Handle the result and close the sockets.
		try {
			this.handleJobResult(job, socketOut);
		} catch (FileNotFoundException e) {
			System.out.println("Job received but it could not be parsed");
			e.printStackTrace();
		}
		this.close(socketIn, socketOut);
	}

	private void handleJobResult(Job job, PrintWriter socketOut)
		throws FileNotFoundException {
		final BufferedReader jobOutput;
		switch (job.currentStatus()) {
			case FINISHED:
				socketOut.println("FINISHED");
				jobOutput = new BufferedReader(new FileReader(job.files.out));
				break;

			case FAILED:
				socketOut.println("FAILED");
				jobOutput = new BufferedReader(new FileReader(job.files.log));
				break;

			case WAITING:
			case RUNNING:
			default:
				throw new RuntimeException("Job#run returned before the Job terminated.");
		}

		// write the whole result back to the master
		try {
			String line;
			while (null != (line = jobOutput.readLine())){
				socketOut.println(line);
			}
		} catch (IOException e) {
			System.out.printf("Couldn't read output back to the master: %s\n", e.getMessage());
		}
	}

	private void close(BufferedReader socketIn, PrintWriter socketOut) {
		socketOut.close();
		try {
			socketIn.close();
			socket.close();

		} catch (IOException e) {
			System.out.println("couldn't close sockets & streams");
			e.printStackTrace();
		}
	}

	private boolean cleanup(Job job) {
		boolean ok = true;
		final File parentDir = job.files.jar.getParentFile();
		ok &= job.files.jar.delete();
		ok &= job.files.in.delete();
		ok &= job.files.out.delete();
		ok &= job.files.log.delete();
		ok &= parentDir.delete();
		return ok;
	}
}
