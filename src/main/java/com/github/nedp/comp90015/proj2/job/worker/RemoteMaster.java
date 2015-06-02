package com.github.nedp.comp90015.proj2.job.worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;

public class RemoteMaster implements Runnable{

	protected ArrayList<JobHandlerThread> jobThreadList;
	private Socket socket;
	private ServerSocketFactory socketFactory;

	public RemoteMaster(Socket s, ServerSocketFactory ssf) {
		socket = s;
		socketFactory = ssf;
		jobThreadList = new ArrayList<>();
	}

	@Override
	public void run() {
		// open listening port
		final ServerSocket serverSocketForJobs;
		final PrintWriter outToMaster;
		try {
			outToMaster = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.printf("couldn't access stream to master: %s\n", e.getMessage());
			return;
		}
		try {
			serverSocketForJobs = socketFactory.createServerSocket();
			serverSocketForJobs.bind(null);
			System.out.printf("OPENED port %d - listening for jobs from %s:%d\n",
				serverSocketForJobs.getLocalPort(), socket.getInetAddress(), socket.getPort());
		} catch (IOException e) {
			System.out.printf("couldn't create job server socket: %s\n", e.getMessage());
			return;
		}
		outToMaster.println("send jobs at this port:" + serverSocketForJobs.getLocalPort());

		// Begin a thread for sending the memory constantly to the Master
		final MemorySender sender = new MemorySender(socket, serverSocketForJobs);
		Thread memoryThread = new Thread(sender);
		memoryThread.setDaemon(true);
		memoryThread.start();

		// accept connections and create new job handler for each
		while (sender.isConnected()) {
			final Socket jobSocket;
			try {
				jobSocket = serverSocketForJobs.accept();
			} catch (IOException e) {
				System.out.printf("couldn't receive the next job in port %d: %s\n",
					serverSocketForJobs.getLocalPort(), e.getMessage());
				break;
			}
			JobHandlerThread jht = new JobHandlerThread(jobSocket, this);

			jobThreadList.add(jht);
			(new Thread(jht)).start();
		}

		sender.disconnect();
	}

	/*** in case you want to check if the same address is connecting somehow
	 * 
	 * @return the inet address of the connected Master
	 */
	protected InetAddress getAddress(){
		return socket.getInetAddress();
	}

}
