package com.github.nedp.comp90015.proj2.job.worker.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.github.nedp.comp90015.proj2.job.Job;

import org.jetbrains.annotations.NotNull;

/**
 * TODO
 * Created by nedp on 21/05/15.
 */
public class RemoteWorker implements Worker {
    private final String hostname;
    private final int port;
    private final int jobPort;
    private SSLSocket workerSocket;
    private long freeMemory;
    private static final String keyDir = "/src/main/resources/assignment2KeyStr";

    public RemoteWorker(@NotNull String hostname, int port) throws UnknownHostException, IOException {
    	
    	// Set the properties of the certificate
		System.setProperty("javax.net.ssl.trustStore",System.getProperty("user.dir") + keyDir);
    	System.setProperty("javax.net.ssl.trustStorePassword","comp90015");
    	
    	this.hostname = hostname;
        this.port = port;
        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        workerSocket = (SSLSocket) factory.createSocket(hostname,port);
        
        // Get the port for the job from the Worker
        BufferedReader input = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
        jobPort = Integer.parseInt(input.readLine().split(":")[1]);
        
        // Begins the status update thread
        Thread memoryThread = new Thread(new MemoryReceiver(this, workerSocket));
        memoryThread.setDaemon(true);
        memoryThread.start();
        
    }

	@NotNull
    @Override
    public Result execute(Job job) {
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		try {
			SSLSocket jobSocket = (SSLSocket) factory.createSocket(hostname,jobPort);
			// TODO Send jobs this way. God I'm tired, I'm going to sleep. Good luck.
		} catch (IOException e) {
			return Result.DISCONNECTED;
		}
		
        return Result.DISCONNECTED; // TODO
    }

    @NotNull
    @Override
    public Status status() {
        return Status.DISCONNECTED; // TODO
    }

    @Override
    public double cpuLoad() {
        return 0; // TODO
    }

    @Override
    public long freeMemory() {
        return freeMemory;
    }
    
    public void setFreeMemory(long freeMemory){
    	this.freeMemory = freeMemory;
    }

    @NotNull
    @Override
    public String identifier() {
        return String.format("Worker Stub %s:%d", this.hostname, this.port); // TODO
    }
}
