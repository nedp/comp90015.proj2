package com.github.nedp.comp90015.proj2.job.worker.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.SocketFactory;
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
    private final BufferedReader input;
    private long freeMemory;
    private Status status;

    // Set the properties of the certificate at startup.
    // TODO swap to JVM args from hard coding for the SSL key store.
    static {
        final String keyDir = "/src/main/resources/assignment2KeyStr";
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + keyDir);
        System.setProperty("javax.net.ssl.trustStorePassword", "comp90015");
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + keyDir);
        System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
    }

    public RemoteWorker(@NotNull String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;
        final SocketFactory factory = SSLSocketFactory.getDefault();
        final Socket workerSocket = factory.createSocket(hostname,port);

        // Get the port for the job from the Worker
        this.input = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
        jobPort = Integer.parseInt(this.input.readLine().split(":")[1]);
    }

    /**
     * Maintains the connection to the remote host.
     */
    public synchronized void maintain() {
        this.status = Status.RUNNING;
        this.input.lines().forEachOrdered(
            line -> RemoteWorker.this.freeMemory = Long.parseLong(line.split(":")[1]));
        this.status = Status.DISCONNECTED;
    }

    @NotNull
    @Override
    public Result execute(Job job) {
        if (this.status == Status.DISCONNECTED) {
            return Result.DISCONNECTED;
        }

        final SocketFactory factory = SSLSocketFactory.getDefault();
        try {
            final Socket jobSocket = factory.createSocket(hostname, jobPort);
            PrintWriter jobOut = new PrintWriter(jobSocket.getOutputStream());
            String JSONJobString = job.toJSON();
            if(null== JSONJobString){
            	System.out.println("Error Turning Job into JSON");
            	return Result.FAILED;
            }
            jobOut.println(JSONJobString);
            jobOut.flush();
            
            
            ;
            
            BufferedReader jobIn = new BufferedReader(new InputStreamReader(jobSocket.getInputStream()));
            String resultType = jobIn.readLine();
            System.out.println("returned string was " + resultType);
            if(resultType.equals(Job.PARSE_ERROR)){
            	//jobSocket.close();
            	System.out.println("There was an Error in parsing the job via JSON");
            	return Result.FAILED;
            	
            } else if(resultType.equals("FAILED")){
            	if(!job.files.log.exists()){
            		job.files.log.createNewFile();
            	}
            	
            	//read all the lines to the logfile
            	PrintWriter pw = new PrintWriter(job.files.log);
            	String line;
            	while(null != (line = jobIn.readLine())){
            		pw.println(line);
            	}
            	pw.close();
            	jobSocket.close();
            	return Result.FAILED;
            	
            } else if(resultType.equals("FINISHED")){
            	if(!job.files.out.exists()){
            		job.files.out.createNewFile();
            	}
            	
            	//read all the lines to the output file
            	PrintWriter pw = new PrintWriter(job.files.out);
            	String line;
            	while(null != (line = jobIn.readLine())){
            		pw.println(line);
            	}
            	pw.close();
            	//jobSocket.close();
            	return Result.FINISHED;
            }
            
            
        } catch (IOException e) {
        	System.out.println("IO exception");
        	e.printStackTrace();
            return Result.DISCONNECTED;
        }
        
        return Result.DISCONNECTED; // TODO
    }

    @NotNull
    @Override
    public Status status() {
        return this.status; // TODO
    }

    @Override
    public long freeMemory() {
        return freeMemory;
    }

    @NotNull
    @Override
    public String identifier() {
        return String.format("RemoteWorker at %s:%d", this.hostname, this.port);
    }

}
