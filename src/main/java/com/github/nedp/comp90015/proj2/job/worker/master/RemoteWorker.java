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

    public RemoteWorker(@NotNull String hostname, int port) throws UnknownHostException, IOException {
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
        
    }

	@NotNull
    @Override
    public Result execute(Job job) {
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
        return 0; // TODO
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
